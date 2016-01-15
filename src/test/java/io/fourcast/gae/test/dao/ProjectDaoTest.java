package io.fourcast.gae.test.dao;

import io.fourcast.gae.dao.ProjectDao;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;
import io.fourcast.gae.util.exceptions.FCServerException;
import io.fourcast.gae.util.exceptions.FCTimestampConflictException;
import io.fourcast.gae.util.exceptions.FCUserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by nbuekers on 15/01/16.
 */
public class ProjectDaoTest extends AbstractDAOTest {

    private static ProjectDao projectDao = new ProjectDao();
    private Project testProject;
    private Long testProjectId;

    @SuppressWarnings("unchecked")
    @Before
    public void setUpSampleProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        testProject = new Project();
        testProject.setActive(true);
        testProject.setStatus(Project.PROJECT_STATUS.CLOSED);
        projectDao.saveProject(testProject);
        Assert.assertNotNull(testProject.getId());
        testProjectId = testProject.getId();

    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSaveNewProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        Project project = new Project();
        Assert.assertNull(project.getId());
        projectDao.saveProject(project);
        Assert.assertNotNull(project.getId());
        Assert.assertNotNull(project.getCreationDate());
        Assert.assertNotNull(project.getLastModified());
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieveExistingProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {

        projectDao.ofy().clear();
        Project savedProject = projectDao.getProject(testProjectId);
        //it should not be the same object references
        Assert.assertNotSame(testProject,savedProject);
        //but it should be the same objects
        Assert.assertEquals(testProject, savedProject);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateLastModDate() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        //clear OFY cache so that savedProject is not the EXACT same Object reference as testProject.
        projectDao.ofy().clear();
        Project savedProject = projectDao.getProject(testProjectId);
        //save again to update lastModDate
        projectDao.saveProject(savedProject);
        Assert.assertTrue(!testProject.getLastModified().equals(savedProject.getLastModified()));
    }



}
