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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mentation.alfonso.aws.IElasticLoadBalancer;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteStateMachine;

public class ElbMonitor extends Thread {
	private FiniteStateMachine _fsm;
	private ElbMonitoringDescriptor _descriptor;
	private IStateAnalyser _stateAnalyser;
	private Logger _logger = Logger.getLogger("FiniteStateMachine");
	
	public ElbMonitor(FiniteStateMachine fsm, ElbMonitoringDescriptor descriptor, IStateAnalyser stateAnalyser) {
		_fsm = fsm;
		_descriptor = descriptor;
		_stateAnalyser = stateAnalyser;
		
		setDaemon(true);
		setName("ElbMonitor:" + _descriptor.getLoadBalancer().getName());
	}
	
	public void run() {
		while (true) {
			try {				
				long timeNow = System.currentTimeMillis();
				
				_fsm.consumeMessage(monitor());
				
				Thread.sleep(_descriptor.getPollingInterval() - System.currentTimeMillis() + timeNow);
			} catch (InterruptedException e) {
				// TODO Implement a counter that to exit if there are to many consecutive exceptions thrown
				e.printStackTrace();
			}
		}
	}

	protected String getElbName() {
		return _descriptor.getLoadBalancer().getName();
	}
	
	protected IElasticLoadBalancer getLoadBalancer() {
		return _descriptor.getLoadBalancer();
	}
	
	protected IMessage monitor() {
		IMessage msg = _stateAnalyser.map(_descriptor);
		
		_logger.log(Level.FINE, new StringBuilder("Monitor returning ").append(msg.name()).append(" from ").append(_descriptor.getLoadBalancer().getName()).toString());
		
		return msg;
	}
}
