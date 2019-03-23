package com.ed.matchandreplace.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Rule {
    private final List<RuleMatcher> matchers = new ArrayList<>();
    private final List<RulePath> paths = new ArrayList<>();

    public Rule() {
    }

    public Rule(RuleMatcher ruleMatcher, List<RulePath> paths) {
        matchers.add(ruleMatcher);
        this.paths.addAll(paths);
    }

    public Rule(RuleMatcher ruleMatcher, RulePath... paths) {
        this(ruleMatcher, Arrays.asList(paths));
    }

    public boolean matches(JsonNode node) {
        for (RuleMatcher matcher : matchers) {
            if( ! matcher.match(node) ) {
                return false;
            }
        }
        return true;
    }

    @JsonProperty
    public List<RuleMatcher> getMatchers() {
        return matchers;
    }

    @JsonProperty
    public List<RulePath> getPaths() {
        return paths;
    }

    public void setPaths(List<RulePath> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
    }

    public static List<Rule> createList(RuleMatcher ruleMatcher, RulePath... paths) {
        return Collections.singletonList(new Rule(ruleMatcher,paths));
    }
}
