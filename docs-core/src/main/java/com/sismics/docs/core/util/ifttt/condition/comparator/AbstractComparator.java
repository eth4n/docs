package com.sismics.docs.core.util.ifttt.condition.comparator;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.condition.ComparingCondition;
import com.sismics.docs.core.util.ifttt.condition.ConditionResult;

import java.util.List;

/**
 * Abstract comparator for a comparing condition. Each comparator works on certain input type(s) which
 * are defined by the consumes() method. Consumed types and provided types need to have at least one
 * common value:
 * @see ComparingCondition#validate(IftttRuleModel.ConditionData)
 */
public abstract class AbstractComparator{
    /**
     * Returns a list of possible types this comparator can work on.
     * @return
     */
    public abstract List<Class> consumes();

    /**
     * Comparing the condition value with this comparator and returns the comparing result.
     * May also return a more specific return value by extending ConditionResult.
     *
     * @param condition
     * @param conditionData
     * @param ctx
     * @return
     */
    public abstract ConditionResult compare(ComparingCondition condition, IftttRuleModel.ConditionData conditionData, IftttContext ctx);

}
