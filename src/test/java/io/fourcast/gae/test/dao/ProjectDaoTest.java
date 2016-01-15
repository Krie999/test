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
    private Project sampleProject;
    private Long testProjectId ;
    @SuppressWarnings("unchecked")
    @Before
    public void setUpSampleProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        sampleProject = new Project();
        sampleProject.setActive(true);
        sampleProject.setStatus(Project.PROJECT_STATUS.CLOSED);
        sampleProject = projectDao.saveProject(sampleProject);
        Assert.assertNotNull(sampleProject);
        testProjectId = sampleProject.getId();

    }


    @SuppressWarnings("unchecked")
    @Test
    public void testSaveNewProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {
        Project project = new Project();
        Assert.assertNull(project.getId());
        Project savedProject = projectDao.saveProject(project);
        Assert.assertNotNull(savedProject.getId());
        Assert.assertNotNull(savedProject.getCreationDate());
        Assert.assertNotNull(savedProject.getLastModified());
    }

    /*
    @SuppressWarnings("unchecked")
    @Test
    public void testSaveExistingProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException {

        Assert.assertNull(project.getId());
        Project savedProject = projectDao.saveProject(project);
        Assert.assertEquals(project,savedProject);
    }
*/
}
