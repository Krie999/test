package io.fourcast.gae.test.service;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.googlecode.objectify.ObjectifyService;
import io.fourcast.gae.dao.ProjectDao;
import io.fourcast.gae.dao.UserDao;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.service.ProjectService;
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;
import io.fourcast.gae.util.exceptions.FCServerException;
import io.fourcast.gae.util.exceptions.FCTimestampConflictException;
import io.fourcast.gae.util.exceptions.FCUserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

/**
 * Created by nbuekers on 17/01/16.
 */
// Setting up service testing following
// http://www.luckyryan.com/2013/06/28/unit-testing-with-mockito/

public class ProjectServiceTest {

    private ProjectDao projectDao;
    private UserDao userDao;
    private ProjectService projectService;
    private ProjectService spyProjectService;

    final Logger log = Logger.getLogger(ProjectServiceTest.class.getName());

    private static final String OAUTH_CONSUMER_KEY = com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;
    private static final String OAUTH_EMAIL = "johnDoe@fourcast.io";
    private static final String OAUTH_USER_ID = "johnDoe";
    private static final String OAUTH_AUTH_DOMAIN = "fourcast.io";
    private static final boolean OAUTH_IS_ADMIN = true;

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalUserServiceTestConfig()
                    .setOAuthConsumerKey(OAUTH_CONSUMER_KEY)
                    .setOAuthEmail(OAUTH_EMAIL)
                    .setOAuthUserId(OAUTH_USER_ID)
                    .setOAuthAuthDomain(OAUTH_AUTH_DOMAIN)
                    .setOAuthIsAdmin(OAUTH_IS_ADMIN));
    @Before
    public void doSetup() throws UnauthorizedException, OAuthRequestException {
        helper.setUp();
        projectDao = mock(ProjectDao.class);
        userDao = mock(UserDao.class);
        projectService = new ProjectService(projectDao, userDao);
        spyProjectService = spy(projectService);
        doReturn(oAuthUser()).when(spyProjectService).validateUser(any(com.google.appengine.api.users.User.class));
        doReturn(oAuthUser()).when(spyProjectService).validateUser(any(com.google.appengine.api.users.User.class),anyBoolean());


        //TODO URGENT find a way to not have to deal with OFY in the service? owner-Logic should not be in Dao.. dunno!
        ObjectifyService.begin();
    }

    @Test
    public void testGetProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException, OAuthRequestException, UnauthorizedException {


        when(projectDao.getProject(any(Long.class))).thenAnswer(new Answer<Project>() {
            @Override
            public Project answer(InvocationOnMock invocation) throws Throwable {
                Project p = new Project();
                p.setId((Long) invocation.getArguments()[0]);
                p.setName("Test project");
                p.setLastModified(new Date());
                p.setStatus(Project.PROJECT_STATUS.ACTIVE);
                return p;
            }
        });

        Project p = spyProjectService.getProject(oAuthUser(), 123L);
        Assert.assertTrue(p.getId() == 123L);
    }

    @Test
    public void testSaveProject() throws FCTimestampConflictException, FCServerException, ConstraintViolationsException, FCUserException, UnauthorizedException, OAuthRequestException {
        when(projectDao.saveProject(any(Project.class))).thenAnswer(new Answer<Project>() {
            @Override
            public Project answer(InvocationOnMock invocation) throws Throwable {
                Project p = (Project) invocation.getArguments()[0];
                p.setLastModified(new Date());
                if (p.getId() == null) p.setId((long) Math.random());
                return p;
            }
        });

        when(userDao.getUserByEmail(any(String.class))).thenReturn(testUser());


        Project p = new Project();

       spyProjectService.saveProject(oAuthUser(), p);
    }

    private com.google.appengine.api.users.User oAuthUser() throws OAuthRequestException {
        return OAuthServiceFactory.getOAuthService().getCurrentUser();
    }
    private io.fourcast.gae.model.user.User testUser(){
        User user = new User();
        user.setId(OAUTH_USER_ID);
        user.setEmail(OAUTH_EMAIL);
        user.setDisplayName(OAUTH_USER_ID);
        return user;
    }

}
