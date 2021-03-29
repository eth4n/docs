package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * If-this-then-that Trigger.
 *
 * @author eth4n
 */
@Entity
@Table(name = "T_IFTTT_TRIGGER")
public class IftttTrigger {
    /**
     * Trigger ID.
     */
    @Id
    @Column(name = "IFTTTT_ID_C", length = 36)
    private String id;

    /**
     * Name.
     */
    @Column(name = "IFTTT_NAME_C", nullable = false, length = 50)
    private String name;


    public String getId() {
        return id;
    }

    public IftttTrigger setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IftttTrigger setName(String name) {
        this.name = name;
        return this;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }


}
