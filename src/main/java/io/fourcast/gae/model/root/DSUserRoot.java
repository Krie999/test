package io.fourcast.gae.model.root;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;

/**
 * Created by nbuekers on 11/01/16.
 */
@SuppressWarnings("serial")
@Entity
public class DSUserRoot extends GenericRoot implements Serializable {
    @SuppressWarnings("nls")
    public final static String ID = "DS_USER_ROOT";

    @Id
    String id = ID;

    public String ID() {
        return id;
    }
}