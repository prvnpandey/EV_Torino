package stats;

import com.google.inject.Inject;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import chargingHandler.ChargingBeforeDayStarts;
import evSimulation.EvNetworkRoutingModule_new;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.discharging.DriveDischargingHandler;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricFleetWriter;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.ev.fleet.ElectricVehicleSpecification;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.core.utils.misc.Time;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EV_stats implements MobsimBeforeCleanupListener {

	@Inject
	DriveDischargingHandler driveDischargingHandler;
	@Inject
	ChargingPowerCollector ChargingPowerCollector;
	@Inject
	ChargingBeforeDayStarts ChargingBeforeDayStarts;
	@Inject
	OutputDirectoryHierarchy controlerIO;
	@Inject
	IterationCounter iterationCounter;
	@Inject
	Network network;
	@Inject
	private ElectricFleet electricFleet;
	@Override
	public void notifyMobsimBeforeCleanup(MobsimBeforeCleanupEvent   event) {
		
		DecimalFormat format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setMinimumIntegerDigits(1);
		format.setMaximumFractionDigits(2);//XXX this is asymmetric to reading vehicles
		format.setGroupingUsed(false);



		try {
			CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getOutputFilename("charging_stat.csv"))), CSVFormat.DEFAULT.withDelimiter(';').
					withHeader("ChargerId", "chargeStartTime", "chargeEndTime", "ChargingDuration", "xCoord", "yCoord","VehicleID", "energyTransmitted_kWh","BeginSoc", "Waitingtime"));
			for (ChargingPowerCollector.ChargingLogEntry e : ChargingPowerCollector.getLogList()) {
				double energyKWh = Math.round(EvUnits.J_to_kWh(e.getTransmitted_Energy()) * 10.) / 10.;
				double BeginSoc = Math.round(EvUnits.J_to_kWh(e.chargeBeginSoc()) * 10.) / 10.;
				csvPrinter.printRecord(e.getCharger().getId(), Time.writeTime(e.getChargeStart()), Time.writeTime(e.getChargeEnd()), Time.writeTime(e.getChargeEnd() - e.getChargeStart()),
						e.getCharger().getCoord().getX(), e.getCharger().getCoord().getY(),e.getVehicleId(), energyKWh, BeginSoc, Time.writeTime(e.getWaitingTime()));
			}
			csvPrinter.close();

			CSVPrinter csvPrinter2 = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getIterationFilename(iterationCounter.getIterationNumber(), "evConsumptionPerLink.csv"))), CSVFormat.DEFAULT.withDelimiter(';').withHeader("Link", "TotalConsumptionPerKm", "TotalConsumption"));
			for (Map.Entry<Id<Link>, Double> e : driveDischargingHandler.getEnergyConsumptionPerLink().entrySet()) {
				csvPrinter2.printRecord(e.getKey(), (EvUnits.J_to_kWh(e.getValue())) / (network.getLinks()
						.get(e.getKey())
						.getLength() / 1000.0), EvUnits.J_to_kWh(e.getValue()));
			}
			csvPrinter2.close();
			
			CSVPrinter csvPrinter3 = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getOutputFilename("EV_file.csv"))), CSVFormat.DEFAULT.withDelimiter(';').
					withHeader("VehicleId", "Battery_cap","Final_soc", "vehicle_type", "Charger_types"));
			for(Entry<Id<ElectricVehicle>, ElectricVehicle> e : electricFleet.getElectricVehicles().entrySet() ) {
				csvPrinter3.printRecord(e.getKey(), 
						EvUnits.J_to_kWh(e.getValue().getBattery().getCapacity()), 
						format.format(EvUnits.J_to_kWh(e.getValue().getBattery().getSoc())), 
						e.getValue().getVehicleType(), 
						"default");
			}
			csvPrinter3.close();
		
			CSVPrinter csvPrinter4 = new CSVPrinter(Files.newBufferedWriter(Paths.get(controlerIO.getOutputFilename("HomeCharging.csv"))), CSVFormat.DEFAULT.withDelimiter(';').
					withHeader("VehicleId","ChargingStartTime", "time","transmittedEnergy_kWh", "initialSoc", "finalSoc"));
			for(ChargingBeforeDayStarts.HomeChargeLogEntry e : ChargingBeforeDayStarts.getlog() ) {
				csvPrinter4.printRecord(e.getVehicleId(), 
						Time.writeTime(e.getStartingtime()),
						Time.writeTime(e.getChargeTime()), 
						format.format(EvUnits.J_to_kWh(e.getTransmitted_Energy())), 
						EvUnits.J_to_kWh(e.initialSoc()), 
						EvUnits.J_to_kWh(e.finalSoc()));
			}
			csvPrinter4.close();
			
			String filename = "D:\\Floating car data\\charger_location\\Vehicle file 10pct\\Random_plans_selected\\ev.xml";
			VehicleWriter VehicleWriter = new VehicleWriter(electricFleet,filename);
			VehicleWriter.WriteVehicle();	
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	

}
