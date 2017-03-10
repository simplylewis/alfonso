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

package com.mentation.alfonso.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Listener;

public class ElasticLoadBalancerTest {
	private static final String REGION_NAME = "us-west-2";
	AmazonElasticLoadBalancing _elbClient;
	private static String _testId = UUID.randomUUID().toString();
	IElasticLoadBalancer _elb;
	String _testName="ELB-"+System.currentTimeMillis();
	String _elbName;
	List<String> _instances = new ArrayList<String>();
	
	@BeforeSuite
	public void beforeSuite() {
		PropertiesFileCredentialsProvider credentials = new PropertiesFileCredentialsProvider("D:\\Development\\aws\\credentials.properties");
		_elbClient = AmazonElasticLoadBalancingClientBuilder.standard().withCredentials(credentials).withRegion(REGION_NAME).build();
		_elbName = _testName+"-1";
		
		System.out.println(createLoadBalancer(_elbName));
		_elb = new ElasticLoadBalancer(_elbName);	
		
		_instances.addAll(createEc2Instance(credentials, REGION_NAME));
		
		AwsInstance awsInstance = new AwsInstance();
		InstanceState current;
		
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			current = awsInstance.getState(_instances).get(_instances.get(0));
			
			System.out.println(current);
		} while (!current.getName().toLowerCase().startsWith("run"));
		

	}

	@AfterSuite
	public void afterSuite() {
		try {
			terminateEc2Instance(_instances);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		deleteLoadBalancer(_elbName);
	}
	
	@Test
	public void attachInstanceTest() {
		System.out.println("Attaching " + _instances.get(0) + " to ELB");
		Assert.assertTrue(_elb.attachInstance(_instances.get(0)));
	}
	
	@Test(dependsOnMethods = { "attachInstanceTest" })
	public void checkHealthyTest() {
		System.out.println("Waiting for instance to be healthy");
		try {
			Thread.sleep(90000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Assert.assertTrue(_elb.isInstanceHealthy());
	}
	
	@Test(dependsOnMethods = { "checkHealthyTest" })
	public void detachInstanceTest() {
		_elb.describe();
		
		Assert.assertTrue(_elb.detachInstance());
	}
	
	private CreateLoadBalancerResult createLoadBalancer(String name) {
		System.out.println("create ELB " + name);

		CreateLoadBalancerRequest createLoadBalancerRequest = new CreateLoadBalancerRequest(name);
		createLoadBalancerRequest.withListeners(new Listener("http", 80, 80));
		createLoadBalancerRequest.withAvailabilityZones("us-west-2a", "us-west-2b", "us-west-2c");
		
		return _elbClient.createLoadBalancer(createLoadBalancerRequest);
	}
	
	private void deleteLoadBalancer(String name) {
		System.out.println("delete ELB " + name);
		_elbClient.deleteLoadBalancer(new DeleteLoadBalancerRequest(name));		
	}
	
	private Collection<String> createEc2Instance(AWSCredentialsProvider credentials, String region) {
		AwsInstance awsInstance = new AwsInstance();

		Collection<String> securityGroupIds = new ArrayList<>();
		securityGroupIds.add("sg-57ff9532");
		securityGroupIds.add("sg-31fb9154");

		Collection<Tag> tags = new ArrayList<>();
		Tag role = new Tag("role", "bluehttp");
		
		tags.add(new Tag("envionment", "test"));
		tags.add(new Tag("testid", _testId ));		
		tags.add(role);

		
		List<String> instances = new ArrayList<String>(awsInstance.launch("ami-effac0df", "t2.micro", AwsInstance.getAvailabilityZones().get(0),
				"aws-general", securityGroupIds, tags));
		
		if (instances.isEmpty()) {
			throw new RuntimeException("Could not create AWS Instance");
		}
		
		return instances;
	}
	
	private void terminateEc2Instance(List<String> instances) {
		AwsInstance awsInstance = new AwsInstance();
		awsInstance.terminate(instances);
	}
}
