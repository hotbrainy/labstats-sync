package edu.sydneyuni.myuni.models.labstat;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.labstats.GroupStationsResponse;
import edu.sydneyuni.myuni.models.labstats.Station;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupStationsResponseTest {

    static GroupStationsResponse generate() {
        return new GroupStationsResponse(new Station[]{new Station(1), new Station(2)});
    }

    @Test
    void testJackson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        GroupStationsResponse response = generate();
        byte[] json = objectMapper.writeValueAsBytes(response);
        GroupStationsResponse ans = objectMapper.readValue(json, GroupStationsResponse.class);
        assertEquals(response, ans);
        assertArrayEquals(response.getResults(), ans.getResults());
        assertEquals(new Station(1), response.getResults()[0]);
    }
}
