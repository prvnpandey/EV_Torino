package testing;


import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.network.algorithms.NetworkCalcTopoType;
import org.matsim.core.network.algorithms.NetworkSegmentDoubleLinks;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.algorithms.intersectionSimplifier.IntersectionSimplifier;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.NetworkCleaner;



public class NetwokLinkMerge {
	final private static Logger LOG = Logger.getLogger(NetwokLinkMerge.class);

	public static void main(String[] args) {
		


				
				
				LOG.info("Starting of main class of matsim program");
				int i =1;
				int j =1;
				String currentDir = "D:\\thesis\\TORINO_MATSIM_SIMULATION\\workingDir\\scenario_afterCleaningNetwork\\";
				LOG.warn("this is main program directory"+ currentDir );
				String output = "D:\\thesis\\TORINO_MATSIM_SIMULATION\\workingDir\\output#.xml";
				String outputNetworkFile = "D:\\thesis\\TORINO_MATSIM_SIMULATION\\workingDir\\clean_network.xml";
				String inputNetworkFile = "D:\\thesis\\TORINO_MATSIM_SIMULATION\\workingDir\\Torino_Roadnetworks\\network_withoutTertiarylink.xml";
				Config config = ConfigUtils.loadConfig(currentDir+"config.xml");
				
				config.linkStats().setWriteLinkStatsInterval(i);
				config.linkStats().setAverageLinkStatsOverIterations(i);
				config.controler().setCreateGraphs(true);
				config.controler().setWritePlansInterval(i);
				config.counts().getAnalyzedModes();
				config.counts().setWriteCountsInterval(i);
				
				Scenario sc = ScenarioUtils.loadScenario(config);
				Network network = sc.getNetwork();
				NetworkSegmentDoubleLinks ngs = new NetworkSegmentDoubleLinks();
				ngs.run(network);
				new NetworkWriter(network);
				IntersectionSimplifier ns = new IntersectionSimplifier(30.0, 4);
				Network newNetwork = ns.simplify(network);
				NetworkCalcTopoType nct = new NetworkCalcTopoType();
				nct.run(newNetwork);
				LOG.info("Simplifying the network...");
				new NetworkSimplifier().run(network);
				
				new NetworkSimplifier().run(newNetwork);
				LOG.info("Cleaning the network...");
				new NetworkCleaner().run(inputNetworkFile, outputNetworkFile);
				IntersectionSimplifier.reportNetworkStatistics(newNetwork);
				new NetworkWriter(newNetwork).write(output);
				
						
	}

}
