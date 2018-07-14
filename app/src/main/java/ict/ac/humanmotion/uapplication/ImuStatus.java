package ict.ac.humanmotion.uapplication;

public class ImuStatus {
	boolean isLogging;
	String logFileName;
	boolean measurementStarted = false;
	
	ImuStatus() {
		logFileName = "";
		isLogging = false;
	}
}