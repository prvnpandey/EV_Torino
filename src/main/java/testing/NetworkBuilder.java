package testing;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCalcTopoType;
import org.matsim.core.network.algorithms.NetworkMergeDoubleLinks;
import org.matsim.core.network.algorithms.NetworkSegmentDoubleLinks;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.algorithms.intersectionSimplifier.IntersectionSimplifier;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.run.NetworkCleaner;


public class NetworkBuilder {
	final private static Logger LOG = Logger.getLogger(Main.class);
	private static final String String = null;
	
	 public static void main(String[] args) {

		 /*
			 * The input file name.
			 */
		 	String osm = "D:\\Floating car data\\Network\\OSM with road network\\finalnetwork.osm";
		 	String outputNetworkFile = "D:\\Floating car data\\Network\\OSM with road network\\network3.xml";
		    String network_file = "D:\\Floating car data\\Network\\Final network\\NEtwork XMl files\\network.xml";
			String clean_network = "D:\\Floating car data\\Network\\Final network\\NEtwork XMl files\\NC_network.xml";
			String intersectionSimplifier_network = "D:\\Floating car data\\Network\\Final network\\IS_network.xml";
			String osmFilename = "D:\\Floating car data\\Network\\nord-ovest-latest.osm_01.osm";
			
			
			/*
			 * The coordinate system to use. OpenStreetMap uses WGS84, but for MATSim, we need a projection where distances
			 * are (roughly) euclidean distances in meters.
			 * 
			 * UTM 33N is one such possibility (for parts of Europe, at least).
			 * 
			 */
//			CoordinateTransformation ct = 
//				 TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.WGS84_UTM33N);
//			
//			Config config = ConfigUtils.createConfig();
//			Scenario scenario = ScenarioUtils.createScenario(config);
//			Network network = scenario.getNetwork();
//			
//			OsmNetworkReader onr = new OsmNetworkReader(network,ct);
//			onr.parse(osm);
//			new NetworkCleaner().run(osm, outputNetworkFile);
			
			/*
			 * Write the Network to a MATSim network file.
			 */
//			new NetworkWriter(network).write("./input/network.xml");
			
			/*
			 * First, create a new Config and a new Scenario. One always has to do this when working with the MATSim 
			 * data containers.
			 * 
			 */
			
			Network network = NetworkUtils.createNetwork();
			new MatsimNetworkReader(network).readFile(clean_network);
			
//			NetworkSimplifier ns = new NetworkSimplifier();
//			boolean ms = true;
//			ns.setMergeLinkStats(ms);
//			ns.run(network);
//			NetworkCleaner ncr = new NetworkCleaner();
//			ncr.run(network_file, clean_network);
			
		
			
//			IntersectionSimplifier is = new IntersectionSimplifier(30.0, 2);
//			Network newNetwork = is.simplify(network);
//			NetworkCalcTopoType nct = new NetworkCalcTopoType();
//			nct.run(newNetwork);
			
//			LOG.info("Simplifying the network...");
//			new NetworkSimplifier().run(network);
//			LOG.info("Cleaning the network...");
//			new NetworkCleaner().run(network_file, clean_network);
//			String cap = "ADDITIVE";
			NetworkMergeDoubleLinks NMDL = new NetworkMergeDoubleLinks(NetworkMergeDoubleLinks.MergeType.MAXIMUM);
			NMDL.run(network);
//			IntersectionSimplifier.reportNetworkStatistics(newNetwork);
			new NetworkWriter(network).write("D:\\Floating car data\\Network\\Final network\\NEtwork XMl files\\NC_network.xml");
	 }
}
