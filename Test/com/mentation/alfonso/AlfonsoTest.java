/*
Copyright 2015 Lewis Foti

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.mentation.alfonso;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mentation.alfonso.aws.ElasticLoadBalancer;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteState;
import com.mentation.fsm.state.FiniteStateMachine;

public class AlfonsoTest {

	@Mocked ElasticLoadBalancer blueElb;
	@Mocked ElasticLoadBalancer greenElb;
	@Mocked ElasticLoadBalancer liveElb;

	@Test
	public void configureFsmTest() {
		
		Alfonso alfonso = new Alfonso();

		FiniteState initialState =
				alfonso.configureFsm(blueElb, "blueId", greenElb, "greenId", liveElb);

		Assert.assertEquals(initialState.getName(), "Start");

		FiniteStateMachine fsm = new FiniteStateMachine("Alfonso Test", initialState);

		fsm.start();

		postMessage(fsm, alfonso._blueIsUnhealthy, "Wait For BlueHealthy");
		postMessage(fsm, alfonso._blueIsHealthy, "Wait For GreenHealthy");	  
		postMessage(fsm, alfonso._greenIsHealthy, "Add Blue To Live");	  
		postMessage(fsm, alfonso._liveHasOne, "Blue Live");	  
		postMessage(fsm, alfonso._blueIsHealthy, "Blue Live");	  
		postMessage(fsm, alfonso._greenIsHealthy, "Blue Live");	  
		postMessage(fsm, alfonso._greenIsUnhealthy, "Green Failed");	  
		postMessage(fsm, alfonso._greenIsHealthy, "Blue Live");	  
		postMessage(fsm, alfonso._blueIsUnhealthy, "Blue Failed");	  
		postMessage(fsm, alfonso._liveHasNone, "Add Green To Live");	  
		postMessage(fsm, alfonso._liveHasOne, "Green Live");	  	  
		postMessage(fsm, alfonso._blueIsUnhealthy, "Green Live");	  
		postMessage(fsm, alfonso._greenIsUnhealthy, "Recovery");	  
		postMessage(fsm, alfonso._greenIsHealthy, "Add Green To Live");	  
		postMessage(fsm, alfonso._liveHasOne, "Green Live");	  	  
		postMessage(fsm, alfonso._blueIsHealthy, "Remove Green From Live");	  
		postMessage(fsm, alfonso._liveHasNone, "Add Blue To Live");	  
		postMessage(fsm, alfonso._liveHasOne, "Blue Live");	  
		postMessage(fsm, alfonso._greenIsUnhealthy, "Green Failed");
		postMessage(fsm, alfonso._blueIsUnhealthy, "Recovery");
		postMessage(fsm, alfonso._blueIsHealthy, "Add Blue To Live");

	}

	private void postMessage(FiniteStateMachine fsm, IMessage msg, String stateName) {
		fsm.consumeMessage(msg);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.assertEquals(fsm.getState().getName(), stateName);
	}

	@Test(dependsOnMethods = {"configureFsmTest"})
	public void runStateMachineTest() {
		new Expectations() {{ 
			blueElb.isInstanceHealthy(); result = true;		
			greenElb.isInstanceHealthy(); result = true;	
			blueElb.getName(); result = "BlueElb";	
			greenElb.getName(); result = "GreenElb";	
			liveElb.getName(); result = "LiveElb";
		}};
		
		Logger l = Logger.getLogger("FiniteStateMachine");
		l.setLevel(Level.FINEST);
		l.addHandler(new ConsoleHandler());
		for (Handler h : l.getHandlers()) {
			h.setLevel(Level.FINEST);
		}
		
		Alfonso alfonso = new Alfonso();
		
		alfonso._blueElb = blueElb;
		alfonso._greenElb = greenElb;
		alfonso._liveElb = liveElb;
		
		Arguments arguments = new Arguments();
		
		String[] args = {"blueElbId=blue", "greenElbId=green", "liveElbId=live", "blueInstanceId=isBlue", "greenInstanceId=isGreen"};
		
		arguments.parseArgs(args);

		Assert.assertTrue(arguments.isValid());
		
		alfonso.configureStateMachine(arguments);
		alfonso.configureMonitors(alfonso._alfonsoStateMachine);
				
		alfonso.runStateMachine();
		
		try {
			Thread.sleep(40000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.assertEquals(alfonso._alfonsoStateMachine.getState().getName(),"Blue Live");
	}
	
}
