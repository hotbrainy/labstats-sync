package edu.sydneyuni.myuni.models.labstats;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StationStatus {

    @JsonProperty("in_use")
    IN_USE,
    @JsonProperty("powered_on")
    POWERED_ON,
    @JsonProperty("offline")
    OFFLINE;
}
