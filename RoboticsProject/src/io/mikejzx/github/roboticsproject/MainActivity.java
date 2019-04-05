package io.mikejzx.github.roboticsproject;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements IToastable {

    /*
        Developed by Michael
        mikejzx03@gmail.com
        https://mikejzx.github.io/

            15.03.2019
        - For school robotics project. -
    */

    public static List<Node> nodes = new ArrayList<Node>();
    public static Node selectedNode = null;
    public static int selectedNodeIndex = -1;

    public static BlueToothHandler bt;
    
    private static AlertDialog dialogAlert;
    private static Builder dialogBuilder;

    private static Menu menuMain;
    
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

        nodes.clear();
        nodes.add(new Node((short)150, (short)600));
        nodes.add(new Node((short)200, (short)400));
        nodes.add(new Node((short)400, (short)200));
        nodes.add(new Node((short)450, (short)500));
        NodeView.setSelectedNode(nodes.get(0), 0);
        
        bt = new BlueToothHandler(this);
        
        Button btnAttach = (Button)findViewById(R.id.btn_attach);
        btnAttach.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bt.tryFindDevice();
			}
        });
    }

    public void btn_upload(View view) {
        byte[] buffer = serialiseNodeData();
        String dataString = getSerialisedDataHexString(buffer);
        bt.sendPacket(buffer);
        System.out.println("Sent packet to bluetooth device containing: \n" + dataString);
    }
    
    public void setBtStatusDisplay(boolean active) {
    	// Change bluetooth status string.
    	String szLabel = active 
    			? getString(R.string.lab_attach_status_true) 
    			: getString(R.string.lab_attach_status_false);
    	TextView label = (TextView)findViewById(R.id.lab_btstatus);
    	label.setText(szLabel);
    	
    	// Enable/disable upload button.
    	Button btn = (Button)findViewById(R.id.btn_upload);
    	btn.setEnabled(active);
    }
    
    public void setBtSecureMode(boolean active) {
    	bt.secureSocket = active;
    	
    	if (menuMain == null) { return; }
    	
    	// Change security status label.
    	String szLabel = active
    			? getString(R.string.action_securemode_true)
    			: getString(R.string.action_securemode_false);
    	menuMain.findItem(R.id.action_securemode).setTitle(szLabel);
    	System.out.println("- Set bluetooth security menu title");
    }
    
    @Override
    protected void onDestroy() {
    	bt.dispose();
    	super.onDestroy();
    }
    
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	bt.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()) {
	    	case (R.id.action_debuginfo): {
	    		System.out.println("Menu clicked");
	    		StringBuilder byteCount = new StringBuilder(); // StringBuilder will be passed 'by reference'
	    		String hexData = serialiseNodeDataString(byteCount);
	    		dialogBuilder.setMessage("Serialised hex data for signed 32-bit vectors (" + byteCount + ")\n\n" +  hexData);
	    		dialogAlert = dialogBuilder.create();
	            dialogAlert.show();
	    	} return true;
	    	
	    	case (R.id.action_securemode): {
	    		setBtSecureMode(bt.secureSocket ^ true);
	    	} return true;
    	
    		default: {
    			return super.onOptionsItemSelected(item);
    		}
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
    	MenuInflater inflator =  getMenuInflater();
    	inflator.inflate(R.menu.menu_main, menu);
    	menuMain = menu;
		return true;
    }
    
    private static byte[] serialiseNodeData () {
    	// Serialised Vector2's into binary form:
    	// Vector2's will be 4-bytes long. (Because both X & Y are 16-bit ints)
    	int nodeCount = nodes.size();
    	final int extraBytes = 2;// +2 b/c to store size, and scale.
        int packetSize = (nodeCount * 4) + extraBytes; 
        byte[] serialisedData = new byte[packetSize];
        
        // First byte in packet represents the number of nodes. 
        // Second represents scale in (undetermined unit [TODO]).
        // (8-bit int is fine because node count is clamped from 0 to 100)
        serialisedData[0] = (byte)nodeCount;
        serialisedData[1] = NodeView.nodeScale;
        
        // TODO: Possible try merging the two control bytes into a single byte.
        // by using LOWORD to store dataA, and storing dataB in the HIWORD.
        
        for (int i = 0; i < nodeCount; i++) {
        	Vector2 node = nodes.get(i).position;
        	byte[] nodeBytes = {
        		(byte)(node.x & 0xFF), (byte)((node.x >> 8) & 0xFF),
        		(byte)(node.y & 0xFF), (byte)((node.y >> 8) & 0xFF)
        	};
        	for (int b = 0; b < 4; b++) {
        		serialisedData[(i * 4) + b + extraBytes] = nodeBytes[b];
        	}
        }
        return serialisedData;
    }
    
    private static String serialiseNodeDataString(StringBuilder byteCount) {
        byte[] serialisedData = serialiseNodeData();
        byteCount.setLength(0);
        int countBytes = serialisedData.length;
        float countKibibytes = Math.round(countBytes / 1024.0f * 100.0f) / 100.0f;
        String szCountKibibytes = String.format(java.util.Locale.UK, "%.2f", countKibibytes);
        byteCount.append(String.format("%d bytes, ~%s KiB", countBytes, szCountKibibytes));
        return getSerialisedDataHexString(serialisedData);
    }
    
    private static String getSerialisedDataHexString (byte[] buffer) {
    	StringBuilder hexData = new StringBuilder();
    	hexData.append(String.format("Byte0 (size)  = 0x%02X%s", buffer[0] & 0xFF, "\n"));
    	hexData.append(String.format("Byte1 (scale) = 0x%02X%s", buffer[1] & 0xFF, "\n"));
        for (int i = 2; i < buffer.length; i++) {
        	String add = " ";
        	if ((i - 1) % 2 == 0) { add = ", "; }
        	if ((i - 1) % 4 == 0) { add = "\n"; }
        	hexData.append(String.format("0x%02X%s", buffer[i] & 0xFF, add));
        }
        return hexData.toString();
    }
    
    public void btn_ins(View view) {
        // Clamp at 100 nodes for stability's sake, (overall, 255 can be fit in the packet.)
        if (nodes.size() > 99) { return; }

        Vector2 posA = nodes.get(selectedNodeIndex).position;
        Vector2 posB;
        int nodeCount = nodes.size();
        
        System.out.println("sel: " + selectedNodeIndex + " count:" + nodeCount);
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

	@Override
	public void log(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}
}
