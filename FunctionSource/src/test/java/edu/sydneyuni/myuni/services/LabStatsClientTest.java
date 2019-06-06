package edu.sydneyuni.myuni.services;

import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.USydCampusesTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LabStatsClientTest {

    @Test
    void testGetRoomStations() throws IOException {
        String labStatsAPIKey = System.getenv("LABSTATS_API_KEY");
        if (StringUtils.isBlank(labStatsAPIKey)) {
            // Don't run this test on every CI. //TODO: Mock LabStats response.
            return;
        }

        LabStatsClient labStatsClient = new LabStatsClient(labStatsAPIKey);
        RoomStation[] roomStations = labStatsClient.getLabStatsRoomStations(USydCampusesTest.generate());
        assertNotNull(roomStations);
        assertTrue(roomStations.length > 0);
    }
}
