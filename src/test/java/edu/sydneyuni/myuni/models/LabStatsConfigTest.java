package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LabStatsConfigTest {

    public static LabStatsConfig generate() {
        LabStatsConfig config = new LabStatsConfig();
        config.put("Fisher Library", new LabStatsConfig.BuildingRooms());
        config.get("Fisher Library").put("Level 2", new LabStatsConfig.BuildingRooms.RoomStationGroups(new int[]{1, 2}, new int[]{3}));
        return config;
    }

    @Test
    void testJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LabStatsConfig config = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("labstats.json"), LabStatsConfig.class);
        assertNotNull(config);
        assertNotNull(config.get("Fisher Library"));
        assertNotNull(config.get("Fisher Library").get("Level 2"));
        assertNotNull(config.get("Fisher Library").get("Level 2").getPcGroups());
        assertNotNull(config.get("Fisher Library").get("Level 2").getPodGroups());
    }
}
