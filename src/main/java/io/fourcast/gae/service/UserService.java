package io.fourcast.gae.service;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.services.admin.directory.Directory;
import com.google.appengine.api.oauth.OAuthRequestException;
import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.manager.AuthManager;
import io.fourcast.gae.manager.UserManager;
import io.fourcast.gae.manager.service.GoogleDirectoryService;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;
import io.fourcast.gae.util.ServiceConstants;

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
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID}
)
public class UserService extends AbstractService {

    private Directory directoryService;
    private static UserManager userMgr = new UserManager();
    private static UserDao userDao = new UserDao();

    @ApiMethod(name = "userDetails")
    public User getUserDetails(com.google.appengine.api.users.User user) throws Exception {
        //only validate login, no roles
        user = AuthManager.validateUser(user);

        User dsUser = userDao.getUserByEmail(user.getEmail());

        //found in cache. now check if it's expired
        if (dsUser != null) {
            long now = new Date().getTime();
            if (now - dsUser.getLastChangeDate().getTime() > Globals.MAX_USER_DS_AGE) {
                //we won't lose the ID of the DS object since it's always the Google Apps ID,
                //so we can recreate the same Key.
                log.warning("cache for user " + dsUser.getEmail() + "expired");
                dsUser = null;
            }
        }

        //null if expired or if not found in DS
        if (dsUser == null) {

            dsUser = userMgr.getRemoteUserDetails(user.getEmail());
            userDao.saveUser(dsUser);
        }
        return dsUser;
    }

    @ApiMethod(name = "allUsers")
    public List<User> getAllUsers(com.google.appengine.api.users.User user) throws OAuthRequestException, UnauthorizedException {
        user = validateUserAccess(user);
        List<User> users = userDao.getAllUsers();
        return users;
    }

    @ApiMethod(name = "allUsersForRole")
    public List<User> getAllUsersForRole(com.google.appengine.api.users.User user, @Named("role")Globals.USER_ROLE role) throws OAuthRequestException, UnauthorizedException {
        //get all users users. depending on boolean, only the ones for the same user's brand.
        user = validateUserAccess(user);
        List<User> users = userDao.getAllUsersWithRole(role);
        Collections.sort(users);
        return users;
    }


    @SuppressWarnings("unused")
	private Directory getDirectoryService() {
        if (directoryService == null) {
            directoryService = GoogleDirectoryService.getDirectoryService();
        }
        return directoryService;
    }

    @Override
    protected Globals.USER_ROLE requiredRole() {
        return Globals.USER_ROLE.ROLE_USER;
    }
}
