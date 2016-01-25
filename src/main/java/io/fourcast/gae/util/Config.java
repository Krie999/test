package io.fourcast.gae.util;

import com.google.gson.Gson;
import java.io.*;

/**
 * Created by nbuekers on 11/01/16.
 */
public class Config implements Serializable {

    private static Config instance = null;
    protected Config() {
        // Exists only to defeat instantiation.
    }

    public static Config getConfig() {
        if(instance == null) {
            Gson gson = new Gson();
            try {
                //read from JSON
                InputStream is = Config.class.getResourceAsStream("/config/config.json");
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                //convert the json string back to object
                instance = gson.fromJson(reader, Config.class);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }


    private String serviceAccountEmail;
    private String serviceAccountPK12FilePath;
    private String appengineAppName;
    private String domainAdmin;

    public static Config getInstance() {
        return instance;
    }

    public String getServiceAccountEmail() {
        return serviceAccountEmail;
    }

    public String getServiceAccountPK12FilePath() {
        return serviceAccountPK12FilePath;
    }

    public String getAppengineAppName() {
        return appengineAppName;
    }

    public String getDomainAdmin() {
        return domainAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        if (serviceAccountEmail != null ? !serviceAccountEmail.equals(config.serviceAccountEmail) : config.serviceAccountEmail != null)
            return false;
        if (serviceAccountPK12FilePath != null ? !serviceAccountPK12FilePath.equals(config.serviceAccountPK12FilePath) : config.serviceAccountPK12FilePath != null)
            return false;
        if (appengineAppName != null ? !appengineAppName.equals(config.appengineAppName) : config.appengineAppName != null)
            return false;
        return !(domainAdmin != null ? !domainAdmin.equals(config.domainAdmin) : config.domainAdmin != null);

    }

    @Override
    public int hashCode() {
        int result = serviceAccountEmail != null ? serviceAccountEmail.hashCode() : 0;
        result = 31 * result + (serviceAccountPK12FilePath != null ? serviceAccountPK12FilePath.hashCode() : 0);
        result = 31 * result + (appengineAppName != null ? appengineAppName.hashCode() : 0);
        result = 31 * result + (domainAdmin != null ? domainAdmin.hashCode() : 0);
        return result;
    }
}
