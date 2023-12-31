package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class USydCampusesTest {

    public static USydCampuses generate() {
        USydCampuses config = new USydCampuses();
        config.put("Camperdown Campus", new USydCampuses.CampusBuildings());
        config.get("Camperdown Campus").put("F03 - Fisher Library", new USydCampuses.CampusBuildings.BuildingRooms());
        config.get("Camperdown Campus").get("F03 - Fisher Library").put("Level 2", new USydCampuses.CampusBuildings.BuildingRooms.RoomStationGroups(new int[]{1215, 1216}, new int[]{1220}));
        return config;
    }

    @Test
    void testJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        USydCampuses config = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("labstats.json"), USydCampuses.class);
        assertNotNull(config);
        assertNotNull(config.get("Camperdown / Darlington Campus"));
        assertNotNull(config.get("Camperdown / Darlington Campus").get("F03 - Fisher Library"));
        assertNotNull(config.get("Camperdown / Darlington Campus").get("F03 - Fisher Library").get("Level 2"));
        assertNotNull(config.get("Camperdown / Darlington Campus").get("F03 - Fisher Library").get("Level 2").getPcGroups());
        assertNotNull(config.get("Camperdown / Darlington Campus").get("F03 - Fisher Library").get("Level 2").getPodGroups());
    }
}
