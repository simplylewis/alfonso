package com.mentation.alfonso;

import java.util.Set;

import com.mentation.fsm.message.IMessage;

public class ElbMonitoringDescriptor {
	ElasticLoadBalancer _loadBalancer;
	int _pollingInterval;
	IMessage _passMessage = null;
	IMessage _failMessage = null;
	
	Set<String> _validInstances = null;

	public ElasticLoadBalancer getLoadBalancer() {
		return _loadBalancer;
	}

	public void setLoadBalancer(ElasticLoadBalancer loadBalancer) {
		this._loadBalancer = loadBalancer;
	}

	public int getPollingInterval() {
		return _pollingInterval;
	}

	public void setPollingInterval(int pollingInterval) {
		this._pollingInterval = pollingInterval;
	}

	public IMessage getPassMessage() {
		return _passMessage;
	}

	public void setPassMessage(IMessage passMessage) {
		_passMessage = passMessage;
	}
	
	public IMessage getFaileMessage() {
		return _failMessage;
	}

	public void setFailMessage(IMessage failMessage) {
		this._failMessage = failMessage;
	}

	public Set<String> getValidInstances() {
		return _validInstances;
	}

	public void setValidInstances(Set<String> validInstances) {
		this._validInstances = validInstances;
	}
	
}
