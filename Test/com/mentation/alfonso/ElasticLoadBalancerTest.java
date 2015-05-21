package com.mentation.alfonso;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ElasticLoadBalancerTest {
	ElasticLoadBalancer _elb;
	
	@BeforeSuite
	public void beforeSuite() {
		_elb = new ElasticLoadBalancer("TestElb");	
	}

	@Test
	public void attachInstanceTest() {
		Assert.assertTrue(_elb.attachInstance("i-c0ce8636"));
	}
	
	@Test(dependsOnMethods = { "attachInstanceTest" })
	public void checkHealthyTest() {
		try {
			Thread.sleep(20000);
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
}
