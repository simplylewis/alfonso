package com.mentation.alfonso;

import com.mentation.fsm.message.IMessage;

public class InstanceStateAnalyser implements IStateAnalyser {

	@Override
	public IMessage map(ElbMonitoringDescriptor descriptor) {
		return descriptor.getLoadBalancer().isInstanceHealthy() ? descriptor.getPassMessage() : descriptor.getFaileMessage();
	}

}
