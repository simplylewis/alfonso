package com.mentation.alfonso;

import com.mentation.fsm.message.IMessage;

public interface IStateAnalyser {

	public IMessage map(ElbMonitoringDescriptor descriptor);

}
