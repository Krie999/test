package io.fourcast.gae.manager.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import io.fourcast.gae.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nielsbuekers on 03/08/15.
 */

public class GoogleDirectoryService extends AbstractGoogleService {

    private static Directory service;

    public static Directory getDirectoryService() {

        if (service == null) {
            List<String> scope = new ArrayList<>();
            scope.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER_READONLY);
            scope.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY);
            scope.add(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);

            GoogleCredential credentials = null;
            try {
                credentials = getCredentialsForScope(scope, true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                    .setApplicationName(Config.getConfig().getAppengineAppName())
                    .build();
        }

        return service;
    }
}
