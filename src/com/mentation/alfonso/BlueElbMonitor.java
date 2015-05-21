package com.mentation.alfonso;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteStateMachine;

public class BlueElbMonitor extends ElbMonitor {

	public BlueElbMonitor(FiniteStateMachine fsm, String elbName,
			int pollingIntervalMillis, AmazonElasticLoadBalancingClient elbClient) {
		super(fsm, elbName, pollingIntervalMillis, elbClient);
	}

	@Override
	protected IMessage monitor() {
		// TODO Auto-generated method stub
		return null;
	}

}
