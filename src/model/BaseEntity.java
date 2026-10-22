package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * BaseEntity provides common identifier and auditing fields.
 * Demonstrates Abstraction and Inheritance across the domain model.
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Integer id;
    protected Timestamp createdAt;
    protected Timestamp updatedAt;

    public BaseEntity() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public BaseEntity(Integer id) {
        this();
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
