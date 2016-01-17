package io.fourcast.gae.service;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import io.fourcast.gae.manager.AuthManager;
import io.fourcast.gae.util.Globals;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nielsbuekers on 03/08/15.
 */
public abstract class AbstractService {

    final Logger log = Logger.getLogger(AbstractService.class.getName());

    /**
     * The role required to read from the implementing service.
     *
     * @return The ADMIN role
     */
    protected abstract Globals.USER_ROLE requiredReadRole();

    /**
     * The role required to write from the implementing service.
     *
     * @return The ADMIN role
     */
    protected abstract Globals.USER_ROLE requiredWriteRole();


    /**
     * Validates if the user has access to the application and for the role determined by the implementation method
     * of requiredReadRole().
     *
     * @param user the OAuth user for which access and role has to be validated
     * @return the domain user in case he has access
     * @throws OAuthRequestException
     * @throws UnauthorizedException
     */

    User validateUser(User user, boolean needsWrite) throws UnauthorizedException {

        // check that the user is logged in and on the correct domain
        user = AuthManager.validateUserLogin(user);
        Globals.USER_ROLE requiredRole = needsWrite ? requiredWriteRole() : requiredReadRole();

        boolean hasPermissions = AuthManager.validateUserRole(user, requiredRole);

        if (!hasPermissions) {
            throw new UnauthorizedException("You don't have sufficient permissions. Required role is " + requiredRole);
        }
        return user;
    }

    /**
     * similar as above, defaults to only needs read access
     *
     * @param user
     * @return
     * @throws UnauthorizedException
     */
    User validateUser(User user) throws UnauthorizedException {
        return validateUser(user, false);
    }


    /**
     * TODO implement memcache?
     * <p/>
     * Get the memcacheService
     */
    protected MemcacheService mc() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        return syncCache;
    }
}
