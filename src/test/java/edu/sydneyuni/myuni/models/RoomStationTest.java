package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoomStationTest {

    public static RoomStation generate() {
        return new RoomStation("Fisher Library", "Level 2", 10, 289, 1);
    }
    @Test
    void testJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RoomStation s = generate();
        byte[] json = objectMapper.writeValueAsBytes(s);
        RoomStation ans = objectMapper.readValue(json, RoomStation.class);
        assertEquals(s, ans);
        assertEquals(s.getBuilding(), ans.getBuilding());
        assertEquals(s.getRoom(), ans.getRoom());
        assertEquals(s.getAvailable(), ans.getAvailable());
        assertEquals(s.getBusy(), ans.getBusy());
        assertEquals(s.getOffline(), ans.getOffline());
    }

    public static RoomStation[] generateArray() {
        return new RoomStation[] {generate()};
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
