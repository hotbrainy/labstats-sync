package edu.sydneyuni.myuni.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.USydCampuses;
import edu.sydneyuni.myuni.models.labstats.GroupStatusResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LabStatsClient {

    private final String labStatsApiKey;
    private final CloseableHttpClient client;
    private final ObjectMapper reader;

    public LabStatsClient(String labStatsApiKey) {
        this(labStatsApiKey, HttpClientBuilder.create()
                .setDefaultHeaders(Collections.singletonList(
                        new BasicHeader("Authorization", labStatsApiKey))).build());
    }

    LabStatsClient(String labStatsApiKey, CloseableHttpClient client) {
        this.labStatsApiKey = labStatsApiKey;
        if (labStatsApiKey == null || labStatsApiKey.length() == 0) {
            throw new IllegalArgumentException("No LabStats LABSTATS_API_KEY specified");
        }
        this.client = client;
        reader = new ObjectMapper();
    }

    public RoomStation[] getLabStatsRoomStations(USydCampuses uSydCampuses) throws IOException {
        List<RoomStation> roomStations = new ArrayList<>();
        for (Map.Entry<String, USydCampuses.CampusBuildings> campus : uSydCampuses.entrySet()) {
            for (Map.Entry<String, USydCampuses.CampusBuildings.BuildingRooms> building : campus.getValue().entrySet()) {
                for (Map.Entry<String, USydCampuses.CampusBuildings.BuildingRooms.RoomStationGroups> room : building.getValue().entrySet()) {
                    int on = 0;
                    int busy = 0;
                    int offline = 0;
                    for (int pcGroup : room.getValue().getPcGroups()) {
                        GroupStatusResponse response = getLabStatsGroupStatus(pcGroup);
                        on += response.getOn();
                        busy += response.getBusy();
                        offline += response.getOffline();
                    }
                    List<RoomStation.Pod> pods = new ArrayList<>();
                    int onPods = 0;
                    int busyPods = 0;
                    int offlinePods = 0;
                    for (int podGroup : room.getValue().getPodGroups()) {
                        GroupStatusResponse response = getLabStatsGroupStatus(podGroup);
                        onPods += response.getOn();
                        busyPods += response.getBusy();
                        offlinePods += response.getOffline();
                    }
                    roomStations.add(new RoomStation(campus.getKey(), building.getKey(), room.getKey(), on, busy, offline,
                            onPods, busyPods, offlinePods, pods));
                }
            }
        }
        return roomStations.toArray(new RoomStation[0]);
    }

    private GroupStatusResponse getLabStatsGroupStatus(int groupId) throws IOException {
        String uri = String.format("https://sea-api.labstats.com/groups/%d/status", groupId);
        try (CloseableHttpResponse response = getClient().execute(new HttpGet(uri))) {
            return getReader().readValue(response.getEntity().getContent(), GroupStatusResponse.class);
        } catch (Exception e) {
            throw new IOException("Error getting LabStats GET /" + uri, e);
        }
    }

    public String getLabStatsApiKey() {
        return labStatsApiKey;
    }

    private CloseableHttpClient getClient() {
        return client;
    }

    private ObjectMapper getReader() {
        return reader;
    }
}
