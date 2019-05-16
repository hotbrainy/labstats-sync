package edu.sydneyuni.myuni;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class LabStatsSyncTest {

    private AmazonS3 s3Mock;
    private LabStatsSync lambda;
    private final String bucketName = "test";

    @BeforeEach
    void setUp() {
        s3Mock = Mockito.mock(AmazonS3.class);
        lambda = new LabStatsSync(s3Mock, bucketName, "sydney_uni");
    }

    @Test
    void testGetBucketKey() {
        String bucketKey = lambda.getBucketKey();
        assertNotNull(bucketKey);
    }

    @Test
    void testGetRoomStationsLabStats() throws IOException {
        LabStatsSync lambdaMock = mock(LabStatsSync.class);

        CloseableHttpClient clientMock = mock(CloseableHttpClient.class);
        CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
        HttpEntity httpEntityMock = mock(HttpEntity.class);
        when(httpEntityMock.getContent()).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ByteArrayInputStream(
                        new ObjectMapper().writeValueAsBytes(
                                new LabStatsGroupStatusResponse(10, 100, 1000)));
            }
        });
        when(responseMock.getEntity()).thenReturn(httpEntityMock);
        when(clientMock.execute(any(HttpUriRequest.class))).thenReturn(responseMock);
        when(lambdaMock.getClient()).thenReturn(clientMock);

        when(lambdaMock.getReader()).thenReturn(lambda.getReader());

        LabStatsConfig config = LabStatsConfigTest.generate();
        when(lambdaMock.getLabStatsRoomStations(config)).thenCallRealMethod();
        when(lambdaMock.getLabStatsGroupStatus(anyInt())).thenCallRealMethod();
        RoomStation[] ans = lambdaMock.getLabStatsRoomStations(config);

        assertEquals(1, ans.length);
    }

    @Test
    void testSyncRoomStationsS3() throws IOException {
        RoomStation[] arr = RoomStationTest.generateArray();
        String bucketKey = "test";
        StorageClass storageClass = StorageClass.StandardInfrequentAccess;
        lambda.syncRoomStationsS3(bucketKey, storageClass, arr);

        ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Mock).putObject(argument.capture());
        PutObjectRequest putObjectRequest = argument.getValue();

        // Testing the PutObjectRequest correct values.
        assertEquals(IOUtils.toString(putObjectRequest.getInputStream()), new ObjectMapper().writeValueAsString(arr));
        assertEquals(bucketName, putObjectRequest.getBucketName());
        assertEquals(bucketKey, putObjectRequest.getKey());
        assertEquals(storageClass.toString(), putObjectRequest.getStorageClass());
    }

    @Test
    void testHandleRequest() throws IOException {
        LabStatsSync lambdaMock = mock(LabStatsSync.class);

        RoomStation[] arr = RoomStationTest.generateArray();
        when(lambdaMock.getLabStatsRoomStations(lambdaMock.getConfig())).thenReturn(arr);

        String bucketKey = "test";
        when(lambdaMock.getBucketKey()).thenReturn(bucketKey);

        doCallRealMethod().when(lambdaMock).handleRequest(nullable(InputStream.class), nullable(OutputStream.class), nullable(Context.class));
        lambdaMock.handleRequest(null, null, null);

        // Testing it extracts LabStats once, uploads a glacier tier stations file and current stations file.
        verify(lambdaMock, times(1)).getLabStatsRoomStations(lambdaMock.getConfig());
        verify(lambdaMock, times(1)).syncRoomStationsS3(bucketKey, StorageClass.Glacier, arr);
        verify(lambdaMock, times(1)).syncRoomStationsS3("current", StorageClass.Standard, arr);
    }
}
