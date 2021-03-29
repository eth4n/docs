package com.sismics.docs.core.util.ifttt.condition.comparator;

/**
 * List of possible comparators.
 *
 * Implementations are defined in
 * @see com.sismics.docs.core.util.ifttt.condition.ComparingCondition#comparators
 */
public enum ComparatorType {
    MATCHES,
    NOT_MATCHES,
    CONTAINS,
    NOT_CONTAINS,
    IS_TRUE,
    IS_FALSE
}
