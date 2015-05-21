package com.mentation.alfonso;

import com.mentation.fsm.message.IMessage;
import com.mentation.fsm.state.FiniteState;
import com.mentation.fsm.state.FiniteStateMachine;

public class Alfonso {
	FiniteStateMachine _alfonsoStateMachine;
	
	class BlueIsHealthy implements IMessage {};
	class BlueIsLive implements IMessage {};
	class BlueIsUnhealthy implements IMessage {};
	class GreenIsHealthy implements IMessage {};
	class GreenIsLive implements IMessage {};
	class GreenIsUnhealthy implements IMessage {};
	class NoneAreLive implements IMessage {};

	public static void main(String[] args) {		
		Alfonso alfonso = new Alfonso();
		
		alfonso.run();
	}
	
	public Alfonso() {
		FiniteState waitForBlueHealthy = new FiniteState(new AddBlueToBlueElb(), "Wait For BlueHealthy");
		FiniteState waitForGreenHealthy = new FiniteState(new AddGreenToGreenElb(), "Wait For GreenHealthy");
		FiniteState addBlueToLive = new FiniteState(new AddBlueToLiveElb(), "Add Blue to Live");
		FiniteState addGreenToLive = new FiniteState(new AddGreenToLiveElb(), "Add Green To Live");
		FiniteState blueLive = new FiniteState(null, "Blue Live");
		FiniteState greenLive = new FiniteState(null, "Green Live");
		FiniteState blueFailed = new FiniteState(new RemoveBlueFromLiveElb(), "Blue Failed");
		FiniteState greenFailed = new FiniteState(null, "Green Failed");
		FiniteState returnBlueToLive = new FiniteState(new RemoveGreenFromLiveElb(), "Return Blue To Live");
		FiniteState recovery = new FiniteState(new RemoveAllFromLiveElb(), "Recovery");
		
		BlueIsHealthy blueIsHealthy = new BlueIsHealthy();
		BlueIsLive blueIsLive = new BlueIsLive();
		BlueIsUnhealthy blueIsUnhealthy = new BlueIsUnhealthy();
		GreenIsHealthy greenIsHealthy = new GreenIsHealthy();
		GreenIsLive greenIsLive = new GreenIsLive();
		GreenIsUnhealthy greenIsUnhealthy = new GreenIsUnhealthy();
		NoneAreLive noneAreLive = new NoneAreLive();
		
		waitForBlueHealthy.addTransition(blueIsHealthy, waitForGreenHealthy);
		
		waitForGreenHealthy.addTransition(greenIsHealthy, addBlueToLive);
		
		addBlueToLive.addTransition(blueIsLive, blueLive);
		addBlueToLive.addTransition(blueIsUnhealthy, blueFailed);
		
		blueLive.addTransition(blueIsUnhealthy, blueFailed);
		blueLive.addTransition(greenIsUnhealthy, greenFailed);
		
		blueFailed.addTransition(noneAreLive, addGreenToLive);
		
		greenFailed.addTransition(greenIsHealthy, blueLive);
		greenFailed.addTransition(blueIsUnhealthy, recovery);
		
		addGreenToLive.addTransition(greenIsLive, greenLive);
		addGreenToLive.addTransition(greenIsUnhealthy, recovery);
		
		greenLive.addTransition(blueIsUnhealthy, returnBlueToLive);
		greenLive.addTransition(greenIsUnhealthy, recovery);
		
		returnBlueToLive.addTransition(noneAreLive, addBlueToLive);
		
		recovery.addTransition(blueIsHealthy, addBlueToLive);
		recovery.addTransition(greenIsHealthy, addGreenToLive);
		
		_alfonsoStateMachine = new FiniteStateMachine("Alfonso", waitForBlueHealthy);
	}
	
	public void run() {
		_alfonsoStateMachine.start();
	}
}
