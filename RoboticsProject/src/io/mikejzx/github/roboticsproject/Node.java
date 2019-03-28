package io.mikejzx.github.roboticsproject;

public class Node implements ICompareCached<Float> {

    public Vector2 position = new Vector2();

    public boolean selected = false;

    public Node(short x, short y) {
        setPosition(x, y);
    }

    public void setPosition (short newX, short newY) {
        position.x = newX;
        position.y = newY;
    }

    
    private Float comparer = 0.0f;
	@Override
	public void setComparer(Float f) { comparer = f; }
	@Override
	public Float getComparer() { return comparer; }
}
