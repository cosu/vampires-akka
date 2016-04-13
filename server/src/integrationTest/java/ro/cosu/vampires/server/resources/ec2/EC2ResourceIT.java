/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;

public class EC2ResourceIT {

    @Test
    @Ignore
    public void testCreate() throws Exception {
        //exploratory test

        File credentialsFile = Paths.get(System.getProperty("user.home"), ".aws").toFile();

        PropertiesCredentials credentials = new PropertiesCredentials(new FileInputStream(credentialsFile));

        AmazonEC2Client client = new AmazonEC2Client(credentials);

        EC2ResourceParameters params = EC2ResourceParameters.builder()
                .imageId("ami-47a23a30")
                .instanceType("t2.micro")
                .keyName("cdumitru-amazon-europe")
                .securityGroup("vampires")
                .region("eu-west-1")
                .command("test")
                .build();
        EC2Resource ec2Resource = new EC2Resource(params, client);
        ec2Resource.onStart();

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        DescribeInstancesResult result = client.describeInstances(request);

        List<Reservation> reservations = result.getReservations();
        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();
            for (Instance instance : instances) {
                if (instance.getState().getName().equals("running") || instance.getState().getName().equals("pending"))
                    System.out.println(instance);
            }
        }

    }
}
