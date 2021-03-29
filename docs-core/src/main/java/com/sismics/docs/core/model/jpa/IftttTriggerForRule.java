package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * If-this-then-that Trigger for Rule.
 *
 * @author eth4n
 */
@Entity
@Table(name = "T_IFTTT_RULE_TRIGGER")
public class IftttTriggerForRule {

    @Id
    @Column(name = "IFTTTRT_ID_C", length = 36)
    private String id;

    @Column(name = "IFTTT_ID_C", length = 36)
    private String ruleId;

    @Column(name = "IFTTTT_ID_C", length = 36)
    private String triggerId;

    public String getId() {
        return id;
    }

    public IftttTriggerForRule setId(String id) {
        this.id = id;
        return this;
    }

    public String getRuleId() {
        return ruleId;
    }

    public IftttTriggerForRule setRuleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public IftttTriggerForRule setTriggerId(String triggerId) {
        this.triggerId = triggerId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("ruleId", ruleId)
                .add("triggerId", triggerId)
                .toString();
    }


}
