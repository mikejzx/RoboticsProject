package io.mikejzx.github.roboticsproject;

/*
	This interface is used for classes that are compared based on an
	internal value that is calculated. Specific, I know...
	Honestly probably should just move all this crap into the Node class but who cares.
*/

public interface ICompareCached<T> {
	public void setComparer(T f);
	public T getComparer();
}
