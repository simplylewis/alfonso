/*
Copyright 2015 Lewis Foti

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.mentation.alfonso;

import com.mentation.fsm.action.IStateEntryAction;

public class AddInstanceToElb implements IStateEntryAction {

	private String _instanceId;
	private ElasticLoadBalancer _elb;

	public AddInstanceToElb(ElasticLoadBalancer elb, String instanceId) {
		_elb = elb;
		_instanceId = instanceId;

	}
	
	@Override
	public void execute() {
		_elb.attachInstance(_instanceId);
	}

}
