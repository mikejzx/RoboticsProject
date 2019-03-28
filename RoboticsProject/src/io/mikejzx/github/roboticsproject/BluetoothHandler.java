package io.mikejzx.github.roboticsproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.app.Activity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/*
https://wingoodharry.wordpress.com/2014/04/15/android-sendreceive-data-with-arduino-using-bluetooth-part-2/
*/

// STILL NEED TO DO: CREATE A BLUETOOTH DEVICE SELECTION ACTIVITY.

public class BlueToothHandler {

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private Activity a;

    // SPP UUID Service
    private static final UUID BT_MODULE_UUID
            = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String macAddress;

    // Used for writing to the bt device. Input stream
    // not included since nothing is being read
    private final OutputStream streamOut;


    public BlueToothHandler(Activity activity) {
        this.a = activity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        checkBtState ();
        
        onResume();

        OutputStream tmpOut = null;
        // Try create I/O streams
        try {
            tmpOut = btSocket.getOutputStream();
        }
        catch (IOException e) {
            System.err.println("ERROR getting input stream of bt socket.");
        }
        streamOut = tmpOut;
    }

    public void writeData(byte[] buffer) {
        try {
            streamOut.write(buffer);
        }
        catch (IOException e) {
            System.err.println("ERROR writing to I/O stream : " + e.getMessage());
            a.finish();
        }
    }

    public void onResume() {
    	checkBtState ();
        Intent intent = a.getIntent();
        macAddress = intent.getStringExtra(BlueToothActivity.EXTRA_DEVICE_ADDRESS);
        if (macAddress == null) { System.err.println("Null BT address."); return; }
        BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);

        // Create socket
        try { btSocket = createBluetoothSocket(device); }
        catch(IOException e) { System.err.println("ERROR creating bt socket: " + e.getMessage()); }

        // Establish connection
        try { btSocket.connect(); }
        catch(IOException e) {
            System.err.println("ERROR establishing bt socket connection: " + e.getMessage());
            closeSocket();
        }

        // Test connection...
        writeData(new String("x").getBytes());
    }

    public void onPause () {
        closeSocket();
    }

    private void closeSocket () {
        try {
            btSocket.close();
        }
        catch (IOException e) {
            System.err.println("ERROR closing bt socket..." + e.getMessage());
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    private void checkBtState () {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                System.out.print("Bluetooth disabled, prompting...");
                Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                a.startActivityForResult(btEnable, 1);
            }
            else {
                System.out.print("Bluetooth enabled.");
            }
        }
        else {
            System.err.println("BLUETOOTH NOT SUPPORTED...");
        }
    }
}
