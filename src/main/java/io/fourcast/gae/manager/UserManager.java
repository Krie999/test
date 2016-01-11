package io.fourcast.gae.manager;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;
import io.fourcast.gae.manager.service.GoogleDirectoryService;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by nielsbuekers on 03/08/15.
 */
public class UserManager {

    final Logger log = Logger.getLogger(UserManager.class.getName());

    private static Map<String, List<Member>> groupMemberCacheForGroup = new HashMap<>();
    private static Long cacheTimestamp = 0L;

    private Directory directoryService;

    /**
     * Gets the user with the given email address from the directory. object is filled with the roles he has.
     *
     * @param userEmail the email for the user which to get the full details
     * @return the User object, with it's email address and display name
     * @throws IOException        when the user's profile can't be read.
     */
    public User getRemoteUserDetails(String userEmail) throws Exception {
        //get email for id
        Directory.Users.Get userRequest = getDirectoryService().users().get(userEmail);
        com.google.api.services.admin.directory.model.User user;
        try {
            user = userRequest.execute();
        } catch (GoogleJsonResponseException e) {
            log.severe("Error getting user <<" + userEmail + ">>");
            throw new Exception(e.getDetails().getMessage());
        }

        User dsUser = new User();
        dsUser.setActive(!user.getSuspended());
        dsUser.setId(user.getId());
        dsUser.setEmail(user.getPrimaryEmail());
        dsUser.setDisplayName(user.getName().getFullName());

        //set brand based on Department in Google Apps User Account
        if (user.getOrganizations() != null) {
            @SuppressWarnings("unchecked")
            List<ArrayMap<Object, Object>> userOrganizations =
                    (List<ArrayMap<Object, Object>>) user.getOrganizations();
            parseUserOrgForUser(userOrganizations.get(0), dsUser);
        } else {
           //set default org values
        }

        //fill his roles
        validateUserRoles(dsUser);

        return dsUser;
    }

    private void parseUserOrgForUser(ArrayMap<Object, Object> org, User user) {

        //set values from org unit to user as needed
    }

    /**
     * @param user the user for which to validate the roles. Removes all roles and checks with Google Groups
     *                which roles to add.
     * @throws IOException        when the Google Groups can't be read
     */
    private void validateUserRoles(User user) throws Exception {
        Date now = new Date();
        if (now.getTime() - cacheTimestamp > Globals.MAX_GROUP_MEMBERSHIP__CACHE_AGE) {
            groupMemberCacheForGroup = new HashMap<>();
            cacheTimestamp = now.getTime();
        }

        //clear stored roles, we'll add the right ones now
        user.clearRoles();
        //always add basic user role for login
        user.addRole(Globals.USER_ROLE.ROLE_USER);

        //add all groups to be checked for membership (except the 'ROLE_GROUP', as this is no google group)
        List<String> groups = Arrays.asList(
                Globals.GAPPS_GROUP_ADMIN,
                Globals.GAPPS_GROUP_PROJECTOWNER);


        //for each group, check if user is member --> if so, has the role linked to the email address
        for (String group : groups) {
            List<Member> members = groupMemberCacheForGroup.get(group);

            //we don't have the users in cache yet --> get them
            if (members == null) {
                members = getGroupMembersForGroup(group);
                groupMemberCacheForGroup.put(group, members);
            }
            if (isUserMemberOfGroup(user.getEmail(), members)) {
                user.addRole(Globals.USER_ROLE.fromEmail(group));
            }
        }
    }


    public boolean isUserMemberOfGroup(String userEmail, List<Member> groupMembers) throws Exception {
        for (Member member : groupMembers) {
            if (member.getEmail().equals(userEmail)) {
                return true;
            }
        }
        return false;
    }

    public List<Member> getGroupMembersForGroup(String groupEmail) throws Exception {
        Directory.Members.List listRequest = getDirectoryService().members().list(groupEmail);
        List<Member> members = new ArrayList<>();
        String pagetoken = null;
        do {
            try {
                listRequest = listRequest.setPageToken(pagetoken);
                Members result = listRequest.execute();
                pagetoken = result.getNextPageToken();
                members.addAll(result.getMembers());
            } catch (GoogleJsonResponseException e) {
                throw new Exception(e.getDetails().getMessage());
            }
        }while(pagetoken!= null);
        return members;
    }

    public Directory getDirectoryService() throws Exception {
        if (directoryService == null) {
            directoryService = GoogleDirectoryService.getDirectoryService();
        }
        return directoryService;
    }
}
