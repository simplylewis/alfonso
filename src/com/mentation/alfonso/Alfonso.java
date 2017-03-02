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

import com.mentation.alfonso.aws.ElasticLoadBalancer;
import com.mentation.alfonso.aws.IElasticLoadBalancer;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteState;
import com.mentation.fsm.state.FiniteStateMachine;

public class Alfonso {
	protected FiniteStateMachine _alfonsoStateMachine;
	protected ElbMonitor _blueElbMonitor;
	protected ElbMonitor _greenElbMonitor;
	protected ElbMonitor _liveElbMonitor;
	
	protected IElasticLoadBalancer _greenElb;
	protected IElasticLoadBalancer _blueElb;
	protected IElasticLoadBalancer _liveElb;
	
	static final AlfonsoMessage _blueIsHealthy = new AlfonsoMessage("BlueIsHealthy");
	static final AlfonsoMessage _blueIsUnhealthy = new AlfonsoMessage("BlueIsUnhealthy");
	static final AlfonsoMessage _greenIsHealthy = new AlfonsoMessage("GreenIsHealthy");
	static final AlfonsoMessage _greenIsUnhealthy = new AlfonsoMessage("GreenIsUnhealthy");
	static final AlfonsoMessage _liveHasNone = new AlfonsoMessage("LiveHasNone");
	static final AlfonsoMessage _liveHasOne = new AlfonsoMessage("LiveHasOne");
	
	public static void main(String[] args) {
		Arguments arguments = new Arguments();
		
		arguments.parseArgs(args);
		
		if (!arguments.isValid()) {
			System.err.println("Arguments are incorrect and/or incomplete, exiting...");
			
			System.exit(1);
		}
		
		Alfonso alfonso = new Alfonso();
		
		alfonso.configureLoadBalancers(arguments);
		alfonso.configureStateMachine(arguments);
		alfonso.configureMonitors(alfonso._alfonsoStateMachine);
		
		alfonso.runStateMachine();
	}

	protected void configureMonitors(FiniteStateMachine fsm) {
		_blueElbMonitor = configureElbMonitor(fsm, _blueElb, _blueIsHealthy, _blueIsUnhealthy, new InstanceStateAnalyser());
		_greenElbMonitor = configureElbMonitor(fsm, _greenElb, _greenIsHealthy, _greenIsUnhealthy, new InstanceStateAnalyser());
		_liveElbMonitor = configureElbMonitor(fsm, _liveElb, _liveHasOne, _liveHasNone, new LiveStateAnalyser());
	}

	private void configureLoadBalancers(Arguments arguments) {
		_blueElb = new ElasticLoadBalancer(arguments.getBlueElbId());
		_greenElb = new ElasticLoadBalancer(arguments.getGreenElbId());
		_liveElb = new ElasticLoadBalancer(arguments.getLiveElbId());
	}

	private ElbMonitor configureElbMonitor(FiniteStateMachine fsm, IElasticLoadBalancer elb, IMessage passMessage, IMessage failMessage, IStateAnalyser stateAnalyser) {
		ElbMonitoringDescriptor descriptor = new ElbMonitoringDescriptor();
		
		descriptor.setLoadBalancer(elb);
		descriptor.setPollingInterval(10000);
		descriptor.setPassMessage(passMessage);
		descriptor.setFailMessage(failMessage);
		
		return new ElbMonitor(fsm, descriptor, stateAnalyser);
	}

	protected FiniteState configureFsm(IElasticLoadBalancer blueElb, String blueId, IElasticLoadBalancer greenElb, String greenId, IElasticLoadBalancer liveElb) {		
		
		FiniteState start = new FiniteState(null, "Start");
		FiniteState waitForBlueHealthy = new FiniteState(new AddInstanceToElb(blueElb, blueId), "Wait For BlueHealthy");
		FiniteState waitForGreenHealthy = new FiniteState(new AddInstanceToElb(greenElb, greenId), "Wait For GreenHealthy");
		FiniteState addBlueToLive = new FiniteState(new AddInstanceToElb(liveElb, blueId), "Add Blue To Live");
		FiniteState addGreenToLive = new FiniteState(new AddInstanceToElb(liveElb, greenId), "Add Green To Live");
		FiniteState blueLive = new FiniteState(null, "Blue Live");
		FiniteState greenLive = new FiniteState(null, "Green Live");
		FiniteState blueFailed = new FiniteState(new RemoveInstanceFromElb(liveElb), "Blue Failed");
		FiniteState greenFailed = new FiniteState(null, "Green Failed");
		FiniteState returnBlueToLive = new FiniteState(new RemoveInstanceFromElb(liveElb), "Remove Green From Live");
		FiniteState recovery = new FiniteState(new RemoveInstanceFromElb(liveElb), "Recovery");
		
		start.addTransition(_blueIsUnhealthy, waitForBlueHealthy);
		start.addTransition(_blueIsHealthy, waitForGreenHealthy);
		
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

		
		return start;
	}
	
	protected void configureStateMachine(Arguments arguments) {			
		_alfonsoStateMachine = new FiniteStateMachine("Alfonso", configureFsm(_blueElb, arguments.getBlueInstanceId(), _greenElb, arguments.getGreenInstanceId(), _liveElb));
	}
	
	protected void runStateMachine() {		
		_alfonsoStateMachine.start();
		
		_blueElbMonitor.start();
		_greenElbMonitor.start();
		_liveElbMonitor.start();
	}
}
