package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.IftttRule;
import com.sismics.docs.core.model.jpa.IftttTriggerForRule;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

/**
 * Relation between If-this-then-that rules and trigger
 *
 */
public class IftttTriggerForRuleDao {

    public String create(IftttTriggerForRule trigger) {
        // Create the UUID
        trigger.setId(UUID.randomUUID().toString());

        // Create the entity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(trigger);

        return trigger.getId();
    }

    public void deleteTriggerForRule(String ruleId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();


        em.createQuery("DELETE FROM IftttTriggerForRule  tr WHERE tr.ruleId = :ruleId")
                .setParameter("ruleId", ruleId)
                .executeUpdate();
    }
}
