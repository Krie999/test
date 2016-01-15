package io.fourcast.gae.dao;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import io.fourcast.gae.model.root.UserRoot;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nielsbuekers on 03/08/15.
 */
public class UserDao extends AbstractDao<User> {

    /**
     * retrieve a user with the given ID {name}.
     *
     * @param userId The ID matches the GoogleApps ID and is not a datastore generated id.
     *               It's a String, DS 'name' property, not an actual 'id' in DS terms.
     * @return the user with the given id. Null if no user is found
     */
    public User getUser(String userId) {
        Preconditions.checkNotNull(userId, "userId cannot be NULL");
        Key<User> k = createKey(userId);
        return ofy().load().key(k).now();
    }

    /**
     * retrieve all users
     *
     * @return a list of all users. An empty list if there are no users
     */
    public List<User> getAllUsers() {
        return ofy()
                .load()
                .type(User.class)
                .ancestor(ancestor())
                .list();
    }

    /**
     * deletes all domain users from the datastore
     */
    public void deleteAllUsers() {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                List<Key<User>> userKeys = ofy()
                        .load()
                        .type(User.class)
                        .ancestor(ancestor())
                        .keys()
                        .list();
                ofy().delete().keys(userKeys);
            }
        });
    }

    /**
     * Find a user based on an email address.
     *
     * @param userEmail the email address for which to find the user. Note that there is no unique DS constraint
     *                  based on the user email address, only on the GApps User ID. ({name} in the User Key<>)
     * @return the domain user for the email address.
     */
    public User getUserByEmail(String userEmail) {
        Preconditions.checkNotNull(userEmail, "userEmail cannot be NULL");
        return ofy()
                .load()
                .type(User.class)
                .filter("email", userEmail)
                .ancestor(ancestor())
                .first()
                .now();
    }


    /**
     * Save the domain user. The Key should be based on the Gapps ID, and therefore always filled. Validate checks this.
     *
     * @param user the domain user to save
     * @return the saved domain user
     * @throws ConstraintViolationsException when the user object is not properly constructed
     */
    public String saveUser(final User user) throws ConstraintViolationsException {
        Preconditions.checkNotNull(user, "user cannot be NULL");

        validate(user);

        return ofy().transact(new Work<String>() {

            @Override
            public String run() {
                user.setUserRoot(ancestor());
                ofy().save().entity(user).now();
                return user.getId();
            }
        });
    }

    /**
     * Batch save a list of users. Similar requiremens as saveUser
     *
     * @param users the users to save
     * @return the list of saved domain users.
     * @throws ConstraintViolationsException when the user object is not properly constructed
     */
    public List<User> saveUsers(final List<User> users) throws ConstraintViolationsException {

        for (User user : users) {
            validate(users);
            user.setUserRoot(ancestor());
        }

        return ofy().transact(new Work<List<User>>() {

            @Override
            public List<User> run() {

                Map<Key<User>, User> userKeys = ofy().save().entities(users).now();
                List<User> users = new ArrayList<>();
                for (Key<User> stored : userKeys.keySet()) {
                    users.add(userKeys.get(stored));
                }
                return users;
            }
        });
    }

    /**
     * retrieves the list of all users that have a certain role
     *
     * @param role the role for which to retrieve the users
     * @return the list of all users that match the given role
     */
    public List<User> getAllUsersWithRole(Globals.USER_ROLE role) {
        return ofy().load().type(User.class).filter("userRoles", role).list();
    }

    /**
     * Get the Key for a given userId. This way we don't need to fetch from DS and then retrieve the Key.
     * Can't use createKey() nor ancestor() since it's a static method
     * Since from everywhere, we need the userkey, make this public / static.
     * Otherwise we always instantiate new UserDao's just to create a Key. Used in transformers.
     *
     * @param userId
     * @return the Key for the passed ID.
     */
    public static Key<User> dsUserKey(String userId) {
        Preconditions.checkNotNull(userId, "userId cannot be NULL");
        Key<UserRoot> rootKey = Key.create(UserRoot.class, UserRoot.ID);
        return Key.create(rootKey, User.class, userId);
    }

    /**
     * Retrieve the ancestor key. Used by the generic method 'query', amongst others.
     *
     * @return the common ancestor key for the Entity kind 'User'.
     */
    @Override
    public Key<UserRoot> ancestor() {
        return Key.create(UserRoot.class, UserRoot.ID);
    }
}
