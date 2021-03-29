package com.sismics.docs.core.util.ifttt;

import java.util.HashMap;
import java.util.Map;

/**
 * Execution context for a If-this-then-that rule. Context holds a variable map of
 * objects provided by the execution utility
 *
 * @see com.sismics.docs.core.util.IftttUtil#executeTrigger(Object)
 */
public class IftttContext {

    private String ruleId;
    private String ruleName;

    private Map<Class, Object> ctx = new HashMap<>();

    public Map<Class, Object> getCtx() {
        return ctx;
    }

    public <T> T getCtx(Class<T> key, T defaultValue) {
        if ( ctx.containsKey(key)) {
            return (T)ctx.get(key);
        }
        return defaultValue;
    }

    public void addToCtx(Class key, Object value) {
        ctx.put(key, value);
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}
