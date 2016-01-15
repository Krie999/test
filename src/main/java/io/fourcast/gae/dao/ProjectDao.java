package io.fourcast.gae.dao;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.model.root.ProjectRoot;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;

import java.util.List;


@SuppressWarnings("serial")
public class ProjectDao extends AbstractDao<Project> {

    private UserDao userDao = new UserDao();


    public Project getProject(Long projectId) {
        Preconditions.checkNotNull(projectId, "ProjectId cannot be NULL");

        Key<Project> projectKey = createKey(projectId);
        return ofy().load().key(projectKey).now();
    }


    public Project saveProject(final Project project) throws ConstraintViolationsException {
        validate(project);
        Project savedProject;


        savedProject = ofy().transact(new Work<Project>() {
            @Override
            public Project run() {
                try {
                    if (project.getId() != null) {
                        //validate timestamp for existing project

                        validateTimestamp(project, getProject(project.getId()));

                        //FE can't update list of linked subprojects. Always override with old data. Subproject links itself to root, only then this list is updated
                        Key<Project> projectKey = createKey(project.getId());
                        Project storedProject = ofy().load().key(projectKey).now();
                        //migt have been an invalid ID
                        if (storedProject != null) {
                            project.setSubProjectIds(storedProject.getSubProjectIds());
                        }
                    }

                    //store this (potentially new) project project so we are sure of our project key, since we might need to set it as a sub of the parent

                    //the subproject is a VIRTUAL child of the PARENT, not an ACTUAL DS link --> avoids trouble with recreating ancestor path
                    //when we're on e.g. LMD and need to recreate the complete ancestor path: project - coca - space. No nested project parents, only linked
                    project.setRootDSEntry(ancestor());
                    Key<Project> newProjectKey = ofy().save().entity(project).now();

                    //if this project project is a subproject, add it to the parent's list of children
                    if (project.getParentId() != 0) {
                        Project parentProject = getProject(project.getParentId());
                        parentProject.addSubProjectId(newProjectKey.getId());

                        //no need for a synchronous call
                        ofy().save().entity(parentProject);
                    }


                    return ofy().load().key(newProjectKey).now();
                }catch(Exception e){
                    log.warning(e.getLocalizedMessage());
                    return null;
                }
            }
        });

        if (savedProject.equalsIgnoreModifDateAndRootProject(project)) {


        }
        return savedProject;

    }

    /**
     * @param e2eUser
     * @return
     */
    @SuppressWarnings("static-access")
    public List<Project> getProjectsForUser(User e2eUser) {
        Ref<User> userRef = Ref.create(userDao.dsUserKey(e2eUser.getId()));
        List<Project> projectsOfUser = query().filter("owner", userRef).list();

        return projectsOfUser;
    }


    /**
     * @return
     */
    public List<Project> getAllProjects() {
        List<Project> response = query().list();
        return response;
    }

    @Override
    public Key<ProjectRoot> ancestor() {
        return Key.create(ProjectRoot.class, ProjectRoot.ID);
    }
}