package io.mikejzx.github.roboticsproject;

public class NodeIndexed implements ICompareCached<Integer> {

	public Node node;
	public int comparer = 0;
	
	public NodeIndexed (Node node, int comp) {
		this.node = node;
		this.comparer = comp;
	}
	
	@Override
	public void setComparer(Integer f) { comparer = f; }

	@Override
	public Integer getComparer() { return comparer; }
}
