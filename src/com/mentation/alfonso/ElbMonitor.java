package com.mentation.alfonso;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteStateMachine;

public abstract class ElbMonitor extends Thread {
	private FiniteStateMachine _fsm;
	private String _elbName;
	private int _pollingIntervalMillis;
	private AmazonElasticLoadBalancingClient _elbClient;
	
	
	public ElbMonitor(FiniteStateMachine fsm, String elbName, int pollingIntervalMillis, AmazonElasticLoadBalancingClient elbClient) {
		_fsm = fsm;
		_elbName = elbName;
		_pollingIntervalMillis = pollingIntervalMillis;
		_elbClient = elbClient;
		
		setDaemon(true);
		setName("ElbMonitor:" + elbName);		
	}
	
	public void run() {
		while (true) {
			try {				
				long timeNow = System.currentTimeMillis();
				
				_fsm.consumeMessage(monitor());
				
				Thread.sleep(_pollingIntervalMillis - System.currentTimeMillis() + timeNow);
			} catch (InterruptedException e) {
				// TODO Implement a counter that to exit if there are to many consecutive exceptions thrown
				e.printStackTrace();
			}
		}
	}

	protected String getElbName() {
		return _elbName;
	}
	
	protected AmazonElasticLoadBalancingClient getElbClient() {
		return _elbClient;
	}
	
	protected abstract IMessage monitor();	
}
