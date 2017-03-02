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

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mentation.alfonso.aws.IElasticLoadBalancer;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteState;
import com.mentation.fsm.state.FiniteStateMachine;

public class AlfonsoTest {

	IElasticLoadBalancer blueElb;
	IElasticLoadBalancer greenElb;
	IElasticLoadBalancer liveElb;
	
	class DummyLoadBalancer implements IElasticLoadBalancer {
		boolean _instanceAttached = false;
		private String _instanceId;
		private String _name;
		
		public DummyLoadBalancer(String name) {
			_name = name;
		}
		
		@Override
		public void describe() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean attachInstance(String instanceId) {
			System.err.println(_name + " attachInstance called");
			_instanceId = instanceId;
			_instanceAttached = true;
			return true;
		}

		@Override
		public boolean detachInstance() {
			System.err.println(_name + " detachInstance called");
			_instanceAttached = false;
			return true;
		}

		@Override
		public String getInstanceId() {
			return _instanceId;
		}

		@Override
		public boolean isInstanceHealthy() {
			return _instanceAttached;
		}

		@Override
		public String getName() {
			return _name;
		}
		
	}

	@Test
	public void configureFsmTest() {
		
		Alfonso alfonso = new Alfonso();

		FiniteState initialState =
				alfonso.configureFsm(new DummyLoadBalancer("BlueElb"), "blueId", 
						new DummyLoadBalancer("GreenElb"), "greenId", 
						new DummyLoadBalancer("LiveElb"));

		Assert.assertEquals(initialState.getName(), "Start");

		FiniteStateMachine fsm = new FiniteStateMachine("Alfonso Test", initialState);

		fsm.start();

		postMessage(fsm, Alfonso._blueIsUnhealthy, "Wait For BlueHealthy");
		postMessage(fsm, Alfonso._blueIsHealthy, "Wait For GreenHealthy");	  
		postMessage(fsm, Alfonso._greenIsHealthy, "Add Blue To Live");	  
		postMessage(fsm, Alfonso._liveHasOne, "Blue Live");	  
		postMessage(fsm, Alfonso._blueIsHealthy, "Blue Live");	  
		postMessage(fsm, Alfonso._greenIsHealthy, "Blue Live");	  
		postMessage(fsm, Alfonso._greenIsUnhealthy, "Green Failed");	  
		postMessage(fsm, Alfonso._greenIsHealthy, "Blue Live");	  
		postMessage(fsm, Alfonso._blueIsUnhealthy, "Blue Failed");	  
		postMessage(fsm, Alfonso._liveHasNone, "Add Green To Live");	  
		postMessage(fsm, Alfonso._liveHasOne, "Green Live");	  	  
		postMessage(fsm, Alfonso._blueIsUnhealthy, "Green Live");	  
		postMessage(fsm, Alfonso._greenIsUnhealthy, "Recovery");	  
		postMessage(fsm, Alfonso._greenIsHealthy, "Add Green To Live");	  
		postMessage(fsm, Alfonso._liveHasOne, "Green Live");	  	  
		postMessage(fsm, Alfonso._blueIsHealthy, "Remove Green From Live");	  
		postMessage(fsm, Alfonso._liveHasNone, "Add Blue To Live");	  
		postMessage(fsm, Alfonso._liveHasOne, "Blue Live");	  
		postMessage(fsm, Alfonso._greenIsUnhealthy, "Green Failed");
		postMessage(fsm, Alfonso._blueIsUnhealthy, "Recovery");
		postMessage(fsm, Alfonso._blueIsHealthy, "Add Blue To Live");
		
		fsm.end();

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


	@Test
	public void runStateMachineTest() {
		
		Logger l = Logger.getLogger("FiniteStateMachine");
		l.setLevel(Level.FINEST);

		for (Handler h : l.getHandlers()) {
			h.setLevel(Level.FINEST);
		}
		
		Alfonso alfonso = new Alfonso();
		
		alfonso._blueElb = new DummyLoadBalancer("BlueElb");
		alfonso._greenElb = new DummyLoadBalancer("GreenElb");
		alfonso._liveElb = new DummyLoadBalancer("LiveElb");
		
		Arguments arguments = new Arguments();
		
		String[] args = {"blueElbId=blue", "greenElbId=green", "liveElbId=live", "blueInstanceId=isBlue", "greenInstanceId=isGreen"};
		
		arguments.parseArgs(args);

		Assert.assertTrue(arguments.isValid());
		
		alfonso.configureStateMachine(arguments);
		alfonso.configureMonitors(alfonso._alfonsoStateMachine);
				
		alfonso.runStateMachine();
		
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.assertEquals(alfonso._alfonsoStateMachine.getState().getName(),"Blue Live");
	}
	
}
