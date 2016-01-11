package io.fourcast.gae.model.transformer;

import com.googlecode.objectify.Key;

import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.model.comms.CommsChannel;
import io.fourcast.gae.model.comms.CommsProject;
import io.fourcast.gae.model.lmd.LmdProject;
import io.fourcast.gae.model.roots.CommsChannelRoot;
import io.fourcast.gae.model.roots.CommsRoot;
import io.fourcast.gae.model.roots.LmdRoot;
import io.fourcast.gae.model.user.DSUser;

/**
 * Created by nielsbuekers on 28/08/15.
 */
public class GenericTransformer {

    Key<DSUser> userKey(String userId) {
        return UserDao.e2eUserKey(userId);
    }

    Key<LmdProject> lmdProjectKey(Long projectId) {
        Key<LmdRoot> rootKey = Key.create(LmdRoot.class, LmdRoot.ID);
        return Key.create(rootKey, LmdProject.class, projectId);
    }
    
    Key<CommsProject> commsProjectKey(Long projectId) {
    	Key<CommsRoot> rootKey = Key.create(CommsRoot.class, CommsRoot.ID);
    	return Key.create(rootKey, CommsProject.class, projectId);
    }
    
    Key<CommsChannel> commsChannelKey(Long projectId){
    	Key<CommsChannelRoot> rootKey = Key.create(CommsChannelRoot.class, CommsChannelRoot.ID);
    	return Key.create(rootKey, CommsChannel.class, projectId);
    }
}
