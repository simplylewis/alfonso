package com.mentation.alfonso.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class AwsInstance {
	
	private static AmazonEC2Client getClient() {
		AmazonEC2Client ec2Client = new AmazonEC2Client();
		// TODO Should read this from properties file
		ec2Client.setRegion(Region.getRegion(Regions.US_WEST_2));
		
		return ec2Client;
	}
	
	public static List<String> getAvailabilityZones() {
		AmazonEC2Client ec2Client = getClient();
		
		DescribeAvailabilityZonesResult dazr = ec2Client.describeAvailabilityZones();
		
		List<String> zones = new ArrayList<>();
		
		for (AvailabilityZone az : dazr.getAvailabilityZones()) {
			zones.add(az.getZoneName());
		}
		
		return zones;		
	}
	
	public static List<String> getInstances(Collection<Tag> tags) {
		AmazonEC2Client ec2Client = getClient();
		
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		for (Tag tag : tags) {
			describeInstancesRequest.withFilters(new Filter().withName("tag:"+tag.getKey()).withValues(tag.getValue()));
		}
		
		DescribeInstancesResult dir = ec2Client.describeInstances(describeInstancesRequest);
		
		List<String> instances = new ArrayList<String>();
		
		for (Reservation res : dir.getReservations()) {
			for (Instance inst : res.getInstances()) {
				instances.add(inst.getInstanceId());
			}
		}
		
		return instances;
	}
	
	public Collection<String> launch(String ami, String instanceType, String availabilityZone, String keyName, Collection<String> securityGroups, Collection<Tag> tags) {
		AmazonEC2Client ec2Client = getClient();
		
		String clientToken = UUID.randomUUID().toString();
		
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest(ami, 1, 1);
		runInstancesRequest.setKeyName(keyName);
		runInstancesRequest.setInstanceType(instanceType);
		runInstancesRequest.setSecurityGroupIds(securityGroups);
		runInstancesRequest.setClientToken(clientToken);		
		runInstancesRequest.setPlacement(new Placement(availabilityZone));
		
		RunInstancesResult runInstancesResult = ec2Client.runInstances(runInstancesRequest);

		System.out.println(runInstancesResult.getReservation());
		
		ArrayList<String> instanceIds = new ArrayList<>();
				
		for (Instance inst : runInstancesResult.getReservation().getInstances()) {
			
			instanceIds.add(inst.getInstanceId());
		}

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.setResources(instanceIds);
		createTagsRequest.setTags(tags);
		
		ec2Client.createTags(createTagsRequest);
		
		return instanceIds;
	}
	
	public boolean terminate(String instanceId) {
		AmazonEC2Client ec2Client = getClient();
		
		List<String> instanceIds = new ArrayList<>();
		instanceIds.add(instanceId);
		
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
		
		TerminateInstancesResult result = ec2Client.terminateInstances(terminateInstancesRequest);
		
		for (InstanceStateChange isc : result.getTerminatingInstances()) {
			if (isc.getInstanceId().equals(instanceId)) return true;
		}
		
		return false;
	}

}
