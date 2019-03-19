package io.mikejzx.github.roboticsproto;

import java.util.ArrayList;
import java.util.List;

import io.mikejzx.github.roboticsproto.ProtoNode;

public class ProtoSerialiser {

	public static void main(String[] args) {
		System.out.println("Running ProtoSerialiser.java");
		
		List<ProtoNode> nodes = new ArrayList<ProtoNode>();
		
		nodes.add(new ProtoNode((short)150, (short)600));
        nodes.add(new ProtoNode((short)200, (short)400));
        nodes.add(new ProtoNode((short)400, (short)200));
        //nodes.add(new ProtoNode((short)450, (short)500));
        
        // 16-bit integer contains 2 bytes.
        /* Short conversion example:
        short x = 0;
        byte[] thisInt16 = {
        	(byte)(x & 0xFF), 
        	(byte)((x >> 8) & 0xFF)
        };*/
        
        // Notes: Each Vector2 is 4 bytes long.
        // Byte 0 = x byte part LOWORD
        // Byte 1 = x byte part HIWORD
        // Byte 2 = y byte part LOWORD
        // Byte 3 = y byte part HIWORD
        int nodeLength = nodes.size();
        int packetSize = nodeLength * 4;
        byte[] serialisedData = new byte[packetSize];
        for (int i = 0; i < nodeLength; i++) {
        	ProtoNode node = nodes.get(i);
        	byte[] nodeBytes = {
    			(byte)(node.x & 0xFF), (byte)((node.x >> 8) & 0xFF),
    			(byte)(node.y & 0xFF), (byte)((node.y >> 8) & 0xFF)
        	};
        	for (int b = 0; b < 4; b++) {
        		serialisedData[(i * 4) + b] = nodeBytes[b];
        	}
        }
        
        StringBuilder hexData = new StringBuilder();
        for (int i = 0; i < packetSize; i++) {
        	String add = " ";
        	if ((i + 1) % 2 == 0) { add = ", "; }
        	if ((i + 1) % 4 == 0) { add = "\n"; }
        	hexData.append(String.format("0x%02X%s", serialisedData[i] & 0xFF, add));
        }
        System.out.println("Launching prototype application with packet: \n" + hexData.toString());
	}
}
