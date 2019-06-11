package edu.sydneyuni.myuni;

import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.RoomStationTest;
import edu.sydneyuni.myuni.services.LabStatsClient;
import edu.sydneyuni.myuni.services.RoomStationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class LabStatsSyncTest {

    private RoomStationDao daoMock;
    private LabStatsClient labStatsClientMock;
    private LabStatsSync lambda;

    @BeforeEach
    void setUp() {
        daoMock = Mockito.mock(RoomStationDao.class);
        labStatsClientMock = Mockito.mock(LabStatsClient.class);
        lambda = new LabStatsSync(daoMock, labStatsClientMock);
    }

    @Test
    void testHandleRequest() throws IOException {
        // Testing it extracts LabStats once, uploads a glacier tier stations file and current stations file.
        RoomStation[] arr = RoomStationTest.generateArray();
        when(labStatsClientMock.getLabStatsRoomStations(lambda.getUSydCampuses())).thenReturn(arr);
        lambda.handleRequest(null, null, null);
        verify(labStatsClientMock, times(1)).getLabStatsRoomStations(lambda.getUSydCampuses());
        verify(daoMock, times(1)).insert(arr);
    }
}
