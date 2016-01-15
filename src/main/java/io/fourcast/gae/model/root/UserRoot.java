package io.fourcast.gae.model.root;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;

/**
 * Created by nbuekers on 11/01/16.
 */
@Entity
public class UserRoot implements Serializable {
    public final static String ID = "USER_ROOT";

    @Id
    String id = ID;
}