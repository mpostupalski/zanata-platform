package org.zanata.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Superclass which holds time based entity. The idea is to have a record with
 * history in same database table with same entityId. Record can be traced with
 * history by using validFrom and validTo to determine the order.
 *
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public class TimeEntityBase implements Serializable {

    private static final long serialVersionUID = 4099946057081739374L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(columnDefinition = "char(36)")
    private String entityId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @NotNull
    private Date validFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    private Date validTo;

    public Long getId() {
        return this.id;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public Date getValidFrom() {
        return new Date(validFrom.getTime());
    }

    public Date getValidTo() {
        return validTo == null ? null : new Date(validTo.getTime());
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public void setValidFrom(final Date validFrom) {
        this.validFrom = new Date(validFrom.getTime());
    }

    public void setValidTo(final Date validTo) {
        this.validTo = new Date(validTo.getTime());
    }
}
