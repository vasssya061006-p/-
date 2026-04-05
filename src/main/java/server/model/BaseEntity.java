package server.model;

import java.io.Serializable;

/**
 * Abstract base class for all entities in the education system.
 * Provides common ID field and implements Serializable for network transmission.
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected int id;
    
    public BaseEntity() {
    }
    
    public BaseEntity(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
