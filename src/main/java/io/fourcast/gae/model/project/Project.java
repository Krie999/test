package io.fourcast.gae.model.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;

import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.model.transformer.ProjectTransformer;
import io.fourcast.gae.model.user.User;

@Entity
@SuppressWarnings("serial")
@ApiTransformer(ProjectTransformer.class)
public class Project extends DSEntry implements Serializable {

    public enum PROJECT_STATUS {
        ACTIVE,
        CLOSED,
        DELETED
    }

    /**
     * linked entities
     **/
    private List<Long> subProjectIds;

    private Ref<User> owner;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (subProjectIds != null ? !subProjectIds.equals(project.subProjectIds) : project.subProjectIds != null)
            return false;
        return !(owner != null ? !owner.equals(project.owner) : project.owner != null);

    }

    @Override
    public int hashCode() {
        int result = subProjectIds != null ? subProjectIds.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }
}