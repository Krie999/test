package io.fourcast.gae.service;


import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.googlecode.objectify.Ref;
import io.fourcast.gae.dao.ProjectDao;
import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.Globals;
import io.fourcast.gae.util.ServiceConstants;

import java.util.List;

@Api(
        name = "smartService",
        version = "v0.0.1",
        description = "Service to handle SMART DSEntry requests",
        clientIds = {
                ServiceConstants.WEB_CLIENT_ID_DEV,
                ServiceConstants.WEB_CLIENT_ID_QA,
                ServiceConstants.WEB_CLIENT_ID_PROD,
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID}
)

public class ProjectService extends AbstractService {

    /**
     * TODO SMART_PROJECT_STATUS Long instead of String
     **/

    private ProjectDao projectDao = new ProjectDao();
    private UserDao userDao = new UserDao();


    @ApiMethod(name = "saveProject", httpMethod = "post")
    public Project saveProject(com.google.appengine.api.users.User user, Project project) throws Exception {
        //only validate login, no roles
        user = validateUserAccess(user);

        //no parent project is a 0
        if (project.getParentId() == null) {
            project.setParentId(0L);
        }
        //set parent code if id is set
        if (project.getParentId() != 0) {
            Project parentProject = projectDao.getProject(project.getParentId());
        }


        User currentUser = userDao.getUserByEmail(user.getEmail());
        Boolean ownerChanged = false;
        Boolean sponsorChanged = false;
        //there must always be an owner
        if (project.getOwner() == null) {
            project.setOwner(Ref.create(currentUser));
        }

        Project oldProject = null;

        if (project.getId() != null) {
            oldProject = projectDao.getProject(project.getId());

        }


        project = projectDao.saveProject(project);


        return project;
    }

    @ApiMethod(name = "getProject")
    public Project getProject(com.google.appengine.api.users.User user, @Named("id") Long smartProjectId) throws OAuthRequestException, UnauthorizedException {
        user = validateUserAccess(user);

        Project project = projectDao.getProject(smartProjectId);
        return project;
    }

    @ApiMethod(name = "getAllProjects", path = "getAllProjects")
    public List<Project> getAllProjects(com.google.appengine.api.users.User user) throws UnauthorizedException, OAuthRequestException {
        user = validateUserAccess(user);
        return projectDao.getAllProjects();
    }


    @ApiMethod(name = "getSmartProjectsForCurrentUser")
    public List<Project> getSmartProjectsForCurrentUser(com.google.appengine.api.users.User user) throws Exception {
        user = validateUserAccess(user);
        User e2EUser = userDao.getUserByEmail(user.getEmail());
        if (e2EUser == null) {
            throw new Exception("user not in DB yet. Is the user an E2E member? Sure you called 'getUserDetails' to fill dS?");
        }
        return projectDao.getProjectsForUser(e2EUser);
    }


    @Override
    protected Globals.USER_ROLE requiredRole() {
        return Globals.USER_ROLE.ROLE_PROJECT_OWNER;
    }


}