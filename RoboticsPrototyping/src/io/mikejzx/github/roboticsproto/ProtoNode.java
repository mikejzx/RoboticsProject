package io.mikejzx.github.roboticsproto;

public class ProtoNode {
	// Short is 16-bit signed integer. This is ideal for transmitting through BlueTooth with Few bytes.
	// Range is From -32,768 to 32,768 (I know, i wish Java supported unsigned shorts too. (apart from char))
	// But should be fine as the maximum value would be less that 10,000 most likely.
	public short x = 0;
	public short y = 0;
	
	// Ctor for simplicity's sake.
	public ProtoNode(short x, short y) {
		this.x = x;
		this.y = y;
	}
}
