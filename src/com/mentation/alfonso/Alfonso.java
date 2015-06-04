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

import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteState;
import com.mentation.fsm.state.FiniteStateMachine;

public class Alfonso {
	FiniteStateMachine _alfonsoStateMachine;
	ElbMonitor _blueElbMonitor;
	ElbMonitor _greenElbMonitor;
	ElbMonitor _liveElbMonitor;
	
	class BlueIsHealthy implements IMessage {};
	class BlueIsUnhealthy implements IMessage {};
	class GreenIsHealthy implements IMessage {};
	class GreenIsUnhealthy implements IMessage {};
	class LiveHasNone implements IMessage {};
	class LiveHasOne implements IMessage {};
	
	public static void main(String[] args) {		
		Alfonso alfonso = new Alfonso();
		
		alfonso.runStateMachine();
	}
	
	public Alfonso() {
		_alfonsoStateMachine = new FiniteStateMachine("Alfonso", configureFsm(null, null, null, null, null));
	}

	private ElbMonitor configureElbMonitor(ElasticLoadBalancer elb, IMessage passMessgage, IMessage failMessage, IStateAnalyser stateAnalyser) {
		ElbMonitoringDescriptor descriptor = new ElbMonitoringDescriptor();
		
		descriptor.setLoadBalancer(elb);
		descriptor.setPollingInterval(10000);
		
		return new ElbMonitor(_alfonsoStateMachine, descriptor, stateAnalyser);
		
	}

	private FiniteState configureFsm(ElasticLoadBalancer blueElb, String blueId, ElasticLoadBalancer greenElb, String greenId, ElasticLoadBalancer liveElb) {		
		
		FiniteState waitForBlueHealthy = new FiniteState(new AddInstanceToElb(blueElb, blueId), "Wait For BlueHealthy");
		FiniteState waitForGreenHealthy = new FiniteState(new AddInstanceToElb(greenElb, greenId), "Wait For GreenHealthy");
		FiniteState addBlueToLive = new FiniteState(new AddInstanceToElb(liveElb, blueId), "Add Blue to Live");
		FiniteState addGreenToLive = new FiniteState(new AddInstanceToElb(liveElb, greenId), "Add Green To Live");
		FiniteState blueLive = new FiniteState(null, "Blue Live");
		FiniteState greenLive = new FiniteState(null, "Green Live");
		FiniteState blueFailed = new FiniteState(new RemoveInstanceFromElb(blueElb), "Blue Failed");
		FiniteState greenFailed = new FiniteState(null, "Green Failed");
		FiniteState returnBlueToLive = new FiniteState(new RemoveInstanceFromElb(liveElb), "Return Blue To Live");
		FiniteState recovery = new FiniteState(new RemoveInstanceFromElb(liveElb), "Recovery");
		
		BlueIsHealthy blueIsHealthy = new BlueIsHealthy();
		BlueIsUnhealthy blueIsUnhealthy = new BlueIsUnhealthy();
		GreenIsHealthy greenIsHealthy = new GreenIsHealthy();
		GreenIsUnhealthy greenIsUnhealthy = new GreenIsUnhealthy();
		LiveHasNone liveHasNone = new LiveHasNone();
		LiveHasOne liveHasOne = new LiveHasOne();
		
		waitForBlueHealthy.addTransition(blueIsHealthy, waitForGreenHealthy);
		
		waitForGreenHealthy.addTransition(greenIsHealthy, addBlueToLive);
		
		addBlueToLive.addTransition(liveHasOne, blueLive);
		addBlueToLive.addTransition(blueIsUnhealthy, blueFailed);
		
		blueLive.addTransition(blueIsUnhealthy, blueFailed);
		blueLive.addTransition(greenIsUnhealthy, greenFailed);
		
		blueFailed.addTransition(liveHasNone, addGreenToLive);
		
		greenFailed.addTransition(greenIsHealthy, blueLive);
		greenFailed.addTransition(blueIsUnhealthy, recovery);
		
		addGreenToLive.addTransition(liveHasOne, greenLive);
		addGreenToLive.addTransition(greenIsUnhealthy, recovery);
		
		greenLive.addTransition(blueIsUnhealthy, returnBlueToLive);
		greenLive.addTransition(greenIsUnhealthy, recovery);
		
		returnBlueToLive.addTransition(liveHasNone, addBlueToLive);
		
		recovery.addTransition(blueIsHealthy, addBlueToLive);
		recovery.addTransition(greenIsHealthy, addGreenToLive);

		_blueElbMonitor = configureElbMonitor(blueElb, blueIsHealthy, blueIsUnhealthy, new InstanceStateAnalyser());
		_greenElbMonitor = configureElbMonitor(blueElb, greenIsHealthy, greenIsUnhealthy, new InstanceStateAnalyser());
		_liveElbMonitor = configureElbMonitor(liveElb, liveHasOne, liveHasNone, new LiveStateAnalyser());
		
		return waitForBlueHealthy;
	}
	
	public void runStateMachine() {
		_alfonsoStateMachine.start();
		_blueElbMonitor.start();
		_greenElbMonitor.start();
		_liveElbMonitor.start();
	}
}
