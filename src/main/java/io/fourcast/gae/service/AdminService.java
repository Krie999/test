package io.fourcast.gae.service;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.services.admin.directory.model.Member;
import io.fourcast.gae.dao.ProjectDao;
import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.manager.UserManager;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nielsbuekers on 07/08/15.
 */
@Api(
        name = "adminService",
        version = "v0.0.1",
        description = "Service to handle Admin Ops",
        clientIds = {
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID}
)
public class AdminService extends AbstractService {

    private UserDao userDao = new UserDao();
	private ProjectDao projectDao = new ProjectDao();

    private UserManager userManager = new UserManager();

    private static final String CONFIRM_CODE = "ja ik ben zeker";
    private static final String CONFIRM_ERROR = "nieje joenge. nieje.";

    @ApiMethod(name = "ping")
    public void Ping(){
        return;
    }


    /**
     * Loads all unknown users that are member from the group e2e_all_users@bnpparibasfortis.com into the DS.
     * For the known users, the TS is NOT validated. If they need a re-fetch, the user should trigger 'getUserDetails'
     * or the users should be removed from the DS, and then this function needs to be run again.
     *
     * @return
     * @throws IOException
     */
    @ApiMethod(name = "loadAllUnknownUsers")
    public List<User> loadAllUnknownUsers(com.google.appengine.api.users.User user) throws Exception {
        user = validateUserAccess(user);
        //get all members from the groups
        List<Member> allMembers = userManager.getGroupMembersForGroup(Globals.GAPPS_GROUP_ALL);
        List<String> emailsToLoad = new ArrayList<>();

        //for each member, check if in DS and valid
        for (Member m : allMembers) {
            User dsUser = userDao.getUserByEmail(m.getEmail());
            if (dsUser == null) {
                emailsToLoad.add(m.getEmail());
                log.info("new user " + m.getEmail() + " found.");
            }
        }
        int count = 0;
        List<User> savedUsers = new ArrayList<>();
        // get user details
        List<User> remoteUsers = new ArrayList<>();
        for (String emailtoLoad : emailsToLoad) {
            User remoteUser = userManager.getRemoteUserDetails(emailtoLoad);
            remoteUsers.add(remoteUser);
            count++;
            if(count % 100 == 0){
                List<User> saved = userDao.saveUsers(remoteUsers);
                savedUsers.addAll(saved);
                remoteUsers.clear();
                break;
            }
        }
        //save last batch < 100
        if(remoteUsers.size() > 0){
            List<User> saved = userDao.saveUsers(remoteUsers);
            savedUsers.addAll(saved);
        }
        //store details
        return savedUsers;
    }

    @Override
    protected Globals.USER_ROLE requiredRole() {
        return Globals.USER_ROLE.ROLE_ADMIN;
    }
}
