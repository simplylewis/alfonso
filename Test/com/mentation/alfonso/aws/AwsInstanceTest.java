package com.mentation.alfonso.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.amazonaws.services.ec2.model.Tag;

public class AwsInstanceTest {
	Collection<String> _instances;
	String _testId;

	@BeforeSuite()
	public void beforeSuite() {
		_testId = UUID.randomUUID().toString();
		System.out.println("TestId: " + _testId);
	}
	
	@Test
	public void launchTest() {

		AwsInstance awsInstance = new AwsInstance();

		Collection<String> securityGroupIds = new ArrayList<>();
		securityGroupIds.add("sg-57ff9532");
		securityGroupIds.add("sg-31fb9154");

		Collection<Tag> tags = new ArrayList<>();
		Tag role = new Tag("role", "bluehttp");
		
		tags.add(new Tag("envionment", "test"));
		tags.add(new Tag("testid", _testId));		
		tags.add(role);

		_instances = awsInstance.launch("ami-e7527ed7", "t2.micro", AwsInstance.getAvailabilityZones().get(0),
				"aws-general", securityGroupIds, tags);
		
		role.setValue("greenhttp");
		
		_instances.addAll(awsInstance.launch("ami-e7527ed7", "t2.micro", AwsInstance.getAvailabilityZones().get(1),
				"aws-general", securityGroupIds, tags));

		Assert.assertNotNull(_instances);
		Assert.assertTrue(_instances.size() >= 2);
		
		
	}
	
	@Test(dependsOnMethods = {"launchTest"})
	public void listInstancesTest() {
		Collection<Tag> tags = new ArrayList<>();
		List<String> instances;
		tags.add(new Tag("testid", _testId));
				
		instances = AwsInstance.getInstances(tags);
		
		Assert.assertNotNull(instances);
		Assert.assertEquals(instances.size(), 2);
		
		tags.add(new Tag("role", "greenhttp"));
				
		instances = AwsInstance.getInstances(tags);
		Assert.assertEquals(instances.size(), 1);
	}
	
	@Test(dependsOnMethods = { "launchTest" })
	public void terminateTest() {
		AwsInstance awsInstance = new AwsInstance();
		
		for(String instanceId : _instances) {
			Assert.assertTrue(awsInstance.terminate(instanceId));
		}
		
	}
	
	@AfterSuite
	public void afterSuite() {
		AwsInstance awsInstance = new AwsInstance();
		
		for(String instanceId : _instances) {
			System.out.println("Shutting down instance " + instanceId);
			awsInstance.terminate(instanceId);
		}
	}
}
