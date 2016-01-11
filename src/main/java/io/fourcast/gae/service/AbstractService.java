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

    //the required role for the specific service
    protected abstract Globals.USER_ROLE requiredRole();

    // return needed for dev so that we don't get stuck with example@exmaple.com
    // but can lists.listService.js with dev@bnpparibasfortis.com
    User validateUserAccess(User user) throws OAuthRequestException, UnauthorizedException {

        //check that the user is logged in + on BNP domain
        user = AuthManager.validateUser(user);

        //check his roles --> uses the uSerService getUserDetails to get the roles
        boolean hasPermissions = AuthManager.validateUserAccess(user, requiredRole());
        if (!hasPermissions) {
            throw new UnauthorizedException("You don't have sufficient permissions");
        }
        return user;
    }

    /**
     * Get the memcacheService
     */
    protected MemcacheService mc() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        return syncCache;
    }
}
