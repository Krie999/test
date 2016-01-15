package io.fourcast.gae.dao;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.model.root.ProjectRoot;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;
import io.fourcast.gae.util.exceptions.FCServerException;
import io.fourcast.gae.util.exceptions.FCTimestampConflictException;
import io.fourcast.gae.util.exceptions.FCUserException;

import java.util.List;


@SuppressWarnings("serial")
public class ProjectDao extends AbstractDao<Project> {

    private UserDao userDao = new UserDao();

    /**
     * get the project for a given id.
     *
     * @param projectId the ID for which the project needs to be retrieved.
     * @return returns the project with the given ID. returns null if no project is found with the given id.
     */
    public Project getProject(Long projectId) {
        Preconditions.checkNotNull(projectId, "ProjectId cannot be NULL");

        Key<Project> projectKey = createKey(projectId);
        return ofy().load().key(projectKey).now();
    }

    /**
     * Save the given project
     *
     * @param project the project to save
     * @return the saved project. In case of a new project, the project now has an ID.
     * @throws ConstraintViolationsException
     */
    public Project saveProject(final Project project) throws ConstraintViolationsException, FCTimestampConflictException, FCServerException, FCUserException {
        validate(project);
        try {
            //return the project once the TXN has finished
            return ofy().transact(new Work<Project>() {
                @Override
                public Project run() {
                    try {
                        if (project.getId() != null) {
                            validateTimestamp(project, getProject(project.getId()));
                        }

                        //store this (potentially new) project so we are sure of our project key, since we might need to set it as a sub of the parent
                        //the subproject is a VIRTUAL child of the PARENT, not part of the ancestor path.
                        project.setRootDSEntry(ancestor());
                        Key<Project> newProjectKey = ofy().save().entity(project).now();

                        //if this project project is a subproject, add it to the parent's list of children
                        if (project.getParentId() != 0) {
                            Project parentProject = getProject(project.getParentId());
                            parentProject.addSubProjectId(newProjectKey.getId());
                            //no need for a synchronous call for the parent, we're not going to use it further
                            ofy().save().entity(parentProject);
                        }
                        return ofy().load().key(newProjectKey).now();
                    } catch (FCTimestampConflictException e) {
                        throw new RuntimeException(e);
                    } catch (FCUserException e) {
                        throw new RuntimeException(e);
                    } catch (FCServerException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        //catch all different exceptions thrown from within the TXN, unwrap and rethrow them
        catch (RuntimeException re) {

            Throwable cause = re.getCause();

            if (cause instanceof FCTimestampConflictException) {
                throw (FCTimestampConflictException) cause;
            } else if (cause instanceof FCUserException) {
                throw (FCUserException) cause;
            } else if (cause instanceof FCServerException) {
                throw (FCServerException) cause;
            }
        }
        return null;

    }

    /**
     * get all projects for which the user is an owner.
     *
     * @param user the domain user for which to get the projects for
     * @return a list of all projects for the given user. An empty list if no projects are found
     */
    public List<Project> getProjectsForUser(User user) {
        Ref<User> userRef = Ref.create(userDao.dsUserKey(user.getId()));
        List<Project> projectsOfUser = query().filter("owner", userRef).list();

        return projectsOfUser;
    }


    /**
     * Retrieve all projects
     *
     * @return a list of all projects. An empty list if there are no projects
     */
    public List<Project> getAllProjects() {
        List<Project> response = query().list();
        return response;
    }

    /**
     * Retrieve the ancestor key. Used by the generic method 'query', amongst others.
     *
     * @return the common ancestor key for the Entity kind 'Project'.
     */
    @Override
    public Key<ProjectRoot> ancestor() {
        return Key.create(ProjectRoot.class, ProjectRoot.ID);
    }


}