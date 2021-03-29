package com.sismics.docs.core.util.ifttt.action;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;

import java.util.Set;

/**
 * Abstract action class for If-This-Then-That rules.
 *
 * An action can require certain values from the execution context. This is defined by the consume()
 * method. After each condition in a rule is validated to true, all actions are called.
 *
 * Actions do hold data (defined in the rule) to provide more data to the action on what to do.
 * Data and context together provide all required information for the action to be executed.
 *
 * Actions are registered in the utility:
 * @see com.sismics.docs.core.util.IftttUtil#actions
 */
public abstract class AbstractAction {
    /**
     * Returns a list of classes this action needs to function (instances of these classes).
     * If multiple classes are returned, only one needs to be given.
     * Required classes should match action triggers (like events) which will be added by the rule
     * execution.
     *
     * @see com.sismics.docs.core.util.IftttUtil#validateModel(IftttRuleModel)
     * @param actionData
     * @return
     */
    public abstract Set<Class> consumes(IftttRuleModel.ActionData actionData);

    /**
     * Main method for processing the action. Data and context are given and contain all required
     * data.
     * @see com.sismics.docs.core.util.IftttUtil#executeTrigger(Object)
     * @param actionData
     * @param ctx
     */
    public abstract void process(IftttRuleModel.ActionData actionData, IftttContext ctx);

    /**
     * Validation method before the If-this-then-that rule is saved. Use this method to check
     * if data is correct and usable.
     *
     * @see com.sismics.docs.core.util.IftttUtil#validateModel(IftttRuleModel)
     * @param actionData
     * @throws Exception
     */
    public void validate(IftttRuleModel.ActionData actionData) throws Exception {
        // NOP
    }

    /**
     * Easy access method to extract data with default value from the action's data.
     *
     * @param actionData
     * @param key
     * @param defaultValue
     * @return
     */
    public Object getData(IftttRuleModel.ActionData actionData, String key, Object defaultValue) {
        if ( actionData.getData().containsKey(key)) {
            return actionData.getData().get(key);
        }
        return defaultValue;
    }

}
