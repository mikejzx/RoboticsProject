package io.mikejzx.github.roboticsproject;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    /*
        Developed by Michael
        mikejzx03@gmail.com
        mikejzx.github.io

            15.03.2019
        - For school robotics project. -
    */

    public static List<Node> nodes = new ArrayList<Node>();
    public static Node selectedNode = null;
    public static int selectedNodeIndex = -1;

    public static BluetoothHandler bt;
    
    private static AlertDialog dialogAlert;
    private static Builder dialogBuilder;

    // Called on application load
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialogBuilder = new AlertDialog.Builder(this)
        	.setTitle("Hex data")
        	.setMessage("Not initialised...")
        	.setPositiveButton("O.K", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
        	.setCancelable(false)
        	.setIcon(android.R.drawable.ic_dialog_info);
        dialogAlert = dialogBuilder.create();
        
        bt = new BluetoothHandler(this);

        nodes.clear();
        nodes.add(new Node((short)150, (short)600));
        nodes.add(new Node((short)200, (short)400));
        nodes.add(new Node((short)400, (short)200));
        nodes.add(new Node((short)450, (short)500));
        NodeView.setSelectedNode(nodes.get(0), 0);
    }

    // Called on application RESUME, (i.e: When the application is re-opened after minimisation)
    @Override
    protected void onResume() {
        super.onResume();
        bt.onResume();
    }

    public void btn_upload(View view) {
        // TODO: implement bluetooth functions...
        byte[] buffer = serialiseNodeData();
        String dataString = getSerialisedDataHexString(buffer);
        bt.writeData(buffer);
        System.out.println("Sent packet to bluetooth device containing: \n" + dataString);
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()) {
	    	case (R.id.action_debuginfo): {
	    		System.out.println("Menu clicked");
	    		dialogBuilder.setMessage("Serialised hex data for signed 16-bit vectors: \n\n" +  serialiseNodeDataString());
	    		dialogAlert = dialogBuilder.create();
	            dialogAlert.show();
	    	} return true;
    	
    		default: {
    			return super.onOptionsItemSelected(item);
    		}
    	}
    }
    
    private static byte[] serialiseNodeData () {
    	// Serialised Vector2's into binary form:
    	// Vector2's will be 4-bytes long. (Because both X & Y are 16-bit ints)
    	int nodeCount = nodes.size();
        int packetSize = nodeCount * 4;
        byte[] serialisedData = new byte[packetSize];
        
        // First byte in packet represents the number of nodes. 
        // (8-bit is fine because node count is clamped from 0 to 100)
        for (int i = 0; i < nodeCount; i++) {
        	Vector2 node = nodes.get(i).position;
        	byte[] nodeBytes = {
        		(byte)(node.x & 0xFF), (byte)((node.x >> 8) & 0xFF),
        		(byte)(node.y & 0xFF), (byte)((node.y >> 8) & 0xFF)
        	};
        	for (int b = 0; b < 4; b++) {
        		serialisedData[(i * 4) + b] = nodeBytes[b];
        	}
        }
        return serialisedData;
    }
    
    private static String serialiseNodeDataString() {
        byte[] serialisedData = serialiseNodeData();
        return getSerialisedDataHexString(serialisedData);
    }
    
    private static String getSerialisedDataHexString (byte[] buffer) {
    	StringBuilder hexData = new StringBuilder();
        for (int i = 0; i < buffer.length; i++) {
        	String add = " ";
        	if ((i + 1) % 2 == 0) { add = ", "; }
        	if ((i + 1) % 4 == 0) { add = "\n"; }
        	hexData.append(String.format("0x%02X%s", buffer[i] & 0xFF, add));
        }
        return hexData.toString();
    }
    
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflator =  getMenuInflater();
    	inflator.inflate(R.menu.menu_main, menu);
		return true;
    }
    
    public void btn_ins(View view) {
        // Clamp at 100 nodes
        if (nodes.size() > 99) { return; }

        Vector2 posA = nodes.get(selectedNodeIndex).position;
        Vector2 posB;
        int nodeCount = nodes.size();
        if (selectedNodeIndex < nodeCount - 1 && selectedNodeIndex > 0) {
            // Average between selected and next
            posB = nodes.get(selectedNodeIndex + 1).position;
            selectedNodeIndex++;
        }
        else {
            if (selectedNodeIndex == 0) {
                posB = nodes.get(nodeCount - 1).position;
                selectedNodeIndex = 0;
            }
            else {
                posB = nodes.get(0).position;
                selectedNodeIndex = nodeCount;
            }
        }

        short posX = (short)Math.round((posA.x + posB.x) / 2.0f);
        short posY = (short)Math.round((posA.y + posB.y) / 2.0f);

        nodes.add(selectedNodeIndex, new Node(posX, posY));
        // Set selected to the inserted
        NodeView.setSelectedNode(nodes.get(selectedNodeIndex), selectedNodeIndex);
    }

    public void btn_rm(View view) {
        if (nodes.size() < 3) { return; }

        nodes.remove(selectedNodeIndex);
        if (selectedNodeIndex > 0) {
            selectedNodeIndex--;
        }
        else {
            selectedNodeIndex = 0;
        }
        NodeView.setSelectedNode(nodes.get(selectedNodeIndex), selectedNodeIndex);
    }
}
