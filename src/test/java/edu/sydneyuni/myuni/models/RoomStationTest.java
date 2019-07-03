package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.labstats.StationStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoomStationTest {

    static RoomStation generate() {
        return new RoomStation("Camperdown", "Fisher Library", "Level 2", 10, 289, 1, 2, 3, 0, Collections.singletonList(new RoomStation.Pod(0, StationStatus.POWERED_ON)));
    }

    @Test
    void testJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RoomStation s = generate();
        byte[] json = objectMapper.writeValueAsBytes(s);
        RoomStation ans = objectMapper.readValue(json, RoomStation.class);
        assertEquals(s, ans);
        assertEquals(s.getCampus(), ans.getCampus());
        assertEquals(s.getBuilding(), ans.getBuilding());
        assertEquals(s.getRoom(), ans.getRoom());
        assertEquals(s.getAvailable(), ans.getAvailable());
        assertEquals(s.getBusy(), ans.getBusy());
        assertEquals(s.getOffline(), ans.getOffline());
        assertEquals(s.getAvailablePods(), ans.getAvailablePods());
        assertEquals(s.getBusyPods(), ans.getBusyPods());
        assertEquals(s.getOfflinePods(), ans.getOfflinePods());
        assertEquals(s.getPods(), ans.getPods());
    }

    public static RoomStation[] generateArray() {
        return new RoomStation[]{generate()};
    }

    @Test
    void testArrayJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RoomStation[] arr = generateArray();
        byte[] json = objectMapper.writeValueAsBytes(arr);

        // System.out.println(new String(json));
        RoomStation[] ans = objectMapper.readValue(json, RoomStation[].class);
        assertArrayEquals(arr, ans);
    }
}
