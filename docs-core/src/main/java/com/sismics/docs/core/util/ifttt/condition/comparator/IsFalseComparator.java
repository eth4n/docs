package com.sismics.docs.core.util.ifttt.condition.comparator;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.condition.ComparingCondition;
import com.sismics.docs.core.util.ifttt.condition.ConditionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Comparator to implement IS_FALSE to check if a boolean value is false (or null - not given).
 */
public class IsFalseComparator extends AbstractComparator {
    private static final Logger log = LoggerFactory.getLogger(IsFalseComparator.class);
    private static List<Class> consumes = new ArrayList<>();

    static {
        consumes.add(Boolean.class);
    }

    @Override
    public List<Class> consumes() {
        return consumes;
    }

    @Override
    public ConditionResult compare(ComparingCondition condition, IftttRuleModel.ConditionData conditionData, IftttContext ctx) {
        ConditionResult result = new ConditionResult();

        Boolean booleanValue = condition.getValue(Boolean.class, conditionData, ctx);

        if (booleanValue == null || !booleanValue) {
            result.setResult(true);
        }
        return result;
    }
}
