package edu.sydneyuni.myuni.models.labstat;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.labstats.StationStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StationStatusTest {

    @Test
    void testJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StationStatus status = objectMapper.readValue("\"in_use\"", StationStatus.class);
        assertEquals(StationStatus.IN_USE, status);
        status = objectMapper.readValue("\"powered_on\"", StationStatus.class);
        assertEquals(StationStatus.POWERED_ON, status);
        status = objectMapper.readValue("\"offline\"", StationStatus.class);
        assertEquals(StationStatus.OFFLINE, status);
    }
}
