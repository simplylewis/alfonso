package com.mentation.alfonso;

import java.util.Set;

import com.mentation.alfonso.aws.IElasticLoadBalancer;
import com.mentation.fsm.message.IMessage;

public class ElbMonitoringDescriptor {
	IElasticLoadBalancer _loadBalancer;
	int _pollingInterval;
	IMessage _passMessage;
	IMessage _failMessage;
	
	Set<String> _validInstances;

	public IElasticLoadBalancer getLoadBalancer() {
		return _loadBalancer;
	}

	public void setLoadBalancer(IElasticLoadBalancer loadBalancer) {
		_loadBalancer = loadBalancer;
	}

	public int getPollingInterval() {
		return _pollingInterval;
	}

	public void setPollingInterval(int pollingInterval) {
		_pollingInterval = pollingInterval;
	}

	public IMessage getPassMessage() {
		return _passMessage;
	}

	public void setPassMessage(IMessage passMessage) {
		_passMessage = passMessage;
	}
	
	public IMessage getFailMessage() {
		return _failMessage;
	}

	public void setFailMessage(IMessage failMessage) {
		_failMessage = failMessage;
	}

	public Set<String> getValidInstances() {
		return _validInstances;
	}

	public void setValidInstances(Set<String> validInstances) {
		_validInstances = validInstances;
	}
	
}
