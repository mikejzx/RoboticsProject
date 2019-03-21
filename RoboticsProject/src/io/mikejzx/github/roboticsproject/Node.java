package io.mikejzx.github.roboticsproject;

public class Node {

    public Vector2 position = new Vector2();

    public boolean selected = false;

    public Node(short x, short y) {
        setPosition(x, y);
    }

    public void setPosition (short newX, short newY) {
        position.x = newX;
        position.y = newY;
    }
}
