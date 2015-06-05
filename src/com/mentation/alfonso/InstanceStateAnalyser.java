package com.mentation.alfonso;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mentation.fsm.message.IMessage;

public class InstanceStateAnalyser implements IStateAnalyser {
	private Logger _logger = Logger.getLogger("FiniteStateMachine");
	
	@Override
	public IMessage map(ElbMonitoringDescriptor descriptor) {
		boolean b = descriptor.getLoadBalancer().isInstanceHealthy();
		
		_logger.log(Level.FINE, 
				new StringBuffer("Monitoring ").append(descriptor.getLoadBalancer().getName()).append(".IsInstanceHealthy returned ").append(b).toString());
		
		return b ? descriptor.getPassMessage() : descriptor.getFaileMessage();
	}

}
