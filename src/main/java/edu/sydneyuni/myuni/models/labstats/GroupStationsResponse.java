package edu.sydneyuni.myuni.models.labstats;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupStationsResponse {

    private final Station[] results;

    @JsonCreator
    public GroupStationsResponse(@JsonProperty("results") Station[] results) {
        this.results = results;
    }

    public Station[] getResults() {
        return results != null ? results : new Station[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupStationsResponse response = (GroupStationsResponse) o;
        return Arrays.equals(results, response.results);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(results);
    }
}
