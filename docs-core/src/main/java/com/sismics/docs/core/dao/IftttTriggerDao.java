package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.IftttRule;
import com.sismics.docs.core.model.jpa.IftttTrigger;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Trigger for If-this-then-that DAO.
 *
 */
public class IftttTriggerDao {

    public String create(IftttTrigger trigger, String userId) {
        // Create the UUID
        trigger.setId(UUID.randomUUID().toString());

        // Create the entity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(trigger);

        return trigger.getId();
    }


    public IftttTrigger getTrigger(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select t from IftttTrigger t where t.id = :id");
        q.setParameter("id", id);
        try {
            return (IftttTrigger) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public IftttTrigger getTriggerByName(String name) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select t from IftttTrigger t where t.name LIKE :name");
        q.setParameter("name", name);
        try {
            return (IftttTrigger) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
