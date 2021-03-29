package com.sismics.docs.core.util.ifttt.condition;

import com.sismics.docs.core.util.ifttt.IftttContext;
import com.sismics.docs.core.util.ifttt.IftttRuleModel;
import com.sismics.docs.core.util.ifttt.condition.comparator.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract condition to compare multiple values with a specific comparator.
 *
 * Additional comparators may be registered in
 * @see #comparators
 *
 *
 */
public abstract class ComparingCondition extends AbstractCondition {
    public static final String DATA_KEY_COMPARATOR = "comparator";
    public static final String DATA_KEY_COMPARATOR_VALUE = "comparing_to";
    private static Map<ComparatorType, AbstractComparator> comparators = new HashMap<>();

    static {
        comparators.put(ComparatorType.MATCHES, new MatcherComparator());
        comparators.put(ComparatorType.NOT_MATCHES, new MatcherComparator());
        comparators.put(ComparatorType.CONTAINS, new ContainComparator());
        comparators.put(ComparatorType.NOT_CONTAINS, new ContainComparator());
        comparators.put(ComparatorType.IS_TRUE, new IsTrueComparator());
        comparators.put(ComparatorType.IS_FALSE, new IsFalseComparator());
    }

    /**
     * Returns a list of types this condition may provide to the comparator. Comparator implementation
     * and condition need to share at least one common type.
     *
     * @see AbstractComparator#consumes()
     * @param conditionData
     * @return
     */
    protected abstract List<Class> provides(IftttRuleModel.ConditionData conditionData);

    /**
     * Returns a value towards the comparator using given type. Data and execution context are given.
     *
     * @param type
     * @param conditionData
     * @param ctx
     * @param <T>
     * @return
     */
    public abstract <T> T getValue(Class<T> type, IftttRuleModel.ConditionData conditionData, IftttContext ctx);

    /**
     * Returns the comparator used in this condition
     *
     * @param conditionData
     * @return
     */
    public ComparatorType getComparator(IftttRuleModel.ConditionData conditionData) {
        Object type = getData(conditionData, DATA_KEY_COMPARATOR, null);
        if ( type == null || !(type instanceof String)) {
            return null;
        }
        return ComparatorType.valueOf((String)type);
    }

    /**
     * Returns the comparator's implementation
     *
     * @see #comparators
     * @param conditionData
     * @return
     */
    public  AbstractComparator getComparatorInstance(IftttRuleModel.ConditionData conditionData) {
        ComparatorType type = getComparator(conditionData);
        if ( type != null ) {
            return comparators.get(type);
        }
        return null;
    }

    /**
     * Returns the value to compare to (statically given by the data, thus persisted in the Ifttt rule)
     *
     * @param conditionData
     * @return
     */
    public Object getValueToCompareTo(IftttRuleModel.ConditionData conditionData) {
        return getData(conditionData, DATA_KEY_COMPARATOR_VALUE, null);
    }

    public void validate(IftttRuleModel.ConditionData conditionData) throws Exception {
        // Validate that this condition produces a context which is consumed by used comparator
        if ( getOverlappingEntries(provides(conditionData), getComparatorInstance(conditionData).consumes()).isEmpty()) {
            throw new Exception("Condition "+ getClass().getSimpleName() +" does not provide required data for comparator "+ getComparator(conditionData));
        }
    }
    private static List<Class> getOverlappingEntries(List<Class> a, List<Class> b) {
        return a.stream()
                .filter(b::contains)
                .collect(Collectors
                        .toList());
    }
}
