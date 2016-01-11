package io.fourcast.gae.dao;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import io.fourcast.gae.model.root.DSUserRoot;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.util.Globals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nielsbuekers on 03/08/15.
 */
public class UserDao extends AbstractDao<DSUser>{

    /**
     *
     * @param userId
     * @return the Key for the passed ID.
     *
     * Since from everywhere, we need the userkey, make this public / static. Otherwise we always instantiate new UserDao's
     */
    public static Key<DSUser> DSUserKey(String userId){
        Key<DSUserRoot> rootKey = Key.create(DSUserRoot.class,DSUserRoot.ID);
        return Key.create(rootKey,DSUser.class,userId);
    }

    public void deleteAllUsers(){
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                List<Key<DSUser>> userKeys = ofy()
                        .load()
                        .type(DSUser.class)
                        .ancestor(ancestor())
                        .keys()
                        .list();
                ofy().delete().keys(userKeys);
            }
        });
    }

    public DSUser getUser(String userId) {
        Preconditions.checkNotNull(userId, "userId cannot be NULL");
        Key<DSUser> k = createKey(userId);
        return ofy().load().key(k).now();
    }

    public List<DSUser> getAllUsers() {
        return ofy()
                .load()
                .type(DSUser.class)
                .ancestor(ancestor())
                .list();
    }

    public DSUser getUserByEmail(String userEmail) {
        Preconditions.checkNotNull(userEmail, "userEmail cannot be NULL");
        return ofy()
                .load()
                .type(DSUser.class)
                .filter("email", userEmail)
                .ancestor(ancestor())
                .first()
                .now();
    }

    public String saveUser(final DSUser user){
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

    public List<DSUser> saveUsers(final List<DSUser> remoteUsers){

        for(DSUser user : remoteUsers){
            validate(user);
            user.setUserRoot(ancestor());
        }

        return ofy().transact(new Work<List<DSUser>>() {

            @Override
            public List<DSUser> run() {

                Map<Key<DSUser>, DSUser> userKeys = ofy().save().entities(remoteUsers).now();
                List<DSUser> users = new ArrayList<DSUser>();
                for(Key<DSUser> stored : userKeys.keySet()){
                    users.add(userKeys.get(stored));
                }
                return users;
            }
        });
    }



    public List<DSUser> getAllUsersWithRole(Globals.USER_ROLE role) {
        return ofy().load().type(DSUser.class).filter("userRoles",role).list();
    }


    @Override
    public Key<DSUserRoot> ancestor() {
        return Key.create(DSUserRoot.class,DSUserRoot.ID);
    }

}
