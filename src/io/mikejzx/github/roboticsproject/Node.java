package io.mikejzx.github.roboticsproject;

public class Node {

    public Vector2 position = new Vector2();

    public boolean selected = false;

    public Node(float x, float y) {
        setPosition(x, y);
    }

    public void setPosition (float newX, float newY) {
        position.setX(newX);
        position.setY(newY);
    }
}
