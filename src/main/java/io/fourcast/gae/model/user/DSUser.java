package io.fourcast.gae.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import io.fourcast.gae.util.Globals;
import io.fourcast.gae.model.root.DSUserRoot;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nielsbuekers on 03/08/15.
 */

@Entity
@Unindex
public class DSUser implements Comparable<DSUser> {


    @Parent
    private Key<DSUserRoot> userRoot;

    @Id
    @NotNull
    //we use the Google Account ID as the OFY ID to save the user, so it must be filled at all times
    private String id;

    @Index
    @NotNull
    private String email;
    @NotNull
    private String displayName;

    @Index
    private List<Globals.USER_ROLE> userRoles;

    @Index
    private Boolean active;

    private Date lastChangeDate;


    public String toString() {
        return "id:" + id + " - display name:" + displayName + " - email:" + email;
    }


    public void addRole(Globals.USER_ROLE role) {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        userRoles.add(role);
    }

    public void clearRoles() {
        this.userRoles = new ArrayList<>();
    }

    @OnSave
    void updateChangeDate() {
        this.lastChangeDate = new Date();
    }


    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public List<Globals.USER_ROLE> getUserRoles() {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        return userRoles;
    }

    public void setUserRoles(List<Globals.USER_ROLE> userRoles) {
        this.userRoles = userRoles;
    }

    public Date getLastChangeDate() {
        return lastChangeDate;
    }

    public void setLastChangeDate(Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }


    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<DSUserRoot> getUserRoot() {
        return userRoot;
    }

    public void setUserRoot(Key<DSUserRoot> userRoot) {
        this.userRoot = userRoot;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(DSUser o) {
        return this.getEmail().compareTo(o.getEmail());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DSUser dsUser = (DSUser) o;

        if (userRoot != null ? !userRoot.equals(dsUser.userRoot) : dsUser.userRoot != null) return false;
        if (id != null ? !id.equals(dsUser.id) : dsUser.id != null) return false;
        if (email != null ? !email.equals(dsUser.email) : dsUser.email != null) return false;
        if (displayName != null ? !displayName.equals(dsUser.displayName) : dsUser.displayName != null) return false;
        if (userRoles != null ? !userRoles.equals(dsUser.userRoles) : dsUser.userRoles != null) return false;
        if (active != null ? !active.equals(dsUser.active) : dsUser.active != null) return false;
        return !(lastChangeDate != null ? !lastChangeDate.equals(dsUser.lastChangeDate) : dsUser.lastChangeDate != null);

    }

    @Override
    public int hashCode() {
        int result = userRoot != null ? userRoot.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (userRoles != null ? userRoles.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (lastChangeDate != null ? lastChangeDate.hashCode() : 0);
        return result;
    }
}
