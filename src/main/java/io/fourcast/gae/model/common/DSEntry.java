package io.fourcast.gae.model.common;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@SuppressWarnings({"serial"})
@Cache
@Entity
public class DSEntry implements Serializable {
    /* auto generate ID */
    @Id
    private Long id;

    /* Parent is a non-existing ROOT class to ensure strong consistency */
    @Parent
    private Key<?> rootDSEntry;

    /* the hierarchical parent, not the DS parent.
    Having only 1 parent and not tied to the DS Key
    avoids having to save a LARGE top-top-top-parent entity
    or having to pass all parent-id's to regenerate the ancestor key. */
    @Index
    @NotNull
    private Long parentId = 0L;


    @Index
    private Date creationDate = new Date();

    @Index
    private Date lastModified = new Date();

    @Index
    private Boolean active = true;

    @OnSave
    void updateChangeDate() {
        this.lastModified = new Date();
    }

    @OnSave
    void generateCreationDate() {
        if (id == 0L) this.creationDate = new Date();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<?> getRootDSEntry() {
        return rootDSEntry;
    }

    public void setRootDSEntry(Key<?> rootDSEntry) {
        this.rootDSEntry = rootDSEntry;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }


    public DSEntry() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}
