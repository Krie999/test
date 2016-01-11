package io.fourcast.gae.model.smart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;

import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.model.transformer.SmartProjectTransformer;
import io.fourcast.gae.model.user.DSUser;

@Entity
@SuppressWarnings("serial")
@ApiTransformer(SmartProjectTransformer.class)
public class SmartProject extends DSEntry implements Serializable {

    public enum PROJECT_STATUS {
        ACTIVE,
        CLOSED,
        DELETED
    }

    /**
     * linked entities
     **/
    private List<Long> subProjectIds;

    private Ref<DSUser> owner;


    public void addSubProjectId(Long subProjectId) {
        if (this.subProjectIds == null) {
            this.subProjectIds = new ArrayList<Long>();
        }

        //dont add twice
        if (!subProjectIds.contains(subProjectId)) {
            this.subProjectIds.add(subProjectId);
        }
    }

    public Ref<DSUser> getOwner() {
        return owner;
    }

    public void setOwner(Ref<DSUser> owner) {
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

}