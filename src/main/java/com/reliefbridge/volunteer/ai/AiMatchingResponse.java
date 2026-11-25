package com.reliefbridge.volunteer.ai;

import java.util.ArrayList;
import java.util.List;

public class AiMatchingResponse {

    private String model;
    private Integer total_results;
    private List<AiMatchingResult> results = new ArrayList<>();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTotal_results() {
        return total_results;
    }

    public void setTotal_results(Integer total_results) {
        this.total_results = total_results;
    }

    public List<AiMatchingResult> getResults() {
        return results;
    }

    public void setResults(List<AiMatchingResult> results) {
        this.results = results;
    }
}