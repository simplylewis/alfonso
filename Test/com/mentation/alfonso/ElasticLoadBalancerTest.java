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
