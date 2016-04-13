package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;

import java.net.URL;

public class EC2Resource extends AbstractResource {

    private static final Logger LOG = LoggerFactory.getLogger(EC2Resource.class);
    private static final int MAX_TRIES = 20;
    private static final int TRY_INTERVAL_MILLI = 2000;


    private final AmazonEC2Client amazonEC2Client;
    private final EC2ResourceParameters parameters;
    private String instanceId;

    public EC2Resource(EC2ResourceParameters parameters, AmazonEC2Client amazonEC2Client) {
        super(parameters);
        this.amazonEC2Client = amazonEC2Client;
        this.parameters = parameters;
    }

    @Override
    public void onStart() throws Exception {

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        amazonEC2Client.setEndpoint(String.format("ec2.%1s.amazonaws.com", parameters.region()));

        URL cloudInitResource = Resources.getResource("cloud_init.yaml");
        String cloudInit = Resources.toString(cloudInitResource, Charsets.UTF_8);

        String command = parameters.command() + " " + description().id();

        cloudInit = cloudInit.replace("$command", command);
        LOG.debug("EC2command:  {}", command);

        RunInstancesRequest request = runInstancesRequest.withImageId(parameters.imageId())
                .withInstanceType(parameters.instanceType())
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(parameters.keyName())
                .withSecurityGroups(parameters.securityGroup())
                .withUserData(Base64.encodeBase64String(cloudInit.getBytes(Charsets.UTF_8)));

        instanceId = amazonEC2Client.runInstances(request)
                .getReservation().getInstances().get(0).getInstanceId();

        LOG.debug("Started amazon instance {}", instanceId);

        CreateTagsRequest createTagsRequest = new CreateTagsRequest();

        createTagsRequest.withResources(instanceId).withTags(new Tag("type", "vampires"));

        amazonEC2Client.createTags(createTagsRequest);

        DescribeInstancesRequest describeRequest = new DescribeInstancesRequest();

        describeRequest.withInstanceIds(instanceId);

        String publicDnsName = getPublicDnsName(describeRequest);

        LOG.info("instance {} : {}", instanceId, publicDnsName);
    }

    private String getPublicDnsName(DescribeInstancesRequest describeRequest) throws InterruptedException {
        String publicDnsName = "";
        int tries = 0;
        while (Strings.isNullOrEmpty(publicDnsName) && tries < MAX_TRIES) {
            publicDnsName = amazonEC2Client.describeInstances(describeRequest)
                    .getReservations().get(0).getInstances().get(0).getPublicDnsName();
            if (!Strings.isNullOrEmpty(publicDnsName)) break;
            tries++;
            Thread.sleep(TRY_INTERVAL_MILLI);
        }
        if (Strings.isNullOrEmpty(publicDnsName)) {
            LOG.warn("unable to get publicDNSName for instance {}", describeRequest.getInstanceIds());
        }
        return publicDnsName;
    }

    @Override
    public void onStop() throws Exception {
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
        TerminateInstancesResult terminateInstancesResult = amazonEC2Client.terminateInstances(terminateInstancesRequest);
        LOG.debug("terminate instance {}", terminateInstancesResult.toString());
    }

    @Override
    public void onFail() throws Exception {
        LOG.debug("ec2 fail");

    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
