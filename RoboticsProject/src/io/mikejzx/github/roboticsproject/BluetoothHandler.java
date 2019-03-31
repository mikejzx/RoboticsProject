package io.mikejzx.github.roboticsproject;

import java.io.IOException;
import java.io.OutputStream;
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

	private BluetoothDevice btDevice;
	private BluetoothSocket btSocket;
	private OutputStream oStream;
	private final ArrayAdapter<String> arrayAdapter;
	
	private final Activity a;
	private final BluetoothAdapter btAdapter;
	private static final int BLUETOOTH_OK_REQUEST = 1;
	
	// Global identifier for the application's bluetooth service.
	// TODO: MAKE SURE THE ARDUINO USES THE SAME GUID OR IT MAY NOT CONNECT.
	private static final UUID BT_GUID = UUID.fromString("34286088-10f5-45bd-98c8-31bd8d6c7351");
	
	private final AlertDialog.Builder dialogBuilder;
	
    public BlueToothHandler(Activity activity) {
        this.a = activity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // Check if BlueTooth is supported. If not, insult the user and their device.
        if (btAdapter == null) {
        	log("BlueTooth functionality is not supported on this old potato.");
        	dialogBuilder = null;
        	arrayAdapter = null;
        	return;
        }
        
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
        
        arrayAdapter = new ArrayAdapter<String>(a, android.R.layout.select_dialog_singlechoice);
        
        // Query the pre-discovered/bonded devices
        Set<BluetoothDevice> devicesPaired = btAdapter.getBondedDevices();
    	for (BluetoothDevice d : devicesPaired) {
    		arrayAdapter.add(String.format("%s\n%s", d.getName(), d.getAddress()));
    	}
    	
    	// Set up builder
    	dialogBuilder = new AlertDialog.Builder(a)
    		.setTitle("BlueTooth query");
    	int checkedItem = 0;
    	// Initialise radio-button list	
    	// Only show devices if they actually exist
    	if (devicesPaired.size() > 0) {
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
		    	.setNegativeButton("Cancel", null);
    	}
    	else {
    		TextView textView = new TextView(a);
    		textView.setText("\nNo devices discovered.\nPlease externally pair this device from "
    				+ "Settings to the Arduino's BlueTooth module.\n\n");
    		textView.setGravity(Gravity.CENTER_HORIZONTAL);
    		dialogBuilder
    			.setView(textView)
	    		.setPositiveButton("O.K, I will do that sir", null);
    	}
    	dialogBuilder.create().show();
    }
    
    
    // TODO: SHOW HOW MANY BYTES, OR MB, OR KiB are in packet
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
    
    // Initiate connection
    private void connectDevice () {
    	if (btDevice == null) {
    		log("Bluetooth device null. Please connect ffs");
    		onDeviceDisconnect();
    		return;
    	}
    	try {
    		// Insecure since the bluetooth module is below version 2.1.
    		btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(BT_GUID);
    	} catch (Exception e) {
    		handleException(e, "Error creating Bluetooth socket.");
    		onDeviceDisconnect();
    		return;
    	}
    	
    	// Try initiate outgoing connection request.
    	try {
    		btSocket.connect();
    	} catch (Exception e) {
    		handleException(e, "Error initiating outgoing connection request.");
    		onDeviceDisconnect();
    		return;
    	}
    	log("Successfully connected.");
    	
    	// Retrieve I/O streams
    	try {
    		oStream = btSocket.getOutputStream();
    	}
    	catch (Exception e) {
    		handleException(e, "Error fetching I/O streams.");
    		onDeviceDisconnect();
    		return;
    	}
    	log ("I/O streams successfully fetched. Data transmission is now enabled.");
    	onDeviceConnect();
    }
    
    private void onDeviceConnect() {
    	System.out.println("onDeviceConnect()");
    	((MainActivity)a).setBtStatusDisplay(true);
    }
    
    private void onDeviceDisconnect () {
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
	    	}
    	}
    }
    
    // Wrapper for Toast.makeText(). Really keeps shit alot shorter.
    @Override
    public void log(String txt) {
    	Toast.makeText(a.getApplicationContext(), txt, Toast.LENGTH_LONG).show();
    }
}
