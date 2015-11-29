package ro.cosu.vampires.server.resources.ec2;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;

public class EC2ResourceTest {

    @Test
    public void testCreate() throws Exception {

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
