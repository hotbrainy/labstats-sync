package edu.sydneyuni.myuni.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LabStatsGroupStatusResponse {

    private final int offline;
    private final int on;
    private final int busy;

    @JsonCreator
    public LabStatsGroupStatusResponse(@JsonProperty("offline_count") int offline,
                                       @JsonProperty("powered_on") int on,
                                       @JsonProperty("in_use_count") int busy) {
        this.offline = offline;
        this.on = on;
        this.busy = busy;
    }

    public int getOffline() {
        return offline;
    }

    public int getOn() {
        return on;
    }

    public int getBusy() {
        return busy;
    }
}
