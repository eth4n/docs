package com.sismics.docs.core.util.ifttt.condition;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;

import java.util.Set;

/**
 * Abstract condition for the If-this-then-that rules.
 *
 * Each condition needs to define one or more required trigger classes. Trigger classes may be events
 * fired from the system. For each event received by the IftttUtil, the conditions are check. Parent
 * classes can be used for triggers.
 *
 * If a rule defines multiple conditions, the conditions need to share a common trigger.
 *
 * Conditions are registered in the utility:
 * @see com.sismics.docs.core.util.IftttUtil#conditions
 */
public abstract class AbstractCondition {

    /**
     * Returns a set of triggers this condition can use to work on. Trigger may be system events OR
     * their parent classes. Each trigger in the set can be used for execution
     *
     * @param conditionData
     * @return
     */
    public abstract Set<Class> getTriggers(IftttRuleModel.ConditionData conditionData);

    /**
     * Main method to check the condition against data and execution context.
     *
     * @param conditionData
     * @param ctx
     * @return
     */
    public abstract ConditionResult getConditionResult(IftttRuleModel.ConditionData conditionData, IftttContext ctx);

    /**
     * Validation method before the If-this-then-that rule is saved. Use this method to check
     * if data is correct and usable.
     *
     * @see com.sismics.docs.core.util.IftttUtil#validateModel(IftttRuleModel)
     * @param conditionData
     * @throws Exception
     */
    public abstract void validate(IftttRuleModel.ConditionData conditionData) throws Exception;

    /**
     * Easy access method to extract data with default value from the condition's data.
     *
     * @param conditionData
     * @param key
     * @param defaultValue
     * @return
     */
    public Object getData(IftttRuleModel.ConditionData conditionData, String key, Object defaultValue) {
        if ( conditionData.getData().containsKey(key)) {
            return conditionData.getData().get(key);
        }
        return defaultValue;
    }

}
