package com.mentation.alfonso;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mentation.fsm.message.IMessage;

public class LiveStateAnalyser implements IStateAnalyser {
	private Logger _logger = Logger.getLogger("FiniteStateMachine");
	
	@Override
	public IMessage map(ElbMonitoringDescriptor descriptor) {
		IMessage msg = descriptor.getLoadBalancer().isInstanceHealthy() ? descriptor.getPassMessage() : descriptor.getFailMessage();
		
		_logger.log(Level.FINE, 
				new StringBuffer("Monitoring ").append(descriptor.getLoadBalancer().getName()).append(".IsInstanceHealthy mapped to ").append(msg.name()).toString());
		
		return msg;
	}

}
