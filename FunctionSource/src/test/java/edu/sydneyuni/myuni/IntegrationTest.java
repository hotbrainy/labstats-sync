package edu.sydneyuni.myuni;

import cloud.localstack.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.RoomStation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {"s3"})
class IntegrationTest {

    @Test
    void testIntegration() throws IOException {
        String labStatsAPIKey = System.getenv("LABSTATS_API_KEY");
        if (StringUtils.isBlank(labStatsAPIKey)) {
            // Don't run this test on every CI. //TODO: Mock LabStats response.
            return;
            //throw new IllegalArgumentException("No LABSTATS_API_KEY");
        }
        final AmazonS3 s3 = TestUtils.getClientS3();
        String bucketName = "test";
        assertNotNull(s3.createBucket(bucketName));

        AmazonS3 s3Mock = mock(AmazonS3.class);
        // Glacier Storage class not supported.
        when(s3Mock.putObject(any(PutObjectRequest.class))).thenAnswer(new Answer<PutObjectResult>() {
            @Override
            public PutObjectResult answer(InvocationOnMock invocationOnMock) {
                PutObjectRequest request = invocationOnMock.getArgument(0);
                if (request.getStorageClass().equals(StorageClass.Glacier.toString())) {
                    request.setStorageClass(StorageClass.Standard);
                }
                return s3.putObject(request);
            }
        });

        LabStatsSync labStatsSync = new LabStatsSync(s3Mock, bucketName, labStatsAPIKey);
        labStatsSync.handleRequest(null, null, null);

        verify(s3Mock, times(2)).putObject(any(PutObjectRequest.class));

        S3Object object = s3.getObject(bucketName, "current");
        assertNotNull(object);

        ObjectMapper objectMapper = new ObjectMapper();
        // Check JSON is RoomStation[]
        RoomStation[] arr = objectMapper.readValue(object.getObjectContent(), RoomStation[].class);
        assertNotNull(arr);

        RoomStationsQuery query = new RoomStationsQuery(s3, bucketName);
        try (PipedInputStream inputStream = new PipedInputStream(); PipedOutputStream outputStream = new PipedOutputStream(inputStream)) {
            query.handleRequest(null, outputStream, null);

            RoomStation[] ans = query.getReader().readValue(inputStream);
            // Check Query returns RoomStation[]
            assertArrayEquals(arr, ans);
        }
    }
}
