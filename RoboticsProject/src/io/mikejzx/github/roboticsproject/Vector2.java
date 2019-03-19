package io.mikejzx.github.roboticsproject;

public class Vector2 {
    private float x = 0.0f;
    private float y = 0.0f;

    public static final Vector2 zero = new Vector2(0.0f, 0.0f);
    public static final Vector2 one = new Vector2(1.0f, 1.0f);

    public float getX() { return x; }
    public float getY() { return y; }
    public float getXClamped(float bound) { return Utils.clamp(x, 0, bound); }
    public float getYClamped(float bound) { return Utils.clamp(y, 0, bound); }
    public void setX(float newX) { x = newX;  }
    public void setY(float newY) { y = newY; }

    public Vector2() { setX(0); setY(0); }
    public Vector2(float X, float Y) { setX(X); setY(Y); }
    public Vector2(Vector2 copy) { setX(copy.x); setY(copy.y); }

    public boolean equals (Vector2 other) {
        return (this.x == other.x) && (this.y == other.y);
    }

    public Vector2 add (Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public void addTo (Vector2 other) {
        this.x += other.x;
        this.y += other.y;
    }

    public Vector2 sub (Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public Vector2 mul (float multiplier) {
        return new Vector2(x * multiplier, y * multiplier);
    }

    public float distance (Vector2 other) {
        float a = other.x - this.x;
        float b = other.y - this.y;
        return (float)Math.sqrt(a * a + b * b);
    }
}
