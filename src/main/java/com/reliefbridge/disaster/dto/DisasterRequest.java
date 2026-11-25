package com.reliefbridge.disaster.dto;

import com.reliefbridge.disaster.entity.DisasterType;
import com.reliefbridge.disaster.entity.Severity;

public class DisasterRequest {

    private String title;

    private String description;

    private DisasterType disasterType;

    private String location;

    private Severity severity;

    public DisasterRequest() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DisasterType getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(DisasterType disasterType) {
        this.disasterType = disasterType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

}