package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.provider.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.batch.AWSBatchClientBuilder;
import com.amazonaws.services.batch.model.ComputeResource;
import com.amazonaws.services.batch.model.CreateComputeEnvironmentRequest;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;

public class BatchClientWrapperTest {


  @Test
  public void assertThatDefaultCredentialsWork(){

//    var batchClient = new BatchClientWrapper().getClient();
//
//    var describeVpcsRequest = new DescribeVpcsRequest();
//
//
//    var vpcClient = AmazonEC2ClientBuilder.standard()
//            .withRegion(Regions.US_EAST_2)
//            .build();
//    var response = vpcClient.describeVpcs(describeVpcsRequest);
//
//    var vpcId = response.getVpcs().get(0);
//
//    var subnets = vpcClient.describeSubnets();
//
//    var securityGroups = vpcClient.describeSecurityGroups();
//
//    var listJobsRequest = new ListJobsRequest();
//
//    var describeComputeEnvironments = new DescribeComputeEnvironmentsRequest();
//
//    var a = batchClient.describeComputeEnvironments(describeComputeEnvironments);
//
//    System.out.println("Response: " + a.getComputeEnvironments().size());

  }


//  @Test
//  public void testComputeEnvironmentParsing() throws IOException {
//
//    var url = Resources.getResource("compute-environment.json");
//    String result = Resources.toString(url, Charsets.UTF_8);
//
//    var computeEnvironment = new Gson().fromJson(result, CreateComputeEnvironmentRequest.class);
//    var client = AWSBatchClientBuilder.standard().build();
//    computeEnvironment.setComputeEnvironmentName("Test");
//    var response = client.createComputeEnvironment(computeEnvironment);
//
//    System.out.println(response);
//
//  }

//  @Test
//  public void assertThatWeCanCreateEnvironmentsWithDefaultPermissions(){
//
//
//    var vpcClient = AmazonEC2ClientBuilder.standard()
//            .withRegion(Regions.US_EAST_2)
//            .build();
//
//    var subnets = vpcClient.describeSubnets().getSubnets();
//
//    var securityGroups = vpcClient.describeSecurityGroups().getSecurityGroups();
//
//    var client = AWSBatchClientBuilder.standard().build();
//    var request = new CreateComputeEnvironmentRequest()
//            .withComputeEnvironmentName("M4Spot")
//            .withType("MANAGED")
//            .withState("ENABLED")
//            .withComputeResources(
//                    new ComputeResource()
//                            .withType("SPOT")
//                            .withMinvCpus(0)
//                            .withMaxvCpus(12)
//                            .withDesiredvCpus(0)
//                            .withInstanceTypes("c4.large")
//                            .withBidPercentage(20)
//                            .withSubnets(subnets.stream().map(subnet -> subnet.getSubnetId()).collect(Collectors.toList()))
//                            .withSecurityGroupIds(securityGroups.stream().map(securityGroup -> securityGroup.getGroupId()).collect(Collectors.toList()))
//                            .withInstanceRole("arn:aws:iam::199428496322:instance-profile/ecsInstanceRole")
//                            .withSpotIamFleetRole("arn:aws:iam::199428496322:role/aws-ec2-spot-fleet-role"))
//            .withServiceRole("arn:aws:iam::199428496322:role/service-role/AWSBatchServiceRole");
//    var response = client.createComputeEnvironment(request);
//
//    System.out.println(response.getComputeEnvironmentName());
//
//  }


}
