package com.sismics.docs.core.util.ifttt.condition.comparator;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.condition.ComparingCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator to implement MATCHES and NOT_MATCHES to check if a value
 * matches a given regex
 *
 * Requires a string as input and a regex to match against. Returns a MatcherConditionResult
 * to provide matching groups to follow-up actions
 *
 * @see MatcherConditionResult
 */
public class MatcherComparator extends AbstractComparator {
private static final Logger log = LoggerFactory.getLogger(MatcherComparator.class);
    private static List<Class> consumes = new ArrayList<>();
    static {
        consumes.add(String.class);
    }
    @Override
    public List<Class> consumes() {
        return consumes;
    }

    @Override
    public MatcherConditionResult compare(ComparingCondition condition, IftttRuleModel.ConditionData conditionData, IftttContext ctx) {
        ComparatorType type = condition.getComparator(conditionData);
        MatcherConditionResult result = new MatcherConditionResult();
        String comparingValue = condition.getValue(String.class, conditionData, ctx);
        String regex = (String)condition.getValueToCompareTo(conditionData);

        if ( comparingValue != null && regex != null ) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(comparingValue);

            boolean matcherFound = matcher.find();

            log.debug("Comparing value (length: " + comparingValue.length() +") with regex '"+ regex +"', comparator: "+ type);
            if (type == ComparatorType.MATCHES && matcherFound) {
                result.setResult(true);
                // copy matching groups
                for (int i = 0; i < matcher.groupCount(); i++) {
                    log.debug("Providing matched value group "+  +(i+1) +": "+ matcher.group(i+1));
                    result.getResultGroups().add(matcher.group(i+1));
                }
            } else if (type == ComparatorType.NOT_MATCHES && !matcherFound) {
                result.setResult(true);
            }
        } else if ( regex != null && type == ComparatorType.NOT_MATCHES) {
            log.debug("Comparing null value with regex '"+ regex +"', comparator: "+ type +": Always true");
            result.setResult(true);
        }
        return result;
    }
}
