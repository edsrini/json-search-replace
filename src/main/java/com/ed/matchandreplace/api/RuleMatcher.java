package com.ed.matchandreplace.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;


import java.util.regex.Pattern;

public class RuleMatcher {
    private Type type;
    private String path;
    private String value;
    private Pattern pattern;

    public RuleMatcher() {
    }

    public RuleMatcher(Type type, String path, String value) {
        this.type = type;
        this.path = path;
        this.value = value;
        if( type == Type.REGEX ) {
            pattern = Pattern.compile(value);
        }
    }

    @JsonProperty
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @JsonProperty
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean match(JsonNode jsonNode) {
        JsonNode node = jsonNode.at(path);
        if( ! ( node instanceof MissingNode ) ) {
            switch (type) {
                case STRING:
                    return value.equals(node.asText());
                case REGEX:
                    if( pattern == null ) {
                        pattern = Pattern.compile(value);
                    }
                    return pattern.matcher(node.asText()).find();
            }
        }
        return false;
    }

    public enum Type {
        STRING, REGEX
    }
}
