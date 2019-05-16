package edu.sydneyuni.myuni;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.RoomStationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LabStatsSyncTest {

    private AmazonS3 s3Mock;
    private LabStatsSync lambda;
    private final String bucketName = "test";
    private final String customerId = "sydney_uni";

    @BeforeEach
    void setUp() {
        s3Mock = Mockito.mock(AmazonS3.class);
        lambda = new LabStatsSync(s3Mock, bucketName, customerId);
    }

    @Test
    void testGetBucketKey() {
        String bucketKey = lambda.getBucketKey();
        assertNotNull(bucketKey);
    }

    @Test
    void testGetRoomStationsLabStats() throws IOException {
        LabStatsSync lambdaMock = mock(LabStatsSync.class);

        when(lambdaMock.getRoomStationsLabStats(ArgumentMatchers.<String>anyList())).thenCallRealMethod();

        RoomStation s = RoomStationTest.generate();
        when(lambdaMock.getRoomStationsLabStats(anyString())).thenReturn(s);

        // Test every public data set is fetched.
        RoomStation[] ans = lambdaMock.getRoomStationsLabStats(Arrays.asList("test", "test1"));
        verify(lambdaMock, times(2)).getRoomStationsLabStats(anyString());
        assertEquals(2, ans.length);
        assertArrayEquals(new RoomStation[]{s, s}, ans);
    }

    // TODO: Test transform of LabStats response to RoomStation.

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

        List<String> publicDataSets = Collections.singletonList("test");
        when(lambdaMock.getPublicDataSets()).thenReturn(publicDataSets);

        RoomStation[] arr = RoomStationTest.generateArray();
        when(lambdaMock.getRoomStationsLabStats(publicDataSets)).thenReturn(arr);

        String bucketKey = "test";
        when(lambdaMock.getBucketKey()).thenReturn(bucketKey);

        doCallRealMethod().when(lambdaMock).handleRequest(nullable(InputStream.class), nullable(OutputStream.class), nullable(Context.class));
        lambdaMock.handleRequest(null, null, null);

        // Testing it extracts LabStats once, uploads a glacier tier stations file and current stations file.
        verify(lambdaMock, times(1)).getRoomStationsLabStats(publicDataSets);
        verify(lambdaMock, times(1)).syncRoomStationsS3(bucketKey, StorageClass.Glacier, arr);
        verify(lambdaMock, times(1)).syncRoomStationsS3("current", StorageClass.Standard, arr);
    }
}
