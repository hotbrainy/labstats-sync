package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class RoomStation {

    private final String building;
    private final String room;
    private final int available;
    private final int busy;
    private final int offline;

    @JsonCreator
    public RoomStation(@JsonProperty("building") String building,
                       @JsonProperty("room") String room,
                       @JsonProperty("available") int available,
                       @JsonProperty("busy") int busy,
                       @JsonProperty("offline") int offline) {
        this.building = building;
        this.room = room;
        this.available = available;
        this.busy = busy;
        this.offline = offline;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomStation that = (RoomStation) o;
        return available == that.available &&
                busy == that.busy &&
                offline == that.offline &&
                Objects.equals(building, that.building) &&
                Objects.equals(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, room, available, busy, offline);
    }
}
