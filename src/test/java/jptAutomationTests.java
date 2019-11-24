
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.actuate.autoconfigure.cloudfoundry.HealthEndpointCloudFoundryExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes={jptAutomation.class})
//@ContextConfiguration(classes={jptAutomation.class})
public class jptAutomationTests {
	
	@Autowired
	private jptAutomation  m_objAutomation;

	@Test
	public void test_getPatchVersion(){
		assertEquals("Validated getPatch() API","08",m_objAutomation.getPatchVersion());
	}

	@Test
	public void test_getBuildVersion(){
		assertEquals("Validated getBuildVersion() API","05",m_objAutomation.getBuildVersion());
	}

	@Test
	public void test_home(){
		assertNotNull(m_objAutomation.home());
	}

	@Test
	public void test_getRel(){
		assertNotNull(m_objAutomation.getRel(0));
	}

	@Test
	public void test_getTeamName(){
		assertEquals("Validated getTeamName() API","SUPPORT",m_objAutomation.getTeamName(0));
		assertEquals("Validated getTeamName() API","MGMT",m_objAutomation.getTeamName(2));
	}

	/*
	@Test
	public void test_getColorTeamName(){
		assertEquals("Validated getTeamName() API","Blue",m_objAutomation.getColorTeamName(0));
		assertEquals("Validated getTeamName() API","Black",m_objAutomation.getColorTeamName(2));
	}
	*/
	

}


/// Step #2. 
/// Step #3. 
// mvn test sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=1805353f12ac75d907948da15a5e3139c17d2dd9
/*
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=1805353f12ac75d907948da15a5e3139c17d2dd9
  */