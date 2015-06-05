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
	protected FiniteStateMachine _alfonsoStateMachine;
	protected ElbMonitor _blueElbMonitor;
	protected ElbMonitor _greenElbMonitor;
	protected ElbMonitor _liveElbMonitor;
	
	protected ElasticLoadBalancer _greenElb;
	protected ElasticLoadBalancer _blueElb;
	protected ElasticLoadBalancer _liveElb;
	
	protected String _blueId;
	protected String _greenId;
	
	class BlueIsHealthy implements IMessage {};
	class BlueIsUnhealthy implements IMessage {};
	class GreenIsHealthy implements IMessage {};
	class GreenIsUnhealthy implements IMessage {};
	class LiveHasNone implements IMessage {};
	class LiveHasOne implements IMessage {};
	
	protected final BlueIsHealthy _blueIsHealthy = new BlueIsHealthy();
	protected final BlueIsUnhealthy _blueIsUnhealthy = new BlueIsUnhealthy();
	protected final GreenIsHealthy _greenIsHealthy = new GreenIsHealthy();
	protected final GreenIsUnhealthy _greenIsUnhealthy = new GreenIsUnhealthy();
	protected final LiveHasNone _liveHasNone = new LiveHasNone();
	protected final LiveHasOne _liveHasOne = new LiveHasOne();
	
	public static void main(String[] args) {		
		Alfonso alfonso = new Alfonso();
		
		alfonso.runStateMachine();
	}
	
	public Alfonso() {
		// TODO need to get the correct values for the parameters
	}

	private ElbMonitor configureElbMonitor(ElasticLoadBalancer elb, IMessage passMessage, IMessage failMessage, IStateAnalyser stateAnalyser) {
		ElbMonitoringDescriptor descriptor = new ElbMonitoringDescriptor();
		
		descriptor.setLoadBalancer(elb);
		descriptor.setPollingInterval(10000);
		descriptor.setPassMessage(passMessage);
		descriptor.setFailMessage(failMessage);
		
		return new ElbMonitor(_alfonsoStateMachine, descriptor, stateAnalyser);
	}

	protected FiniteState configureFsm(ElasticLoadBalancer blueElb, String blueId, ElasticLoadBalancer greenElb, String greenId, ElasticLoadBalancer liveElb) {		
		
		FiniteState waitForBlueHealthy = new FiniteState(new AddInstanceToElb(blueElb, blueId), "Wait For BlueHealthy");
		FiniteState waitForGreenHealthy = new FiniteState(new AddInstanceToElb(greenElb, greenId), "Wait For GreenHealthy");
		FiniteState addBlueToLive = new FiniteState(new AddInstanceToElb(liveElb, blueId), "Add Blue To Live");
		FiniteState addGreenToLive = new FiniteState(new AddInstanceToElb(liveElb, greenId), "Add Green To Live");
		FiniteState blueLive = new FiniteState(null, "Blue Live");
		FiniteState greenLive = new FiniteState(null, "Green Live");
		FiniteState blueFailed = new FiniteState(new RemoveInstanceFromElb(blueElb), "Blue Failed");
		FiniteState greenFailed = new FiniteState(null, "Green Failed");
		FiniteState returnBlueToLive = new FiniteState(new RemoveInstanceFromElb(liveElb), "Remove Green From Live");
		FiniteState recovery = new FiniteState(new RemoveInstanceFromElb(liveElb), "Recovery");
		
		waitForBlueHealthy.addTransition(_blueIsHealthy, waitForGreenHealthy);
		
		waitForGreenHealthy.addTransition(_greenIsHealthy, addBlueToLive);
		
		addBlueToLive.addTransition(_liveHasOne, blueLive);
		addBlueToLive.addTransition(_blueIsUnhealthy, blueFailed);
		
		blueLive.addTransition(_blueIsUnhealthy, blueFailed);
		blueLive.addTransition(_greenIsUnhealthy, greenFailed);
		
		blueFailed.addTransition(_liveHasNone, addGreenToLive);
		
		greenFailed.addTransition(_greenIsHealthy, blueLive);
		greenFailed.addTransition(_blueIsUnhealthy, recovery);
		
		addGreenToLive.addTransition(_liveHasOne, greenLive);
		addGreenToLive.addTransition(_greenIsUnhealthy, recovery);
		
		greenLive.addTransition(_blueIsHealthy, returnBlueToLive);
		greenLive.addTransition(_greenIsUnhealthy, recovery);
		
		returnBlueToLive.addTransition(_liveHasNone, addBlueToLive);
		
		recovery.addTransition(_blueIsHealthy, addBlueToLive);
		recovery.addTransition(_greenIsHealthy, addGreenToLive);

		
		return waitForBlueHealthy;
	}
	
	protected FiniteStateMachine runStateMachine() {
		_alfonsoStateMachine = new FiniteStateMachine("Alfonso", configureFsm(_blueElb, _blueId, _greenElb, _greenId, _liveElb));

		_alfonsoStateMachine.start();
		
		_blueElbMonitor = configureElbMonitor(_blueElb, _blueIsHealthy, _blueIsUnhealthy, new InstanceStateAnalyser());
		_greenElbMonitor = configureElbMonitor(_greenElb, _greenIsHealthy, _greenIsUnhealthy, new InstanceStateAnalyser());
		_liveElbMonitor = configureElbMonitor(_liveElb, _liveHasOne, _liveHasNone, new LiveStateAnalyser());
		
		_blueElbMonitor.start();
		_greenElbMonitor.start();
		_liveElbMonitor.start();
		
		return _alfonsoStateMachine;
	}
}
