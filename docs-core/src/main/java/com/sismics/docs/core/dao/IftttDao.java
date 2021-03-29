package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.IftttRule;
import com.sismics.docs.core.model.jpa.RouteModel;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * If-this-then-that Rule DAO.
 *
 */
public class IftttDao {
    /**
     * Creates a new rule for if-this-then-that.
     *
     * @param rule IftttRule
     * @param userId User ID
     * @return New ID
     */
    public String create(IftttRule rule, String userId) {
        // Create the UUID
        rule.setId(UUID.randomUUID().toString());

        // Create the rule
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        rule.setCreateDate(new Date());
        em.persist(rule);

        // Create audit log
        AuditLogUtil.create(rule, AuditLogType.CREATE, userId);

        return rule.getId();
    }

    /**
     * Update a if-this-then-that rule model.
     *
     * @param rule Rule model to update
     * @param userId User ID
     * @return Updated rule model
     */
    public IftttRule update(IftttRule rule, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the rule model
        Query q = em.createQuery("select r from IftttRule r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", rule.getId());
        IftttRule ruleModelDb = (IftttRule) q.getSingleResult();

        // Update the route model
        ruleModelDb.setName(rule.getName());
        ruleModelDb.setRule(rule.getRule());

        // Create audit log
        AuditLogUtil.create(ruleModelDb, AuditLogType.UPDATE, userId);

        return ruleModelDb;
    }

    /**
     * Deletes a rule
     *
     * @param ruleId Rule ID
     * @param userId User ID
     */
    public void deleteRule(String ruleId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Create audit log
        IftttRule rule = em.find(IftttRule.class, ruleId);
        AuditLogUtil.create(rule, AuditLogType.DELETE, userId);

        em.createNativeQuery("update T_IFTTT_RULE r set IFTTT_DELETEDATE_D = :dateNow where r.IFTTT_ID_C = :ruleId and r.IFTTT_DELETEDATE_D is null")
                .setParameter("ruleId", ruleId)
                .setParameter("dateNow", new Date())
                .executeUpdate();
    }

    /**
     * Returns all rules listening on one or more of given triggers.
     *
     * @param trigger
     * @return
     */
    public List<IftttRule> getActiveByTrigger(List<String> trigger) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select distinct r from IftttRule r where r.deleteDate is null AND r.id IN(SELECT tr.ruleId FROM IftttTriggerForRule tr JOIN IftttTrigger t ON tr.triggerId = t.id WHERE t.name IN :trigger) ");
        q.setParameter("trigger", trigger);
        return q.getResultList();
    }

    /**
     * Gets an active rule model by its ID.
     *
     * @param id Rule model ID
     * @return Rule model
     */
    public IftttRule getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from IftttRule r where r.id = :id and r.deleteDate is null");
            q.setParameter("id", id);
            return (IftttRule) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns the list of all rules.
     *
     * @return List of rules
     */
    public List<IftttRule> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from IftttRule r where r.deleteDate is null ORDER BY r.name ASC");
        return q.getResultList();
    }
}
