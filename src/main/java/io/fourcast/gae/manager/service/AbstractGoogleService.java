package io.fourcast.gae.manager.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import io.fourcast.gae.util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.List;

/**
 * Created by nielsbuekers on 03/08/15.
 */
public class AbstractGoogleService {

    protected static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    protected static final JsonFactory JSON_FACTORY = new JacksonFactory();


    protected static GoogleCredential getCredentialsForScope(List<String> scope, boolean needsAdmin) throws Exception {


        InputStream is = AbstractGoogleService.class.getResourceAsStream(Config.getConfig().getServiceAccountPK12FilePath());
        PrivateKey key = null;
        try {
            key = SecurityUtils.loadPrivateKeyFromKeyStore(
                    SecurityUtils.getPkcs12KeyStore(), is, "notasecret", "privatekey", "notasecret");
        } catch (GeneralSecurityException ges) {
            throw new Exception("Service Account Credential security exception. " + ges.getLocalizedMessage());
        } catch (IOException e) {
            throw new Exception("Can't read PrivateKey file. " + e.getLocalizedMessage());
        }


        GoogleCredential.Builder builder = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(Config.getConfig().getServiceAccountEmail())
                .setServiceAccountScopes(scope)
                .setServiceAccountPrivateKey(key);

        if (needsAdmin) {
            builder.setServiceAccountUser(Config.getConfig().getDomainAdmin());
        }

        GoogleCredential credentials = builder.build();


        return credentials;
    }
}
