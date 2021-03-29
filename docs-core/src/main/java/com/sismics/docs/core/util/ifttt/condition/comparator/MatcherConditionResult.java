package com.sismics.docs.core.util.ifttt.condition.comparator;

import com.sismics.docs.core.util.ifttt.condition.ConditionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * More specific condition result to hold matching groups (e.g. for regex matching)
 */
public class MatcherConditionResult extends ConditionResult {

    private List<String> resultGroups = new ArrayList<>();

    public List<String> getResultGroups() {
        return resultGroups;
    }

    public void setResultGroups(List<String> resultGroups) {
        this.resultGroups = resultGroups;
    }
}
