package testing;

import java.util.ArrayList;
import java.util.List;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;
public class ODAnalysis {

	public static void main(String[] args) {
		String rootDirectory = null;
		final String runDirectory = rootDirectory + "runs-svn/sav-pricing-setupA/output_savA-0d/";
		final String runId = "savA-0d";
		final String shapeFile = rootDirectory + "public-svn/matsim/scenarios/countries/de/berlin/projects/avoev/berlin-sav-v5.2-10pct/input/shp-stadtteile-split-zone-3/Bezirksregionen_zone_GK4_fixed.shp";
		final String crs = "GK4";
		final double scaleFactor = 10.;
		final String[] helpLegModes = {TransportMode.transit_walk, TransportMode.non_network_walk, "access_walk", "egress_walk"}; // for backward compatibility
		final String stageActivitySubString = "interaction";
		final String zoneId = "NO";
		
		final List<String> modes = new ArrayList<>();
		modes.add(TransportMode.taxi);
		modes.add(TransportMode.car);
				
		EventsManager events = EventsUtils.createEventsManager();


		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(runDirectory + runId + ".output_events.xml.gz");
		
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(runDirectory + runId + ".output_network.xml.gz");
		config.network().setInputCRS(crs);
		config.global().setCoordinateSystem(crs);
		Network network = ScenarioUtils.loadScenario(config).getNetwork();
		
		ODAnalysis odAnalysis = new ODAnalysis();
		

	}

}
