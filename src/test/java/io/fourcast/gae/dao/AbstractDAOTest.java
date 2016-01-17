package io.fourcast.gae.dao;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@SuppressWarnings("nls")
public class AbstractDAOTest {

	public final static LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalUserServiceTestConfig(),
			new LocalDatastoreServiceTestConfig()
					.setDefaultHighRepJobPolicyUnappliedJobPercentage(100.0f)
					.setNoIndexAutoGen(true));

	final Logger log = Logger.getLogger(AbstractDAOTest.class.getName());

	private Closeable closeable;

	@BeforeClass
	public static void setupLogging() throws IOException {
		InputStream fis = AbstractDAOTest.class
				.getResourceAsStream("/logging.unit.properties");
		LogManager.getLogManager().readConfiguration(fis);
		fis.close();
	}

	@Before
	public void setup() {
		helper.setEnforceApiDeadlines(true);
		helper.setSimulateProdLatencies(true);
		helper.setUp();
		
		closeable = ObjectifyService.begin();
	}


	public void login(String email) {
		helper.setEnvEmail(email);
		helper.setEnvAuthDomain("gmail.com");
	}

	@After
	public void tearDown() {
		closeable.close();
		helper.tearDown();
	}

}
