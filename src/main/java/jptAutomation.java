import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.context.ApplicationContext;
import com.launchdarkly.client.*;
import java.io.IOException;
import static java.util.Collections.singletonList;


@RestController
@EnableAutoConfiguration
public class jptAutomation {

	public static LDClient ldClient = null;

	@RequestMapping("/")
	String home() {
		return "Welcome to JPT Release version generator..";
	}

	//IAE-0
	@RequestMapping(value = "/rel", method = RequestMethod.GET)
	@ResponseBody
	String getRel(long nAppId) {
		//updated a new release version
		if(nAppId == 1) //Web
			return getWebRelVersion()+"."+getBuildVersion();
		if(nAppId == 2) //API
			return getAPIRelVersion()+"."+getBuildVersion();
		
		return getRelVersion()+"."+getBuildVersion();	
	}

	String getWebRelVersion() {
		//updated a new build version
		return "01";
	}

	String getAPIRelVersion() {
		//updated a new build version
		return "02";
	}

	//IAE-1
	@RequestMapping(value = "/rel/version", method = RequestMethod.GET)
	@ResponseBody
	String getRelVersion() {

			//LDUser user = new LDUser("dinesh@gsa.gov");
   LDUser user = new LDUser.Builder("bob@example.com")
                           .firstName("Bob")
                           .lastName("Loblaw")
                           .customString("groups", singletonList("beta_testers"))
                           .build();

			boolean showFeature = ldClient.boolVariation("immutable-demo", user, false);


			if (showFeature) {
			// application code to show the feature 
				return "Rel. 28 (The feature flag is turned ON)";
			}
			else {
			// the code to run if the feature is off
				return "Rel. 27 (The feature flag is turned OFF)";
			}
	}

	//IAE-2
	@RequestMapping(value = "/rel/build", method = RequestMethod.GET)
	@ResponseBody
	String getBuildVersion() {
		//updated a new build version
		return "05";
	}

	//IAE-3
	@RequestMapping(value = "/rel/patch", method = RequestMethod.GET)
	@ResponseBody
	String getPatchVersion() {
		//updated a new patch version
		return "08";
	}

	//IAE-3
	@RequestMapping(value = "/rel/team", method = RequestMethod.GET)
	@ResponseBody
	String getTeamName(int nTeamId) {
		//updated a new patch version
		if(nTeamId == 1)
			return "IT";
		if(nTeamId == 2)
			return "MGMT";
		return "SUPPORT";
	}

	@RequestMapping(value = "/rel/colorteam", method = RequestMethod.GET)
	@ResponseBody
	String getColorTeamName(int nTeamId) {
		//updated a new patch version
		if(nTeamId == 1)
			return "Copper";
		if(nTeamId == 2)
			return "Black";
		return "Blue";
	}

	// public static void main(String[] args) {

	// 	 SpringApplication.run(jptAutomation.class, args);
	// }

	public static void main(String[] args) throws IOException {

			//ApplicationContext ctx = SpringApplication.run(jptAutomation.class, args);
			LDClient ldClient = new LDClient("sdk-152bd4e4-5e04-439e-828b-1c8b4955d045");
			//app.ldClient = ldClient;
			jptAutomation.ldClient = ldClient;

			SpringApplication.run(jptAutomation.class, args);

			//ldClient.flush();
			//ldClient.close();

	}

}