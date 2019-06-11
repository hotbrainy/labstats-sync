package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class USydCampuses extends HashMap<String, USydCampuses.CampusBuildings> {

    public static class CampusBuildings extends HashMap<String, CampusBuildings.BuildingRooms> {
        public static class BuildingRooms extends HashMap<String, BuildingRooms.RoomStationGroups> {
            public static class RoomStationGroups {

                final int[] pcGroups;
                final int[] podGroups;

                @JsonCreator
                RoomStationGroups(@JsonProperty("pc") int[] pcGroups, @JsonProperty("pods") int[] podGroups) {
                    this.pcGroups = pcGroups;
                    this.podGroups = podGroups;
                }

                public int[] getPcGroups() {
                    return pcGroups != null ? pcGroups : new int[0];
                }

                public int[] getPodGroups() {
                    return podGroups != null ? podGroups : new int[0];
                }
            }
        }
    }


}
