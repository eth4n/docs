package com.sismics.docs.core.util.ifttt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Definition of a Ifttt-Rule which will be stored in a serialized JSON form in the entity:
 * @see com.sismics.docs.core.model.jpa.IftttRule#rule
 *
 * A Ifttt rule may check for multiple conditions and if (and only if) all conditions are
 * <strong>true</strong>, all actions are executed.
 *
 * Rule execution is done in the util:
 * @see com.sismics.docs.core.util.IftttUtil#executeTrigger(Object)
 */
public class IftttRuleModel {

    private List<ConditionData> conditions = new ArrayList<>();
    private List<ActionData> actions = new ArrayList<>();

    public List<ConditionData> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionData> conditions) {
        this.conditions = conditions;
    }

    public List<ActionData> getActions() {
        return actions;
    }

    public void setActions(List<ActionData> actions) {
        this.actions = actions;
    }

    public static class ConditionData extends XData {
        private String condition;

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }

    public static class ActionData extends XData {
        private String action;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    public static abstract class XData {
        protected Map<String, Object> data;

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}
