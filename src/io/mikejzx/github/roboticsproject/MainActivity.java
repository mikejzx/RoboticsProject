package io.mikejzx.github.roboticsproject;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
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

    // Called on application load
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BluetoothHandler(this);

        nodes.clear();
        nodes.add(new Node(150, 600));
        nodes.add(new Node(200, 400));
        nodes.add(new Node(400, 200));
        nodes.add(new Node(450, 500));
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

        // Contains positions of all nodes in binary form.
        // TODO: CONVERT VECTOR2 CLASS TO STORE INTEGERS. FLOATING-POINT VALUES WILL NOT WORK
        byte[] buffer = new byte[0];


        bt.writeData(buffer);
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

        float posX = (posA.getX() + posB.getX()) / 2.0f;
        float posY = (posA.getY() + posB.getY()) / 2.0f;

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
