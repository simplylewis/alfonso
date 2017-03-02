package com.mentation.alfonso;

import com.mentation.fsm.message.IMessage;

public class AlfonsoMessage implements IMessage {
	private String _name;

	public AlfonsoMessage(String name) {
		_name = name;
	}
	
	@Override
	public String name() {
		 return _name;
	}

}
