package io.fourcast.gae.model.root;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by nbuekers on 11/01/16.
 */
@Entity
public class ProjectRoot {

    public final static String ID = "PROJECT_ROOT";
    @Id
    String id = ID;

}
