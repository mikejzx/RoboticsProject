package io.mikejzx.github.roboticsproject;

public class Vector2 {
    public short x = 0;
    public short y = 0;

    public static final Vector2 zero = new Vector2((short)0, (short)0);
    public static final Vector2 one = new Vector2((short)1, (short)1);

    public short getXClamped(short bound) { return (short)Utils.clamp(x, 0, bound); }
    public short getYClamped(short bound) { return (short)Utils.clamp(y, 0, bound); }

    public Vector2() { x = (short)0; y = (short)0; }
    public Vector2(short X, short Y) { x = X; y = Y; }
    public Vector2(Vector2 copy) { x = copy.x; y = copy.y; }

    public boolean equals (Vector2 other) { 
    	return (this.x == other.x) && (this.y == other.y); 
    }

    public Vector2 add (Vector2 other) {
    	return new Vector2(
			(short)(x + other.x), 
			(short)(y + other.y));
    }

    public void addTo (Vector2 other) {
        this.x += other.x;
        this.y += other.y;
    }

    public Vector2 sub (Vector2 other) {
        return new Vector2(
    		(short)(x - other.x), 
    		(short)(y - other.y));
    }

    public Vector2 mul (float multiplier) {
        return new Vector2((short)Math.round(x * multiplier), 
        	(short)Math.round(y * multiplier));
    }

    public float distance (Vector2 other) {
        float a = other.x - this.x;
        float b = other.y - this.y;
        return (float)Math.sqrt(a * a + b * b);
    }
}
