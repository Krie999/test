package io.fourcast.gae.service;


import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Ref;
import io.fourcast.gae.dao.SmartDao;
import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.model.cocamat.CocamatProject;
import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.model.lmd.LmdProject;
import io.fourcast.gae.model.lmd.LmdTask;
import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.model.space.SpaceProject;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.service.dto.smart.SmartContributorDTO;
import io.fourcast.gae.service.dto.smart.SmartProjectListDTO;
import io.fourcast.gae.service.dto.smart.SmartProjectResponseDTO;
import io.fourcast.gae.utils.Globals;
import io.fourcast.gae.utils.exceptions.ConstraintViolationsException;
import io.fourcast.gae.utils.exceptions.E2EServerException;

import java.text.ParseException;
import java.util.*;

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

public class SmartService extends ProjectService {

    /**
     * TODO SMART_PROJECT_STATUS Long instead of String
     **/

    private SmartDao smartDao = new SmartDao();
    private UserDao userDao = new UserDao();
    private LmdDao lmdDao = new LmdDao();
    private static ProjectManager projectManager = new ProjectManager();
    private NotificationManager notifications = new NotificationManager();

    @ApiMethod(name = "saveProjects")
    public List<SmartProject> saveProjects(User user,SmartProjectListDTO projects) throws OAuthRequestException, UnauthorizedException, ConstraintViolationsException, ParseException, E2EServerException {
        validateUserAccess(user);
        List<SmartProject> saved = new ArrayList<>();
        for(SmartProject p : projects.getList()){
            p = saveProject(user,p);
            log.info("saved project #" + p.getProjectCode() + " with id " + p.getId());
            saved.add(p);
        }
        return saved;
    }

    @ApiMethod(name = "saveProject", httpMethod = "post")
    public SmartProject saveProject(User user, SmartProject project) throws UnauthorizedException, OAuthRequestException, ConstraintViolationsException, E2EServerException, ParseException {
        //only validate login, no roles
        user = validateUserAccess(user);

        //no parent smart is a 0
        if (project.getParentId() == null) {
            project.setParentId(0L);
        }
        //set parent code if id is set
        if(project.getParentId() != 0){
            SmartProject parentProject = smartDao.getSmartProject(project.getParentId());
            project.setParentProjectCode(parentProject.getProjectCode());

        }


        DSUser currentUser = userDao.getUserByEmail(user.getEmail());
        Boolean ownerChanged = false;
        Boolean sponsorChanged = false;
        //there must always be an owner
        if (project.getOwner() == null) {
            project.setOwner(Ref.create(currentUser));
        }

        //validate that no more than 3 KPI's are selected
        if (project.getKpiIds() != null) {
            //TODO refactor check boolean to be TRUE, not just PRESENT! Can be false!
            if (project.getExtraKpiField() != null) {
                if (project.getExtraKpiField().length() > 0) {

                    if (countAmountOfKPIS(project.getKpiIds()) > 2) {
                        throw new ConstraintViolationsException("It is not allowed to select more than 3 KPI's");
                    }
                }
            } else {
                if (countAmountOfKPIS(project.getKpiIds()) > 3) {
                    throw new ConstraintViolationsException("It is not allowed to select more than 3 KPI's");
                }
            }
        }
        //if the project is new, set the initial properties
        if (project.getId() == null) {
            if(project.getCreationDate()== null || project.getCreationDate().getTime() == 0) {
                project.setCreationDate(new Date());
            }
            if(project.getProjectCode() == null || project.getProjectCode().length() == 0) {
                project.setProjectCode(smartDao.generateNewSmartCode());
            }

            if (project.getDepartmentId() == null && project.getOwner() != null) {
                project.setDepartmentId(project.getOwner().getDepartmentId());
            }

            //only on creation, default brand to current user
            if (project.getBrand() == null) {
                project.setBrand(currentUser.getBrand());
            }
        } else {
            //validate that the parentSmartProject is not the project itsself
            if (project.getParentId() != 0) {
                if (project.getParentId().equals(project.getId())) {
                    throw new ConstraintViolationsException("A project cannot be its own parent!");
                }
                //check if parent smart project is not a child project at the same time
                // if X is the parent of Y, Y cannot be the parent of X
                if (project.getSubProjectIds() != null) {
                    if (project.getSubProjectIds().contains(project.getParentId())) {
                        throw new E2EServerException("A child project cannot be parent of it's parent project");
                    }
                }
            }
        }
        SmartProject oldProject = null;

        if(project.getId() != null){
            oldProject = smartDao.getSmartProject(project.getId());

        }



        project = smartDao.saveSMARTProject(project);
        smartDao.addProjectToSearchDS(project);

        if(oldProject != null) {
            if (!project.getOwner().equals(oldProject.getOwner())) {
                Notification notification = new Notification(Ref.create(project.getOwner()), Globals.NOTIFICATION_TYPE.OWNER_CHANGED, Globals.APP.APP_SMART, project.getId(), Ref.create(currentUser));
                notification.setDescription(currentUser.getDisplayName() + " has assigned you as Owner of Initiative " + oldProject.getProjectCode() + " " + project.getProjectName() + "!");
                notifications.addNotification(currentUser, notification);

                notification = new Notification(Ref.create(oldProject.getOwner()), Globals.NOTIFICATION_TYPE.OWNER_CHANGED, Globals.APP.APP_SMART, project.getId(), Ref.create(currentUser));
                notification.setDescription(currentUser.getDisplayName() + " has changed the Owner of Initiative " + oldProject.getProjectCode() + " " + project.getProjectName() + "!");
                notifications.addNotification(currentUser, notification);

            } else if (!project.getStatus().equals(oldProject.getStatus())) {
                Notification notification = new Notification(Ref.create(project.getOwner()), Globals.NOTIFICATION_TYPE.STATUS_CHANGED, Globals.APP.APP_SMART, project.getId(), Ref.create(currentUser));
                notification.setDescription("Your project " + oldProject.getProjectCode() + " " + oldProject.getProjectName() + " has been given a new status!");
                notifications.addNotification(currentUser, notification);

            } else if (project.getGoLiveDate() != null && !project.getGoLiveDate().equals(oldProject.getGoLiveDate())) {
                Notification notification = new Notification(Ref.create(project.getOwner()), Globals.NOTIFICATION_TYPE.TIMING_CHANGED, Globals.APP.APP_SMART, project.getId(), Ref.create(currentUser));
                notification.setDescription("The Go Live date of your project " + oldProject.getProjectCode() + " " + oldProject.getProjectName() + " has been updated!");
                notifications.addNotification(currentUser, notification);

            } else if (!project.equalsIgnoreModifDateAndRootProject(oldProject)) {
                Notification notification = new Notification(Ref.create(project.getOwner()), Globals.NOTIFICATION_TYPE.PROJECT_CHANGED, Globals.APP.APP_SMART, project.getId(), Ref.create(currentUser));
                notification.setDescription("Your project " + oldProject.getProjectCode() + " " + oldProject.getProjectName() + " has been updated!");
                notifications.addNotification(currentUser, notification);

            }
        }

        return project;
    }

    @ApiMethod(name = "getProject")
    public SmartProject getProject(User user, @Named("id") Long smartProjectId) throws OAuthRequestException, UnauthorizedException {
        user = validateUserAccess(user);

        SmartProject project = smartDao.getSmartProject(smartProjectId);
        return project;
    }

    @ApiMethod(name = "getAllProjects", path = "getAllProjects")
    public List<SmartProject> getAllProjects(User user) throws UnauthorizedException, OAuthRequestException {
        user = validateUserAccess(user);
        return smartDao.getAllSmartProjects();
    }

    @ApiMethod(name = "getSimpleSmartProjects")
    public List<SimpleProjectResponseDTO> getSimpleSmartProjects(User user) throws UnauthorizedException, OAuthRequestException {
        user = validateUserAccess(user);
        List<SimpleProjectResponseDTO> resp = new ArrayList<>();
        for (SmartProject s : smartDao.getAllSmartProjects()) {
            String localDesc = s.getProjectCode() + " - " + s.getProjectName() + " - " + s.getOwner().getDisplayName();
            resp.add(new SimpleProjectResponseDTO(s.getId(), localDesc));
        }
        return resp;
    }

    @SuppressWarnings("unchecked")
    @ApiMethod(name = "getNumberOfSmartProjects")
    public SmartProjectResponseDTO getNumberOfSmartProjects(User user, CursorDTO cursorDTO, @Named("numberOfItems") int numberOfItems) throws OAuthRequestException, UnauthorizedException {
        user = validateUserAccess(user);
        String cursorString = cursorDTO.getCursor();

        Map<Globals.PROJECT_MAP, Object> response = smartDao.getNumberOfSmartProjects(cursorString, numberOfItems);

        SmartProjectResponseDTO result = new SmartProjectResponseDTO();
        result.setCursor((String) response.get(Globals.PROJECT_MAP.CURSOR));
        result.setProjects((List<SmartProject>) response.get(Globals.PROJECT_MAP.PROJECTLIST));

        return result;
    }

    @ApiMethod(name = "getLatestSmartProjects", path = "getLatestSmartProjects")
    public List<SmartProject> getLastestSmartProjects(User user) throws UnauthorizedException, OAuthRequestException {
        user = validateUserAccess(user);
        return smartDao.getLatestSmartProjects();
    }

    @ApiMethod(name = "getSmartProjectsForCurrentUser")
    public List<SmartProject> getSmartProjectsForCurrentUser(User user) throws E2EServerException, UnauthorizedException, OAuthRequestException {
        user = validateUserAccess(user);
        DSUser e2EUser = userDao.getUserByEmail(user.getEmail());
        if (e2EUser == null) {
            throw new E2EServerException("user not in DB yet. Is the user an E2E member? Sure you called 'getUserDetails' to fill dS?");
        }
        return smartDao.getSmartProjectsForUser(e2EUser);
    }


    @ApiMethod(name = "getProjectTree")
    public List<ProjectTreeEntry> getProjectTree(@Named("id") Long projectId) {
        return Arrays.asList(findSimpleProjectTree(Globals.APP.APP_SMART, projectId));
    }

    @ApiMethod(name = "contributorsForProject")
    public List<SmartContributorDTO> contributorsForProject(User user,@Named("id")Long projectId) throws OAuthRequestException, UnauthorizedException {
        validateUserAccess(user);
        SmartProject project = smartDao.getSmartProject(projectId);
        List<SmartContributorDTO> contributors = getContributorsForSmart(project);

        //child project contributors
        List<ProjectTreeEntry> childProjectEntries = projectManager.findChildEntriesForProject(project);
        for(ProjectTreeEntry childEntry : childProjectEntries){
            List<SmartContributorDTO> contributorDTOs = getContributorsForProjectEntry(childEntry);
            contributors.addAll(contributorDTOs);
        }

        return contributors;
    }

    private List<SmartContributorDTO> getContributorsForProjectEntry(ProjectTreeEntry entry){
        List<SmartContributorDTO> currentProjectContributor = new ArrayList<>();
        switch (entry.getAppType()){
            case APP_SMART:
                currentProjectContributor.addAll(getContributorsForSmart((SmartProject)entry.getProject()));
                break;
            case APP_COCAMAT:
                currentProjectContributor.addAll(getContributorsForCocamat((CocamatProject)entry.getProject()));
                break;
            case APP_SPACE:
                currentProjectContributor.addAll(getContributorsForSpace((SpaceProject)entry.getProject()));
                break;
            case APP_LMD:
                currentProjectContributor.addAll(getContributorsForLmd((LmdProject)entry.getProject()));
                break;
            case APP_COMMS:
                break;
            case APP_CHANNELS:
                break;
            case APP_ALL:
                break;
        }
        for(ProjectTreeEntry childEntries: entry.getProjects()){
            currentProjectContributor.addAll(getContributorsForProjectEntry(childEntries));
        }
        return currentProjectContributor;
    }

    private List<SmartContributorDTO> getContributorsForSmart(SmartProject project){
        List<SmartContributorDTO> contribs = new ArrayList<>();
        contribs.add(ownerContributorForProject(project,Globals.APP.APP_SMART).setProjectCode(project.getProjectCode()));
        if(project.getSponsor() != null) {
            contribs.add(sponsorContributorForSmartProject(project));
        }
        return contribs;
    }

    private SmartContributorDTO sponsorContributorForSmartProject(SmartProject project) {
        SmartContributorDTO sponsor = null;
        if(project.getSponsor() != null) {
           sponsor =  new SmartContributorDTO();
            sponsor.setAppType(Globals.APP.APP_SMART);
            sponsor.setProjectId(project.getId());
            sponsor.setProjectName(project.getProjectName());
            sponsor.setDisplayName(project.getSponsor().getDisplayName());
            sponsor.setRole(SmartContributorDTO.SMARTCONTRIBUTOR_ROLE.SPONSOR);
            sponsor.setProjectCode(project.getProjectCode());
        }


        return sponsor;
    }

    private List<SmartContributorDTO> getContributorsForCocamat(CocamatProject project){
        List<SmartContributorDTO> contribs = new ArrayList<>();
        contribs.add(ownerContributorForProject(project, Globals.APP.APP_COCAMAT).setProjectCode(project.getCpCode()));
        return contribs;
    }

    private List<SmartContributorDTO> getContributorsForSpace(SpaceProject project){
        List<SmartContributorDTO> contribs = new ArrayList<>();


        contribs.add(ownerContributorForProject(project,Globals.APP.APP_SPACE).setProjectCode(project.getCpCode()));
        return contribs;
    }

    private List<SmartContributorDTO> getContributorsForLmd(LmdProject project){
        List<SmartContributorDTO> contribs = new ArrayList<>();
        SmartContributorDTO owner;
        List<LmdTask> tasks = lmdDao.getTasksForProject(project.getId());

        if(tasks != null){
            for(LmdTask task: tasks){
                if(task.getOwner() != null) {
                    owner = new SmartContributorDTO();
                    owner.setProjectId(project.getId());
                    owner.setProjectName(task.getTaskDescription());
                    owner.setProjectCode(task.getTaskNumber());
                    owner.setAppType(Globals.APP.APP_LMD);
                    owner.setDisplayName(task.getOwner().get().getDisplayName());
                    owner.setRole(SmartContributorDTO.SMARTCONTRIBUTOR_ROLE.LMD_ANALYST);
                    contribs.add(owner);
                }
            }

        }
        return contribs;
    }

    //general contributor for all project types
    private SmartContributorDTO ownerContributorForProject(DSEntry project, Globals.APP appType) {
        SmartContributorDTO owner = new SmartContributorDTO();
        owner.setDisplayName(project.getOwner().getDisplayName());
        owner.setAppType(appType);
        owner.setProjectId(project.getId());
        owner.setProjectName(project.getProjectName());
        owner.setRole(SmartContributorDTO.SMARTCONTRIBUTOR_ROLE.OWNER);
        return owner;
    }

    private int countAmountOfKPIS(Map<String,Boolean> kpis){
        int amountOfKPIS = 0;
        for (Boolean s : kpis.values()) {
            if(s) amountOfKPIS++;
        }
        return amountOfKPIS;
    }

    @Override
    protected Globals.USER_ROLE requiredRole() {
        return Globals.USER_ROLE.ROLE_SMART;
    }


}