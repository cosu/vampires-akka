/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.resources.ec2;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import ro.cosu.vampires.server.resources.AbstractResource;

public class EC2Resource extends AbstractResource {

    private static final Logger LOG = LoggerFactory.getLogger(EC2Resource.class);
    private static final int MAX_TRIES = 20;
    private static final int TRY_INTERVAL_MILLI = 2000;


    private final AmazonEC2 amazonEC2Client;
    private final EC2ResourceParameters parameters;
    private String instanceId;

    public EC2Resource(EC2ResourceParameters parameters, AmazonEC2 amazonEC2Client) {
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

        String command = parameters.command() + " " + parameters.serverId() + " " + parameters().id();

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

        createTagsRequest.withResources(instanceId).withTags(new Tag("providerType", "vampires"));

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
            LOG.warn("unable to create publicDNSName for instance {}", describeRequest.getInstanceIds());
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
