package com.sismics.docs.core.util.ifttt.condition.comparator;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.condition.ComparingCondition;
import com.sismics.docs.core.util.ifttt.condition.ConditionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator to implement CONTAINS and NOT_CONTAINS to check if a value
 * is in a given list or not at all.
 *
 * Requires a string array as input and a regex to match against each value.
 */
public class ContainComparator extends AbstractComparator {
private static final Logger log = LoggerFactory.getLogger(ContainComparator.class);
    private static List<Class> consumes = new ArrayList<>();
    static {
        consumes.add(String[].class);
    }
    @Override
    public List<Class> consumes() {
        return consumes;
    }

    @Override
    public ConditionResult compare(ComparingCondition condition, IftttRuleModel.ConditionData conditionData, IftttContext ctx) {
        ComparatorType type = condition.getComparator(conditionData);
        ConditionResult result = new ConditionResult();

        // Get list of tags (tag names) from condition
        String[] comparingValues = condition.getValue(String[].class, conditionData, ctx);
        String regex = (String)condition.getValueToCompareTo(conditionData);

        if ( comparingValues != null && regex != null ) {
            Pattern pattern = Pattern.compile(regex);

            for(int i=0; i<comparingValues.length; i++) {
                Matcher matcher = pattern.matcher(comparingValues[i]);
                boolean matcherFound = matcher.find();

                if ( matcherFound ) {
                    log.debug("Regex '"+ regex +"' matched one of the given list ("+ comparingValues.length +" values)");
                    if (type == ComparatorType.CONTAINS) {
                        result.setResult(true);
                        return result;
                    } else if (type == ComparatorType.NOT_CONTAINS) {
                        result.setResult(false);
                        return result;
                    }
                }
            }
            log.debug("Regex '"+ regex +"' matched none of the given list ("+ comparingValues.length +" values)");
            if (type == ComparatorType.NOT_CONTAINS ) {
                result.setResult(true);
            }
        } else if ( regex != null && type == ComparatorType.NOT_CONTAINS) {
            log.debug("Comparing null value with regex '"+ regex +"', comparator: "+ type +": Always true");
            result.setResult(true);
        }
        return result;
    }
}
