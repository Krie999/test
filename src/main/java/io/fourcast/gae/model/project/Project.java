package io.fourcast.gae.model.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;

import com.googlecode.objectify.annotation.Index;
import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.model.transformer.ProjectTransformer;
import io.fourcast.gae.model.user.User;

@Entity
@Cache
@ApiTransformer(ProjectTransformer.class)
public class Project extends DSEntry implements Serializable {

    @Index
    public enum PROJECT_STATUS {
        ACTIVE,
        CLOSED,
        DELETED
    }

    /**
     * linked entities
     **/
    @Index
    private List<Long> subProjectIds;

    @Index
    private PROJECT_STATUS status;

    @Index
    private Ref<User> owner;

    private String name;


    public void addSubProjectId(Long subProjectId) {
        if (this.subProjectIds == null) {
            this.subProjectIds = new ArrayList<Long>();
        }

        //dont add twice
        if (!subProjectIds.contains(subProjectId)) {
            this.subProjectIds.add(subProjectId);
        }
    }

    public Ref<User> getOwner() {
        return owner;
    }

    public void setOwner(Ref<User> owner) {
        this.owner = owner;
    }

    public List<Long> getSubProjectIds() {
        return this.subProjectIds;
    }

    //FE passes list of IDs that need to be stored as refs.
    //do it with ref since that's easier to create from a generated key (id passed from FE)
    //vs otherwise fetching the details form the DS just to set the list of refs it here.
    public void setSubProjectIds(List<Long> subProjects) {
        this.subProjectIds = subProjects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PROJECT_STATUS getStatus() {
        return status;
    }

    public void setStatus(PROJECT_STATUS status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Project project = (Project) o;

        if (subProjectIds != null ? !subProjectIds.equals(project.subProjectIds) : project.subProjectIds != null)
            return false;
        if (status != project.status) return false;
        if (owner != null ? !owner.equals(project.owner) : project.owner != null) return false;
        return !(name != null ? !name.equals(project.name) : project.name != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (subProjectIds != null ? subProjectIds.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}