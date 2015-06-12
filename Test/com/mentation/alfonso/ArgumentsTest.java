package com.mentation.alfonso;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ArgumentsTest {

  @Test
  public void parseArgs() {
	  Arguments arguments = new Arguments();
	  
	  String[] args = {"blueElbId=blue", "greenElbId=green", "liveElbId=live", "blueInstanceId=isBlue", "greenInstanceId=isGreen"};
		
	  arguments.parseArgs(args);
		
	  Assert.assertTrue(arguments.isValid());
	  
	  Assert.assertEquals(arguments.getBlueElbId(), "blue");
	  Assert.assertEquals(arguments.getGreenElbId(), "green");
	  Assert.assertEquals(arguments.getLiveElbId(), "live");
	  Assert.assertEquals(arguments.getBlueInstanceId(), "isBlue");
	  Assert.assertEquals(arguments.getGreenInstanceId(), "isGreen");
  }
  
  @Test
  public void missingArgsTest() {
	  Arguments arguments = new Arguments();
	  
	  String[] missingArgs = {"greenElbId=green", "liveElbId=live", "blueInstanceId=isBlue", "greenInstanceId=isGreen"};
	  
	  arguments.parseArgs(missingArgs);
		
	  Assert.assertFalse(arguments.isValid());
  }
  
  
  @Test
  public void badArgsTest() {
	  Arguments arguments = new Arguments();
	  
	  String[] args = {"XblueElbId=blue", "greenElbId=green", "liveElbId=live", "blueInstanceId=isBlue", "greenInstanceId=isGreen"};
		
	  arguments.parseArgs(args);
		
	  Assert.assertFalse(arguments.isValid());
  }
}
