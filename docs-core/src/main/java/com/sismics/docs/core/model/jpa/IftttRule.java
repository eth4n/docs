package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * If-this-then-tha Rule.
 *
 * @author eth4n
 */
@Entity
@Table(name = "T_IFTTT_RULE")
public class IftttRule implements Loggable {
    /**
     * Route ID.
     */
    @Id
    @Column(name = "IFTTT_ID_C", length = 36)
    private String id;

    /**
     * Name.
     */
    @Column(name = "IFTTT_NAME_C", nullable = false, length = 50)
    private String name;


    /**
     * Rule data.
     * @see com.sismics.docs.core.util.ifttt.IftttRuleModel
     */
    @Column(name = "IFTTT_RULE_C", nullable = false, length = 5000)
    private String rule;

    /**
     * Creation date.
     */
    @Column(name = "IFTTT_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "IFTTT_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public IftttRule setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IftttRule setName(String name) {
        this.name = name;
        return this;
    }

    public String getRule() {
        return rule;
    }

    public IftttRule setRule(String rule) {
        this.rule = rule;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public IftttRule setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public IftttRule setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    @Override
    public String toMessage() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("rule", rule)
                .add("createDate", createDate)
                .toString();
    }


}
