package ict.ac.humanmotion.uapplication;

public class ImuStatus {
	boolean isLogging;
	String logFileName = new String();
	boolean measurementStarted = false;
	
	public ImuStatus() {
		logFileName = "";
		isLogging = false;
	}
}