package io.fourcast.gae.manager;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

import io.fourcast.gae.model.user.User;
import io.fourcast.gae.service.UserService;
import io.fourcast.gae.util.Globals;

/**
 * Created by nielsbuekers on 05/08/15.
 */
public class AuthManager {

  public static final UserService userService = new UserService();
  static com.google.appengine.api.users.UserService gaeUserService = UserServiceFactory.getUserService();

  /**
   * Validates if the logged in user is from the correct domain. Workaround for localhost
   *
   * @return the validated OAuth user, possibly mutated for localhost development
   * @throws UnauthorizedException when the user has no access to the system (i.e. does not belong to the domain).
   */
  public static com.google.appengine.api.users.User validateUserLogin() throws UnauthorizedException {

    if (!gaeUserService.isUserLoggedIn()) {
      throw new UnauthorizedException("Please login before making requests.");
    }

    //if on development, we're stuck with example@example.com.. modify here
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development && gaeUserService.getCurrentUser().getEmail().equalsIgnoreCase("test@example.com")) {
      return new com.google.appengine.api.users.User(Globals.LOCAL_DEV_USER, Globals.ALLOWED_DOMAIN, "114265751743224928536");
    }

    return gaeUserService.getCurrentUser();
  }


  /**
   * Validates if the OAuth User has the given role
   *
   * @param role the role to check for
   * @return true if the user has access to that role, false if not.
   * @throws UnauthorizedException
   */
  public static boolean validateUserRole(Globals.USER_ROLE role) throws UnauthorizedException {

    User dsUser;
    try {
      //TODO Robin kan dit mooier evt? meteen Dao gebruiken ipv via de service? Risico dan op expired cache data. de service checkt daar wel op.. kweet t niet goed.
      dsUser = userService.getUser();
    } catch (Exception e) {
      e.printStackTrace();
      throw new UnauthorizedException("No permission for " + role.getRole());
    }
    return dsUser.getUserRoles().contains(role);
  }
}
