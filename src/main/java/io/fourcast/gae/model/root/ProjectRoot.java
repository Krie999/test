package io.fourcast.gae.model.root;

import com.googlecode.objectify.annotation.Id;

/**
 * Created by nbuekers on 11/01/16.
 */
public class ProjectRoot {
    @SuppressWarnings("nls")
    public final static String ID = "PROJECT_ROOT";

    @Id
    String id = ID;

    public String ID() {
        return id;
    }
}
