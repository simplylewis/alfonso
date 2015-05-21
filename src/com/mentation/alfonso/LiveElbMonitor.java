package com.mentation.alfonso;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteStateMachine;

public class LiveElbMonitor extends ElbMonitor {

	public LiveElbMonitor(FiniteStateMachine fsm, String elbName,
			int pollingIntervalMillis, AmazonElasticLoadBalancingClient elbClient) {
		super(fsm, elbName, pollingIntervalMillis, elbClient);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IMessage monitor() {
		// TODO Auto-generated method stub
		return null;
	}

}
