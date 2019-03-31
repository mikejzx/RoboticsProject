package io.mikejzx.github.roboticsproject;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BlueToothHandler implements IToastable {

	private final Activity a;
	private final BluetoothAdapter btAdapter;
	private static final int BLUETOOTH_OK_REQUEST = 1;
	
	private final AlertDialog.Builder dialogBuilder;
	
    public BlueToothHandler(Activity activity) {
        this.a = activity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // Check if BlueTooth is supported. If not, insult the user and their device.
        if (btAdapter == null) {
        	log("BlueTooth functionality is not supported on this old potato.");
        	dialogBuilder = null;
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
        
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(a, android.R.layout.select_dialog_singlechoice);
        
        // Query the pre-discovered/bonded devices
        Set<BluetoothDevice> devicesPaired = btAdapter.getBondedDevices();
    	for (BluetoothDevice d : devicesPaired) {
    		arrayAdapter.add(String.format("%s\n%s", d.getName(), d.getAddress()));
    	}
    	
    	// Set up builder
    	dialogBuilder = new AlertDialog.Builder(a)
    		.setTitle("BlueTooth query");
    	int checkedItem = 1;
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
		    			log ("O.K was clicked.");
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
	    		.setPositiveButton("O.K, I will do that sir", new DialogInterface.OnClickListener() {
		    		@Override
		    		public void onClick (DialogInterface dialog, int which) {
		    			log ("O.K was clicked.");
		    		}
		    	});
    	}
    	dialogBuilder.create().show();
    	
    	
    	// Initialise the dialog to display the devices (insanely messy, i know...)
        /*dialogBuilder = new AlertDialog.Builder(a)
    		.setTitle("BlueTooth device Select")
        	.setMessage("Not initialised...")
        	.setCancelable(true)
        	.setIcon(android.R.drawable.ic_dialog_dialer)
        	// Lambda's would be nice ffs...
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
        	})
        	.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String szName = arrayAdapter.getItem(which);
					AlertDialog.Builder inner = new AlertDialog.Builder(a)
						.setMessage(szName)
						.setTitle("Selected Device is")
						.setPositiveButton("O.K", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
					inner.show();
				}
			});
        dialogBuilder.show();*/
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
