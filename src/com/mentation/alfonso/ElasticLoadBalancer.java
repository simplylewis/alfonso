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

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

public class ElasticLoadBalancer {

	private String _name;
	private Instance _instance;
	private AmazonElasticLoadBalancingClient _elbClient;
	
	public ElasticLoadBalancer(String name) {
		_name = name;
		
		_elbClient = new AmazonElasticLoadBalancingClient();
		// TODO Should read this from properties file
		_elbClient.setRegion(Region.getRegion(Regions.US_WEST_2));  
	}
	
	
	public void describe() {
		List<String> loadBalancers = new ArrayList<>();
		loadBalancers.add(_name);
		
		DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest(loadBalancers);
		DescribeLoadBalancersResult describeLoadBalancersResult = _elbClient.describeLoadBalancers(describeLoadBalancersRequest);
		
		for (LoadBalancerDescription lbd : describeLoadBalancersResult.getLoadBalancerDescriptions()) {
			System.out.println(lbd);
		}
	}
	
	public boolean attachInstance(String instanceId) {
		Instance instance = new Instance(instanceId);
		
		List<Instance> instances = new ArrayList<>();
		instances.add(instance);
		
		RegisterInstancesWithLoadBalancerRequest registerInstancesWithLoadBalancerRequest = new RegisterInstancesWithLoadBalancerRequest("TestElb", instances);
		
		RegisterInstancesWithLoadBalancerResult result = _elbClient.registerInstancesWithLoadBalancer(registerInstancesWithLoadBalancerRequest);
		
		if (result.getInstances().contains(instance)) {
			_instance = instance;
			return true;
		}

		return false;
	}

	public boolean detachInstance() {
		List<Instance> instances = new ArrayList<>();
		instances.add(_instance);
		System.out.println("Attempt to deregister " + _instance.getInstanceId());
		
		DeregisterInstancesFromLoadBalancerRequest deregisterInstancesFromLoadBalancerRequest = 
				new DeregisterInstancesFromLoadBalancerRequest(_name, instances);
		
		DeregisterInstancesFromLoadBalancerResult deregisterInstancesFromLoadBalancerResult =
				_elbClient.deregisterInstancesFromLoadBalancer(deregisterInstancesFromLoadBalancerRequest);
		
		System.out.println(deregisterInstancesFromLoadBalancerResult.getInstances());
		
		for (Instance instance : deregisterInstancesFromLoadBalancerResult.getInstances()) {
			if (instance.getInstanceId().equals(_instance.getInstanceId())) {
				return false;
			}
		}
		
		_instance = null;
		
		return true;
	}
	
	public boolean isInstanceHealthy() {
		List<Instance> instances = new ArrayList<>();
		instances.add(_instance);
		
		DescribeInstanceHealthRequest describeInstanceHealthRequest = new DescribeInstanceHealthRequest(_name);
		describeInstanceHealthRequest.withInstances(instances);
		
		DescribeInstanceHealthResult describeInstanceHealthResult = _elbClient.describeInstanceHealth(describeInstanceHealthRequest);
		
		for (InstanceState state : describeInstanceHealthResult.getInstanceStates())  {
			if (state.getState().equals("InService") && state.getInstanceId().equals(_instance.getInstanceId())) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getName() {
		return _name;
	}


	public int countInstances() {
		// TODO Auto-generated method stub
		return 0;
	}
}
