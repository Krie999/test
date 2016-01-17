package io.fourcast.gae.service;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.manager.AuthManager;
import io.fourcast.gae.manager.UserManager;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;
import io.fourcast.gae.util.ServiceConstants;
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;
import io.fourcast.gae.util.exceptions.FCServerException;
import io.fourcast.gae.util.exceptions.FCUserException;

import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Created by nielsbuekers on 03/08/15.
 */

@Api(
        name = "userService",
        version = "v0.0.1",
        description = "Service to handle user requests",
        clientIds = {
                ServiceConstants.WEB_CLIENT_ID_DEV,
                ServiceConstants.WEB_CLIENT_ID_QA,
                ServiceConstants.WEB_CLIENT_ID_PROD,
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID}//when going live, remove this client id!
)
public class UserService extends AbstractService {

    private static UserManager userMgr = new UserManager();
    private static UserDao userDao = new UserDao();

    /**
     *
     * @param user the Cloud Endpoints authorized user (OAuth2.0 Google Account)
     * @return the domain user behind the email address that was logged in
     * @throws UnauthorizedException when a user tries to access the service w/o having proper access
     * @throws FCServerException
     * @throws ConstraintViolationsException there is a logical error in the data
     */
    @ApiMethod(name = "user")
    public User getUser(com.google.appengine.api.users.User user) throws UnauthorizedException, FCServerException, FCUserException {
        //only login access to app, can't fetch roles since roles users this service, inifinte loop.. TODO optimize
        user = AuthManager.validateUserLogin(user);

        User dsUser = userDao.getUserByEmail(user.getEmail());

        //user found in cache. now check if it's expired
        if (dsUser != null) {
            long now = new Date().getTime();
            if (now - dsUser.getLastChangeDate().getTime() > Globals.MAX_USER_DS_AGE) {
                //we won't lose the ID of the DS object since it's always the Google Apps ID,
                //so we can recreate the same Key manually.
                log.warning("cache for user " + dsUser.getEmail() + "expired");
                dsUser = null;
            }
        }

        //null if expired (set to null above) or not found in DS
        if (dsUser == null) {
            dsUser = userMgr.getRemoteUserDetails(user.getEmail());
            try {
                userDao.saveUser(dsUser);
            } catch (ConstraintViolationsException e) {
                log.warning(e.getLocalizedMessage());
                throw new FCUserException("Can't save the retrieved user.",e.getLocalizedMessage());
            }
        }

        return dsUser;
    }

    /**
     * List all users
     * @param user the OAuth user
     * @return all users in the application
     * @throws UnauthorizedException when a user tries to access the service w/o having proper access
     */
    @ApiMethod(name = "allUsers")
    public List<User> getAllUsers(com.google.appengine.api.users.User user) throws  UnauthorizedException {
        user = validateUser(user);
        List<User> users = userDao.getAllUsers();
        return users;
    }

    /**
     * List all users for a given role
     * @param user the OAuth user
     * @param role the role for which the users need to be listed
     * @return the users matching the role
     * @throws UnauthorizedException when a user tries to access the service w/o having proper access
     */
    @ApiMethod(name = "allUsersForRole")
    public List<User> getAllUsersForRole(com.google.appengine.api.users.User user, @Named("role") Globals.USER_ROLE role) throws  UnauthorizedException {
        //get all users with role
        user = validateUser(user);
        List<User> users = userDao.getAllUsersWithRole(role);
        Collections.sort(users);
        return users;
    }

    /**
     * Anyone can read
     * @return
     */
    @Override
    protected Globals.USER_ROLE requiredReadRole() {
        return Globals.USER_ROLE.ROLE_USER;
    }

    /**
     * no write operations should be performed
     * @return
     */
    @Override
    protected Globals.USER_ROLE requiredWriteRole() {
        return null;
    }
}
