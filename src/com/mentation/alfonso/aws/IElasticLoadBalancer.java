package com.mentation.alfonso.aws;

public interface IElasticLoadBalancer {

	void describe();

	boolean attachInstance(String instanceId);

	boolean detachInstance();

	String getInstanceId();

	boolean isInstanceHealthy();

	String getName();

}