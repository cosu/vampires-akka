package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;

import java.net.URL;

public class EC2Resource extends AbstractResource {

    static final Logger LOG = LoggerFactory.getLogger(EC2Resource.class);

    private final AmazonEC2Client amazonEC2Client;
    private final EC2ResourceParameters parameters;
    private  String instanceId;

    public EC2Resource(EC2ResourceParameters parameters, AmazonEC2Client amazonEC2Client) {
        super(parameters);
        this.amazonEC2Client = amazonEC2Client;
        this.parameters = parameters;
    }

    @Override
    public void onStart() throws Exception {

        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        amazonEC2Client.setEndpoint(String.format("ec2.%1s.amazonaws.com", parameters.region()));


        URL cloudInitResource = Resources.getResource("cloud_init.yaml");
        String cloudInit = Resources.toString(cloudInitResource, Charsets.UTF_8);

        cloudInit = cloudInit.replace("$command", parameters.command());
        LOG.info("command {}", parameters.command());

        RunInstancesRequest request = runInstancesRequest.withImageId(parameters.imageId())
                .withInstanceType(parameters.instanceType())
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(parameters.keyName())
                .withSecurityGroups(parameters.securityGroup())
                .withUserData(Base64.encodeBase64String(cloudInit.getBytes()));


        RunInstancesResult result;

        result = amazonEC2Client.runInstances(request);

        instanceId = result.getReservation().getInstances().get(0).getInstanceId();

        LOG.info("Started amazon instance {}", instanceId);

        CreateTagsRequest createTagsRequest = new CreateTagsRequest();

        createTagsRequest.withResources(instanceId)
                .withTags(
                        new Tag("type", "vampires")
                );

        amazonEC2Client.createTags(createTagsRequest);

        DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();

        describeRequest.withInstanceIds(instanceId);

        DescribeInstancesResult describeInstanceResult = amazonEC2Client.describeInstances(describeRequest);

        String publicDnsName = describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicDnsName();
        LOG.info("instance {} : {}",instanceId, publicDnsName );

    }

    @Override
    public void onStop() throws Exception {
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
        TerminateInstancesResult terminateInstancesResult = amazonEC2Client.terminateInstances(terminateInstancesRequest);
        LOG.info("terminate instance {}", terminateInstancesResult.toString());
    }

    @Override
    public void onFail() throws Exception {
        LOG.debug("ec2 fail");

    }
}