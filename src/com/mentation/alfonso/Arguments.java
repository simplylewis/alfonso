package com.mentation.alfonso;

import java.io.PrintStream;

class Arguments {
	public String getBlueInstanceId() {
		return _blueInstanceId;
	}

	public String getGreenInstanceId() {
		return _greenInstanceId;
	}

	public String getBlueElbId() {
		return _blueElbId;
	}

	public String getGreenElbId() {
		return _greenElbId;
	}

	public String getLiveElbId() {
		return _liveElbId;
	}

	private String _blueInstanceId = null;
	private String _greenInstanceId = null;
	
	private String _blueElbId = null;
	private String _greenElbId = null;
	private String _liveElbId = null;
	
	boolean parseArgs(String[] args) {
		for (String arg : args) {
			String[] argParts = arg.split("=");
			
			switch (argParts[0].toLowerCase()) {
			case "blueelbid" :
				_blueElbId = argParts[1];
				break;
				
			case "greenelbid" :
				_greenElbId = argParts[1];
				break;
				
			case "liveelbid" :
				_liveElbId = argParts[1];
				break;
				
			case "blueinstanceid" :
				_blueInstanceId = argParts[1];
				break;
				
			case "greeninstanceid" :
				_greenInstanceId = argParts[1];
				break;
				
			default: 
				System.err.println("Unknown argument: " + arg);
				printInstructions(System.err);				
				return false;
			}
		}
		
		return  isValid();
	}

	boolean isValid() {
		return _blueElbId != null && _greenElbId != null && _liveElbId != null && _blueInstanceId != null && _greenInstanceId != null;
	}
	
	private void printInstructions(PrintStream printstream) {		
		printstream.println("Required arguments are as follows:\n\tblueelbid=<name> greenelbid=<name> liveelbid=<name> blueinstanceid=<instance-id> greeninstanceid=<instance-id>");
		printstream.println("Where <name> is the elb name specified when created");
		printstream.println("Where <instance-id> is the value assigned by AWS when the instance is cerated");
	}
}