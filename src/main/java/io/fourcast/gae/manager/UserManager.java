package io.fourcast.gae.manager;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;
import io.fourcast.gae.manager.service.GoogleDirectoryService;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;
import io.fourcast.gae.util.exceptions.FCServerException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


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
     * @throws IOException when the user's profile can't be read.
     */
    public User getRemoteUserDetails(String userEmail) throws FCServerException {

        com.google.api.services.admin.directory.model.User directoryUser;

        try {
            directoryUser = getDirectoryService().users().get(userEmail).execute();
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            throw new FCServerException("Error parsing remote user details.",e.getDetails().getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new FCServerException("Error getting remote user details.",e.getLocalizedMessage());
        }

        User dsUser = dsUserFromDirectoryUser(directoryUser);

        fetchAndUpdateUserRoles(dsUser);

        return dsUser;
    }

    /**
     * Creates a domain user for the given directory user
     * @param directoryUser the directory user to start with
     * @return the domain user created based on the directory user
     */
    private User dsUserFromDirectoryUser(com.google.api.services.admin.directory.model.User directoryUser) {
        User dsUser = new User();
        dsUser.setActive(!directoryUser.getSuspended());
        dsUser.setId(directoryUser.getId());
        dsUser.setEmail(directoryUser.getPrimaryEmail());
        dsUser.setDisplayName(directoryUser.getName().getFullName());

        //set brand based on Department in Google Apps User Account
        if (directoryUser.getOrganizations() != null) {
            @SuppressWarnings("unchecked")
            List<ArrayMap<Object, Object>> userOrganizations =
                    (List<ArrayMap<Object, Object>>) directoryUser.getOrganizations();
            parseUserOrgForUser(userOrganizations.get(0), dsUser);
        } else {
            //set default org values
        }
        return dsUser;
    }

    /**
     * Parses additional attributes from the Gapps Directory like Org details (cost center, department...)
     * @param org the Org to parse (google apps model, not regular admin org unit)
     * @param user the user in which to save the parsed data
     */
    private void parseUserOrgForUser(ArrayMap<Object, Object> org, User user) {
        //set extra values from org unit to user as needed. Default none needed, depends on customer needs
    }

    /**
     *
     * @param user
     * @throws FCServerException
     */
    private void fetchAndUpdateUserRoles(User user) throws FCServerException {
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

    public List<Member> getGroupMembersForGroup(String groupEmail) throws FCServerException {

        //create a list request
        Directory.Members.List listRequest;
        try {
            listRequest = getDirectoryService().members().list(groupEmail);
        } catch (IOException e) {
            throw new FCServerException("Error creating Directory List request.", e.getLocalizedMessage());
        }

        //as long as there is a pagination token, keep executing the request with the new token to get all results
        List<Member> members = new ArrayList<>();
        String pagetoken = null;
        do {
            try {
                listRequest = listRequest.setPageToken(pagetoken);
                Members result = listRequest.execute();
                pagetoken = result.getNextPageToken();
                members.addAll(result.getMembers());
            } catch (GoogleJsonResponseException e) {
                throw new FCServerException("Error parsing list request", e.getDetails().getMessage());
            } catch (IOException e) {
                throw new FCServerException("Error executing list request.", e.getLocalizedMessage());
            }
        } while (pagetoken != null);


        return members;
    }

    private boolean isUserMemberOfGroup(String userEmail, List<Member> groupMembers) {
        for (Member member : groupMembers) {
            if (member.getEmail().equals(userEmail)) {
                return true;
            }
        }
        return false;
    }

    private Directory getDirectoryService() {
        if (directoryService == null) {
            directoryService = GoogleDirectoryService.getDirectoryService();
        }
        return directoryService;
    }
}
