package io.mikejzx.github.roboticsproject;

/*
	Forces a class to implement a toast-logging wrapper method.
	Honestly  don't know why i bothered writing an interface for this xD
*/
public interface IToastable {
	public void log(String text);
}
