package com.mentation.alfonso;

import com.mentation.fsm.message.IMessage;

public class LiveStateAnalyser implements IStateAnalyser {

	@Override
	public IMessage map(ElbMonitoringDescriptor descriptor) {
		return descriptor.getLoadBalancer().countInstances() == 1 ? descriptor.getPassMessage() : descriptor.getFaileMessage();
	}

}
