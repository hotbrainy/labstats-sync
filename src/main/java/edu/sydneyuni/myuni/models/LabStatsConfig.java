package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class LabStatsConfig extends HashMap<String, LabStatsConfig.BuildingRooms> {

    public static class BuildingRooms extends HashMap<String, BuildingRooms.RoomStationGroups> {

        public static class RoomStationGroups {

            final int[] pcGroups;
            final int[] podGroups;

            @JsonCreator
            public RoomStationGroups(@JsonProperty("pc") int[] pcGroups, @JsonProperty("pods") int[] podGroups) {
                this.pcGroups = pcGroups == null ? new int[0] : pcGroups;
                this.podGroups = podGroups == null ? new int[0] : podGroups;
            }

            public int[] getPcGroups() {
                return pcGroups;
            }

            public int[] getPodGroups() {
                return podGroups;
            }
        }
    }
}
