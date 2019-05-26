package io.mikejzx.github.roboticsproject;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*
	Invaluable source:
	https://www.androidauthority.com/adding-bluetooth-to-your-app-742538/
*/

public class BlueToothHandler implements IToastable {

	public boolean secureSocket = false;
	
	private Thread connectionThread;
	
	private BluetoothDevice btDevice;
	private BluetoothSocket btSocket;
	private OutputStream oStream;
	private boolean connected = false;
	private final boolean supported;
	private final ArrayAdapter<String> arrayAdapter;
	
	private final Activity a;
	private final BluetoothAdapter btAdapter;
	private static final int BLUETOOTH_OK_REQUEST = 1;
	
	// Global identifier for the application's bluetooth service.
	// TODO: MAKE SURE THE ARDUINO USES THE SAME GUID OR IT MAY NOT CONNECT.
	private static final UUID BT_GUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		// "Well known" UUID^^^
		//UUID.fromString("34286088-10f5-45bd-98c8-31bd8d6c7351");
	
	private final AlertDialog.Builder dialogBuilder;
	private final AlertDialog.Builder dialogBuilderError;
	
    public BlueToothHandler(Activity activity) {
        this.a = activity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        onDeviceDisconnect();
        
        // Check if BlueTooth is supported. If not, insult the user and their device.
        if (btAdapter == null) {
        	supported = false;
        	checkSupported();
        	dialogBuilder = null;
        	dialogBuilderError = null;
        	arrayAdapter = null;
        	return;
        }
        supported = true;
        
        // Initialise final variables.
        arrayAdapter = new ArrayAdapter<String>(a, android.R.layout.select_dialog_singlechoice);
        dialogBuilder = new AlertDialog.Builder(a);
        
        // No-device dialog can be initialised here since it is basically static.
        dialogBuilderError = new AlertDialog.Builder(a);
        dialogBuilderError.setTitle("BlueTooth query");
 		TextView textView = new TextView(a);
 		textView.setText("\nNo devices discovered.\nPlease externally pair this device from "
 				+ "Settings to the Arduino's BlueTooth module.\n\n");
 		textView.setGravity(Gravity.CENTER_HORIZONTAL);
 		dialogBuilderError
 			.setView(textView)
 			.setCancelable(false)
     		.setPositiveButton("O.K, I will do that sir", null);
 		dialogBuilderError.create();
        
        tryFindDevice();
    }
    
    public void tryFindDevice() {
    	if (!checkSupported()) { return; }
    	
    	// Check if Bluetooth is enabled on the device.
        if (!btAdapter.isEnabled()) {
        	// This intent kindly promopts user to enable BlueTooth on their potato.
        	Intent btEnableRequest = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
       
        	// Actually start the prompt
        	a.startActivityForResult(btEnableRequest, BLUETOOTH_OK_REQUEST);
        	log("BlueTooth is being enabled...");
        }
        else {
        	log("BlueTooth: O.K");
        }
        
        // Query the pre-discovered/bonded devices
        arrayAdapter.clear();
        Set<BluetoothDevice> devicesPaired = btAdapter.getBondedDevices();
    	for (BluetoothDevice d : devicesPaired) {
    		arrayAdapter.add(String.format("%s\n%s", d.getName(), d.getAddress()));
    	}
    	
    	// Set up builder
        dialogBuilder.setTitle("BlueTooth query");
    	int checkedItem = 0;
    	// Initialise radio-button list	
    	// Successful dialog.
		dialogBuilder.setSingleChoiceItems(arrayAdapter, checkedItem,
    		new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					log("Item was checked: @ " + which);
				}
			})
    		// Show positive button
    		.setPositiveButton("O.K", new DialogInterface.OnClickListener() {
	    		@Override
	    		public void onClick (DialogInterface dialog, int which) {
	    			getDevice(which);
	    			connectDevice();
	    		}
	    	})
    		// and negative
    		.setCancelable(true)
	    	.setNegativeButton("Cancel", null);
		
		dialogBuilder.create();
		
		if (devicesPaired.size() > 0) {
			dialogBuilder.show();
		}
		else {
			dialogBuilderError.show();
		}
    }
    
    // Initiates an insulting toast if not supported.
    private boolean checkSupported() {
    	if (!supported) {
    		log("BlueTooth functionality is not supported on this old potato. Please consider closing this now-useless application, "
    				+ "and buying a new mobile-device.");
    		return false;
    	}
    	return true;
    }
    
    private void getDevice(int idx) {
    	// Retrieve MAC address.
    	++idx; // Not sure why i needed this.
    	System.out.println("WHICH=" + idx);
    	String info = arrayAdapter.getItem(idx);
    	// MAC address is last 17 chars in name
    	String mac = info.substring(info.length() - 17);
    	log("Attempting to connect @ MAC address: " + mac);
    	btDevice = btAdapter.getRemoteDevice(mac);
    }
    
    private boolean sockcreate_standard () {
    	try {
			// The actual module is Bluetooth 4.0 but i decided to include both just  in case
			if (secureSocket) {
				// Secure connections are supported on Bluetooth 2.1 and above.
	    		btSocket = btDevice.createRfcommSocketToServiceRecord(BT_GUID);
			}
			else {
				// Insecure socket for bluetooth modules below version 2.1.
	    		btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(BT_GUID);
			}
    	} catch (Exception e) {
    		String i = "";
    		if (secureSocket) {
    			i = " Please try running in Insecure mode.";
    		}
    		handleExceptionThreaded(e, "Error creating Bluetooth socket." + i);
    		return false;
		}
    	return true;
    }
    
    private boolean sockcreate_dodgy () {
    	try {
	    	// To try fix the 'out going connection request' issue, we can
			// try use the hidden createRfcommSocket method.
			Method method;
			Class<?>[] paramTypes = new Class[] { Integer.TYPE };
			
			// The actual module is Bluetooth 4.0 but i decided to include both just  in case
			if (secureSocket) {
				// Retrieve the hidden symbol from BluetoothDevice
				method = btDevice.getClass().getMethod("createRfcommSocket", paramTypes);
			}
			else {
				// Insecure socket for bluetooth modules below version 2.1.
				method = btDevice.getClass().getMethod("createInsecureRfcommSocket", paramTypes);
			}
			Object[] params = new Object[] { Integer.valueOf(1) };
			btSocket = (BluetoothSocket)method.invoke(btDevice, params);
		} catch (Exception e) {
    		String i = "";
    		if (secureSocket) {
    			i = " Please try running in Insecure mode.";
    		}
    		handleExceptionThreaded(e, "Error creating Bluetooth socket." + i);
    		return false;
		}
    	return true;
    }
    
    // Initiate connection
    private void connectDevice () {
    	if (btDevice == null) {
    		log("Bluetooth device null. Please connect ffs");
    		onDeviceDisconnect();
    		return;
    	}
    	
    	// Prevent blocking the main thread
    	Runnable runnable = new Runnable() {
    		@Override
			public void run() {
		    	String szMode = secureSocket ? " (Secure mode)" : " (Insecure mode)";
		    	if (sockcreate_standard()) {
		    		logThreaded("Socket successfully created through seperate thread ." + szMode);
		    	} else {
		    		if (sockcreate_dodgy()) {
		    			logThreaded("Standard socket failed, fallback socket created SUCCESS.");
		    		}
		    		else {
		    			logThreaded("Standard & Fallback socket failed to create.");
		    			setConnectionThreaded(false);
			    		return;
		    		}
		    	}
		    	
		    	// Try initiate outgoing connection request.
		    	try {
		    		try { btAdapter.cancelDiscovery(); } 
		    		catch (Exception e) { handleExceptionThreaded(e, "Error cancelling discovery mode."); }
		    		btSocket.connect();
		    	} catch (Exception e) {
		    		handleExceptionThreaded(e, "Error initiating outgoing connection request. Attempting fallback....");
		    		
		    		// Try create fallback socket and connect to that.
		    		if (sockcreate_dodgy()) {
		    			logThreaded("Fallback socket created successfully.");
		    			
		    			// Try connect
		    			try {
		    				try { btAdapter.cancelDiscovery(); } 
				    		catch (Exception e1) { handleExceptionThreaded(e1, "Error cancelling discovery mode."); }
				    		btSocket.connect();
		    			} catch (Exception e2) {
		    				handleExceptionThreaded(e2, "Error initiating outgoing connection request with fallback. Aborting...");
			    			setConnectionThreaded(false);
				    		return;
		    			}
		    		}
		    		else {
		    			logThreaded("Fallback socket failed to create.");
		    			setConnectionThreaded(false);
			    		return;
		    		}
		    	}
		    	logThreaded("Successfully connected." + szMode);
		    	
		    	// Retrieve I/O streams
		    	try {
		    		oStream = btSocket.getOutputStream();
		    	}
		    	catch (Exception e) {
		    		handleExceptionThreaded(e, "Error fetching I/O streams.");
		    		setConnectionThreaded(false);
		    		return;
		    	}
		    	logThreaded ("I/O streams successfully fetched. Data transmission is now enabled.");
		    	setConnectionThreaded(true);
			}
    	};
    	connectionThread = new Thread(runnable);
    	connectionThread.start();
    }
    
    public void sendPacket (byte[] buffer) {
    	// Insult user again if they somehow try send a packet
    	if (!checkSupported()) {
    		return;
    	}
    	if (!connected) {
    		System.err.println("Attemped to write whilst being disconnected.");
    		log ("Cannot write buffer to null device. Connect to BlueTooth device.");
    		return;
    	}
    	if (oStream == null) {
    		System.err.println("Attemped to write to null oStream.");
    		log ("Attemped to write to null oStream.");
    		return;
    	}
    	
    	// Try actually send the packet
    	try {
			oStream.write(buffer);
		} catch (IOException e) {
			handleException(e, "ERROR writing to oStream...");
			return;
		}
    	log("Successfully wrote to oStream....");
    }
    
    private void onDeviceConnect() {
    	connected = true;
    	System.out.println("onDeviceConnect()");
    	((MainActivity)a).setBtStatusDisplay(true);
    }
    
    private void onDeviceDisconnect () {
    	connected = false;
    	System.out.println("onDeviceDisconnect()");
    	((MainActivity)a).setBtStatusDisplay(false);
    }
    
    // Free resources, close socket connection.
    public void dispose () {
    	if (btSocket == null) {
    		return;
    	}
    	try {
    		btSocket.close();
		} catch (IOException e) {
			handleException(e, "Error freeing btSocket resources.");
		}
    	
    	if (connectionThread.isAlive()) {
    		try { connectionThread.join(); } 
    		catch (InterruptedException e) { e.printStackTrace(); }
    	}
    }
    
    private void handleException (Exception e, String s) {
    	e.printStackTrace();
		log(s + ", \n [Verbose:]" + e.getMessage());
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode != BLUETOOTH_OK_REQUEST) { 
    		return; 
    	}
    	switch (resultCode) {
    		// Successful request
	    	case (Activity.RESULT_OK): {
	    		log("BlueTooth successfully Enabled.");
	    	} break;
	    	
	    	// Failed request
	    	case (Activity.RESULT_CANCELED): {
	    		log("There was an error in the attempt to enable Bluetooth.");
	    	} break;
	    	
	    	// Unknown
	    	default: {
	    		log("An unexpected error occurred while attempting to enable Bluetooth.");
	    	} break;
    	}
    }
    
    // Wrapper for Toast.makeText(). Really keeps shit alot shorter.
    @Override
    public void log(String txt) {
    	Toast.makeText(a.getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
    }
    
    // To be called from alternativ threads.
    public void logThreaded (final String txt) {
    	a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				log(txt);
			}
    	});
    }
    
    private void handleExceptionThreaded (final Exception e, final String s) {
    	a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				e.printStackTrace();
				log(s + ", \n [Verbose:]" + e.getMessage());
			}
    	});
    }
    
    private void setConnectionThreaded(final boolean b) {
    	a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (b) {
					onDeviceConnect();
				}
				else {
					onDeviceDisconnect();
				}
			}
    	});
    }
}
