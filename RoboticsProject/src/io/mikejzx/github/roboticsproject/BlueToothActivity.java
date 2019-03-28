package io.mikejzx.github.roboticsproject;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

// Used to display Bluetooth device list.

public class BlueToothActivity extends Activity {

	private BluetoothAdapter btAdapter;
	private ArrayAdapter<String> pairedDevicesArray;
	
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	private static Builder dialogBuilder;
	private static AlertDialog dialogAlert;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btdevices);
        
        dialogBuilder =  new AlertDialog.Builder(this)
        	.setTitle("ERROR") .setMessage("Error...")
        	.setPositiveButton("O.K", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) { finish(); }
			}).setCancelable(false).setIcon(android.R.drawable.ic_dialog_alert);
        dialogAlert = dialogBuilder.create();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		checkBluetoothState();
		
		// Paired devices array adapter
		pairedDevicesArray = new ArrayAdapter<String>(this, R.layout.activity_btdevices);
		// Set up list view
		ListView listView = (ListView)findViewById(R.id.btdevices_list);
		listView.setAdapter(pairedDevicesArray);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				System.out.println("Connecting...");
				// Get MAC address (last 17 chars in view)
				String info  = ((TextView)view).getText().toString();
				String addr = info.substring(info.length() - 17);
				Intent i = new Intent(BlueToothActivity.this, MainActivity.class);
				i.putExtra(EXTRA_DEVICE_ADDRESS, addr);
				startActivity(i);
			}
		});
		
		// Get bluetooth adapter
		final Set<BluetoothDevice> pairedDevices;
		try {
			btAdapter = BluetoothAdapter.getDefaultAdapter();
			// Get paired devices
			pairedDevices = btAdapter.getBondedDevices();
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			dialogBuilder.setMessage("There was an error attempting to get the BlueTooth Devices. "
					+ "Your device may not support Bluetooth.\n\nTechnical:\n" + e.getMessage());
			dialogAlert = dialogBuilder.create();
			dialogAlert.show();
			return;
		}
		
		// Add previously paired devices to array
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice d : pairedDevices) {
				pairedDevicesArray.add(String.format("%s\n%s", d.getName(), d.getAddress()));
			}
		}
		else {
			pairedDevicesArray.add("No devices paired...");
		}
	}
	
	private void checkBluetoothState () {
		// Check if device has bluetoothm and that it's on
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null) {
			if (btAdapter.isEnabled()) {
				System.out.println("BlueTooth is ON");
			}
			else {
				// Request/prompt user to enable blueTooth.
				Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBt, 0);
			}
		}
	}
}
