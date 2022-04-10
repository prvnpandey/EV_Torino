package testing;


import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.TimeVariantLink;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class Main {
	final private static Logger LOG = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		
				
				LOG.info("Starting of main class of matsim program");
				int i =1;
				int j =1;
				double center_x = 1394743.8157114468;
				double center_y = 4991523.933084457;
				double radius = 10000.0;


				double x_min = center_x - radius; 
				double x_max = center_x + radius;
				double y_min = center_y - radius; 
				double y_max = center_y + radius ;
				LocalDate today = LocalDate.now( ZoneId.of( "Europe/Paris" ) ) ;
				String date = today.toString() ; 
				final String network = "D:\\Floating car data\\Network\\Final network\\NEtwork XMl files\\Final_network.xml";
				final String plan = "D:\\Floating car data\\plans\\Day Plans\\Monday.xml";
				final String currentDir = "D:\\Floating car data\\Config\\";
				LOG.warn("this is main program directory"+ currentDir );
				Config config = ConfigUtils.loadConfig(currentDir+"config.xml");
				config.plans().setInputFile(plan);
				config.network().setInputFile(network);
				config.controler().setLastIteration(j);
//				config.vehicles().setVehiclesFile("D:\\Floating car data\\charger_location\\vehiclefile.xml");
				config.qsim().setFlowCapFactor(1.0);
				config.qsim().setStorageCapFactor(1.0);

				  
				Scenario sc = ScenarioUtils.loadScenario(config);
				


				Controler controler = new Controler(sc);
				final String DIR =  "D:\\Floating car data\\Matsim_output\\Final Simulation\\";
				controler.getConfig().controler().setOutputDirectory(DIR + "monday_defaultspeed");
				controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
				controler.run();
						
	}
}
