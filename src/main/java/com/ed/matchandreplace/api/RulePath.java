package com.ed.matchandreplace.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RulePath {
    private String newValue;
    private String group;
    private String path;

    public RulePath() {
    }

    public RulePath(String newValue, String group, String path) {
        this.newValue = newValue;
        this.group = group;
        this.path = path;
    }

    @JsonProperty
    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @JsonProperty
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
