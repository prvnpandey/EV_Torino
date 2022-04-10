package evSimulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.fleet.Battery;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector.ProfileCalculator;
import org.matsim.contrib.util.timeprofile.TimeProfiles;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.vehicles.Vehicle;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class IndividualSocTimeProfileCollectorProvider1 implements Provider<MobsimListener> {
	private final ElectricFleet evFleet;
	private final MatsimServices matsimServices;

	@Inject
	public IndividualSocTimeProfileCollectorProvider1(ElectricFleet evFleet, MatsimServices matsimServices) {
		this.evFleet = evFleet;
		this.matsimServices = matsimServices;
	}

	@Override
	public MobsimListener get() {
		ProfileCalculator calc = createIndividualSocCalculator(evFleet);
		return new TimeProfileCollector(calc, 9600, "individual_soc_time_profiles", matsimServices);
	}

	private static final int MAX_VEHICLE_COLUMNS = 50;

	public static ProfileCalculator createIndividualSocCalculator(final ElectricFleet evFleet2) {
		int vehicles = evFleet2.getElectricVehicles().size();
		int columns = Math.max(vehicles, MAX_VEHICLE_COLUMNS);
		List<ElectricVehicle> allEvs = new ArrayList<>();
		allEvs.addAll(evFleet2.getElectricVehicles().values());
		Collections.shuffle(allEvs);
		List<ElectricVehicle> selectedEvs = allEvs.stream().limit(columns).collect(Collectors.toList());

		String[] header = selectedEvs.stream().map(ev -> ev.getId() + "").toArray(String[]::new);

		return TimeProfiles.createProfileCalculator(header, () -> {
			return selectedEvs.stream().map(ev -> EvUnits.J_to_kWh(ev.getBattery().getSoc()))/*in [kWh]*/
					.toArray(Double[]::new);
		});
	}
	
}

