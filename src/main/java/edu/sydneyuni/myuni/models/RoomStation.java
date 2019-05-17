package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.sydneyuni.myuni.models.labstats.StationStatus;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class RoomStation {

    private final String campus;
    private final String building;
    private final String room;
    private final int available;
    private final int busy;
    private final int offline;
    private final int availablePods;
    private final int busyPods;
    private final int offlinePods;
    private final List<Pod> pods;

    public static class Pod {

        private final int stationId;
        private final StationStatus stationStatus;

        @JsonCreator
        public Pod(@JsonProperty("stationId") int stationId, @JsonProperty("status") StationStatus stationStatus) {
            this.stationId = stationId;
            this.stationStatus = stationStatus;
        }

        public int getStationId() {
            return stationId;
        }

        @JsonProperty("status")
        public StationStatus getStationStatus() {
            return stationStatus;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pod pod = (Pod) o;
            return stationId == pod.stationId &&
                    stationStatus == pod.stationStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(stationId, stationStatus);
        }
    }

    @JsonCreator
    public RoomStation(@JsonProperty("campus") String campus,
                       @JsonProperty("building") String building,
                       @JsonProperty("room") String room,
                       @JsonProperty("available") int available,
                       @JsonProperty("busy") int busy,
                       @JsonProperty("offline") int offline,
                       @JsonProperty("availablePods") int availablePods,
                       @JsonProperty("busyPods") int busyPods,
                       @JsonProperty("offlinePods") int offlinePods,
                       @JsonProperty("pods") List<Pod> pods) {
        this.campus = campus;
        this.building = building;
        this.room = room;
        this.available = available;
        this.busy = busy;
        this.offline = offline;
        this.availablePods = availablePods;
        this.busyPods = busyPods;
        this.offlinePods = offlinePods;
        this.pods = pods;
    }

    public String getCampus() {
        return campus;
    }

    public String getBuilding() {
        return building;
    }

    public String getRoom() {
        return room;
    }

    public int getAvailable() {
        return available;
    }

    public int getBusy() {
        return busy;
    }

    public int getOffline() {
        return offline;
    }

    public int getAvailablePods() {
        return availablePods;
    }

    public int getBusyPods() {
        return busyPods;
    }

    public int getOfflinePods() {
        return offlinePods;
    }

    public List<Pod> getPods() {
        return pods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomStation that = (RoomStation) o;
        return available == that.available &&
                busy == that.busy &&
                offline == that.offline &&
                availablePods == that.availablePods &&
                busyPods == that.busyPods &&
                offlinePods == that.offlinePods &&
                Objects.equals(campus, that.campus) &&
                Objects.equals(building, that.building) &&
                Objects.equals(room, that.room) &&
                Objects.equals(pods, that.pods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(campus, building, room, available, busy, offline, availablePods, busyPods, offlinePods, pods);
    }
}
