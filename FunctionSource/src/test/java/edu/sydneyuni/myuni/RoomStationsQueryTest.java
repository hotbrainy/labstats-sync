package edu.sydneyuni.myuni;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.RoomStationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

class RoomStationsQueryTest {

    private final String bucketName = "test";
    private AmazonS3 s3Mock;
    private RoomStationsQuery lambda;

    @BeforeEach
    void setUp() {
        s3Mock = Mockito.mock(AmazonS3.class);
        lambda = new RoomStationsQuery(s3Mock, bucketName);
    }

    @Test
    void testGetRoomStationsS3() throws IOException {
        RoomStation[] arr = RoomStationTest.generateArray();

        S3Object object = new S3Object();
        object.setObjectContent(new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(arr)));
        when(s3Mock.getObject(bucketName, "current")).thenReturn(object);

        RoomStation[] ans = lambda.getRoomStationsS3();
        assertArrayEquals(arr, ans);

        verify(s3Mock, times(1)).getObject(anyString(), anyString());
    }

    @Test
    void testHandleRequest() throws IOException {
        RoomStationsQuery lambdaMock = mock(RoomStationsQuery.class);

        RoomStation[] arr = RoomStationTest.generateArray();
        when(lambdaMock.getRoomStationsS3()).thenReturn(arr);

        when(lambdaMock.getWriter()).thenReturn(lambda.getWriter());

        doCallRealMethod().when(lambdaMock).handleRequest(nullable(InputStream.class), any(OutputStream.class), nullable(Context.class));
        try (PipedInputStream inputStream = new PipedInputStream(); PipedOutputStream outputStream = new PipedOutputStream(inputStream)) {
            lambdaMock.handleRequest(null, outputStream, null);

            verify(lambdaMock, times(1)).getRoomStationsS3();

            RoomStation[] ans = lambda.getReader().readValue(inputStream);
            assertArrayEquals(arr, ans);
        }
    }
}
