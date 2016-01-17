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

import java.util.Date;

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

        //clear OFY cache so that any retrieved project is not the EXACT same Object reference as testProject.
        projectDao.ofy().clear();
    }


    /**
     * Validate that a new project w/o ID, creationDate or lastModDate, gets these three values assigned on creation
     *
     * @throws Exception
     * @throws FCServerException
     * @throws ConstraintViolationsException
     * @throws FCUserException
     */
    @Test
    public void testSaveNewProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        Project project = new Project();
        Assert.assertNull(project.getId());
        projectDao.saveProject(project);
        Assert.assertNotNull(project.getId());
        Assert.assertNotNull(project.getCreationDate());
        Assert.assertNotNull(project.getLastModified());
    }

    @Test
    public void testRetrieveExistingProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        Project savedProject = projectDao.getProject(testProjectId);
        //it should not be the same object references
        Assert.assertNotSame(testProject, savedProject);
        //but it should be the same objects
        Assert.assertEquals(testProject, savedProject);
    }

    /**
     * Validate that the last modified date is updated on a save
     * @throws Exception
     */
    @Test
    public void testUpdateLastModDate() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        Date beforeSaveDate = new Date();

        Project savedProject = projectDao.getProject(testProjectId);

        //save again to update lastModDate
        projectDao.saveProject(savedProject);

        //value must have been updated
        Assert.assertTrue(testProject.getLastModified().getTime() != savedProject.getLastModified().getTime());

        //value must be later than when we initialised the save
        Assert.assertTrue(beforeSaveDate.getTime() < savedProject.getLastModified().getTime());

        //value must be younger than 'now'
        Assert.assertTrue(savedProject.getLastModified().getTime() < new Date().getTime());
    }

    /**
     * Validates that an exception is thrown when the timestamp in the DS is newer than the one passed in a save ops
     */
    @Test(expected = FCTimestampConflictException.class)
    public void testTimestampValidation() throws FCTimestampConflictException, FCUserException, FCServerException, ConstraintViolationsException {
        Date old = new Date(0L);
        testProject.setLastModified(old);
        projectDao.saveProject(testProject);
    }

    /**
     * TODO muchos moros testingos
     */
}
