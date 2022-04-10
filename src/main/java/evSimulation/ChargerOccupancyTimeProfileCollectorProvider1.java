package evSimulation;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.charging.ChargingLogic;
import org.matsim.contrib.ev.charging.ChargingWithQueueingAndAssignmentLogic;
import org.matsim.contrib.ev.infrastructure.Charger;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructure;
import org.matsim.contrib.util.timeprofile.TimeProfileCharts.ChartType;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector;
import org.matsim.contrib.util.timeprofile.TimeProfileCollector.ProfileCalculator;
import org.matsim.contrib.util.timeprofile.TimeProfiles;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ChargerOccupancyTimeProfileCollectorProvider1 implements Provider<MobsimListener> {
	private final ChargingInfrastructure chargingInfrastructure;
	private final MatsimServices matsimServices;

	@Inject
	public ChargerOccupancyTimeProfileCollectorProvider1(ChargingInfrastructure chargingInfrastructure,
			MatsimServices matsimServices) {
		this.chargingInfrastructure = chargingInfrastructure;
		this.matsimServices = matsimServices;
	}

	@Override
	public MobsimListener get() {
		ProfileCalculator calc = createChargerOccupancyCalculator(chargingInfrastructure);
		TimeProfileCollector collector = new TimeProfileCollector(calc, 3600, "charger_occupancy_time_profiles",
				matsimServices);
		collector.setChartTypes(ChartType.Line, ChartType.StackedArea);
		return collector;
	}

	public static ProfileCalculator createChargerOccupancyCalculator(
			final ChargingInfrastructure chargingInfrastructure) {
		String[] header = { "plugged", "queued", "assigned" };
		return TimeProfiles.createProfileCalculator(header, () -> {
			int plugged = 0;
			int queued = 0;
			int assigned = 0;
			for (Charger c : chargingInfrastructure.getChargers().values()) {
				Id<Charger> chargerid = c.getId();
				
				ChargingLogic logic = c.getLogic();				
				plugged += logic.getPluggedVehicles().size();
				queued += logic.getQueuedVehicles().size();
				if (logic instanceof ChargingWithQueueingAndAssignmentLogic) {
					assigned += ((ChargingWithQueueingAndAssignmentLogic)logic).getAssignedVehicles().size();
				}
			}
			return new Integer[] { plugged, queued, assigned };
		});
	}
}
