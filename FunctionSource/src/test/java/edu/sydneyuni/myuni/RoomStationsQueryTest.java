package edu.sydneyuni.myuni;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.RoomStationTest;
import edu.sydneyuni.myuni.services.RoomStationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

public class RoomStationsQueryTest {

    private RoomStationDao daoMock;
    private RoomStationsQuery lambda;

    @BeforeEach
    void setUp() {
        daoMock = Mockito.mock(RoomStationDao.class);
        lambda = new RoomStationsQuery(daoMock);
    }

    @Test
    void testHandleRequest() throws IOException {
        RoomStation[] arr = RoomStationTest.generateArray();
        when(daoMock.getLatestRoomStations()).thenReturn(arr);
        when(daoMock.getWriter()).thenReturn(new ObjectMapper().writerFor(RoomStation[].class));

        try (PipedInputStream inputStream = new PipedInputStream(); PipedOutputStream outputStream = new PipedOutputStream(inputStream)) {
            lambda.handleRequest(null, outputStream, null);

            verify(daoMock, times(1)).getLatestRoomStations();

            RoomStation[] ans = new ObjectMapper().readerFor(RoomStation[].class).readValue(inputStream);
            assertArrayEquals(arr, ans);
        }
    }
}
