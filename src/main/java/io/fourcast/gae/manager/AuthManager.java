package io.fourcast.gae.manager;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.utils.SystemProperty;

import io.fourcast.gae.model.user.User;
import io.fourcast.gae.service.UserService;
import io.fourcast.gae.util.Globals;

/**
 * Created by nielsbuekers on 05/08/15.
 */
public class AuthManager {

    public static final UserService userService= new UserService();

    public static com.google.appengine.api.users.User validateUser(com.google.appengine.api.users.User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("Please login before making requests.");
        }

        //if on development, we're stuck with example@example.com.. modify here
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development
                && user.getEmail().equalsIgnoreCase("example@example.com")) {
            return new com.google.appengine.api.users.User(Globals.LOCAL_DEV_USER,"fourcast.io","114265751743224928536");
        }

        if(!user.getEmail().endsWith("@bnpparibasfortis.com")){
            throw new OAuthRequestException("Please login with your fourcast.ioaccount.");
        }

        return user;
    }


    public static boolean validateUserAccess(com.google.appengine.api.users.User user, Globals.USER_ROLE role) throws UnauthorizedException{
        User e2eUser = null;
        try {
            e2eUser = userService.getUserDetails(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnauthorizedException("No permission for " + role.getRole());
        }
        return e2eUser.getUserRoles().contains(role);
    }
}
