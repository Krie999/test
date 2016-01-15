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
public class UserDao extends AbstractDao<User>{

    /**
     *
     * @param userId
     * @return the Key for the passed ID.
     *
     * Since from everywhere, we need the userkey, make this public / static. Otherwise we always instantiate new UserDao's
     */
    public static Key<User> dsUserKey(String userId){
        Key<UserRoot> rootKey = Key.create(UserRoot.class, UserRoot.ID);
        return Key.create(rootKey,User.class,userId);
    }

    public void deleteAllUsers(){
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

    public User getUser(String userId) {
        Preconditions.checkNotNull(userId, "userId cannot be NULL");
        Key<User> k = createKey(userId);
        return ofy().load().key(k).now();
    }

    public List<User> getAllUsers() {
        return ofy()
                .load()
                .type(User.class)
                .ancestor(ancestor())
                .list();
    }

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

    public String saveUser(final User user) throws ConstraintViolationsException {
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

    public List<User> saveUsers(final List<User> remoteUsers) throws Exception{

        for(User user : remoteUsers){
            validate(user);
            user.setUserRoot(ancestor());
        }

        return ofy().transact(new Work<List<User>>() {

            @Override
            public List<User> run() {

                Map<Key<User>, User> userKeys = ofy().save().entities(remoteUsers).now();
                List<User> users = new ArrayList<User>();
                for(Key<User> stored : userKeys.keySet()){
                    users.add(userKeys.get(stored));
                }
                return users;
            }
        });
    }



    public List<User> getAllUsersWithRole(Globals.USER_ROLE role) {
        return ofy().load().type(User.class).filter("userRoles",role).list();
    }


    @Override
    public Key<UserRoot> ancestor() {
        return Key.create(UserRoot.class, UserRoot.ID);
    }

}
