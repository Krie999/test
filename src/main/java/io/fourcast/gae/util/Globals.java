package io.fourcast.gae.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nbuekers on 11/01/16.
 */
public class Globals {

    /**
     * AUTH
     */
    public static final String LOCAL_DEV_USER = "dev";
    public static final String ALLOWED_DOMAIN = "fourcast.io";

    /**
     * USER
     */
    //common role for all users --> for actions not related to app, only login purposes
    public static final String GAPPS_ROLE_USER = "ROLE_USER";
    //todo only need one of these 2?
    public static final String GAPPS_GROUP_ALL = "zz_dev_users@fourcast.io";

    //user roles - match Google Groups
    public static final String GAPPS_GROUP_PROJECTOWNER = "zz_dev_crud@fourcast.io";
    public static final String GAPPS_GROUP_ADMIN = "zz_dev_admin@fourcast.io";


    public static final long MAX_USER_DS_AGE = 60 * 60 * 1000; //1 hour in milliseconds
    public static final long MAX_GROUP_MEMBERSHIP__CACHE_AGE = 30*60*1000; // 30 minutes in milliseconds

    public static final SimpleDateFormat DATE_FORMAT_WITH_TIME = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");


    public enum USER_ROLE {
        ROLE_USER(GAPPS_ROLE_USER), //can login into the application
        ROLE_PROJECT_OWNER(GAPPS_GROUP_PROJECTOWNER), //has access to projects
        ROLE_ADMIN(GAPPS_GROUP_ADMIN); //admin functionalities

        // Reverse-lookup map for getting a USER_ROLE from an abbreviation
        private static final Map<String, USER_ROLE> lookup = new HashMap<String, USER_ROLE>();

        //create reverse-lookup hashmap
        static {
            for (USER_ROLE d : USER_ROLE.values()) {
                lookup.put(d.getRole(), d);
            }
        }

        private final String role;

        USER_ROLE(String role) {
            this.role = role;
        }

        //used to map google group to a role.
        public static USER_ROLE fromEmail(String email) {
            return lookup.get(email);
        }

        public String getRole() {
            return role;
        }

    }
}
