package io.fourcast.gae.model.transformer;

import com.googlecode.objectify.Key;

import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.model.user.User;

/**
 * Created by nielsbuekers on 28/08/15.
 */
public class GenericTransformer {

    Key<User> userKey(String userId) {
        return UserDao.dsUserKey(userId);
    }

}
