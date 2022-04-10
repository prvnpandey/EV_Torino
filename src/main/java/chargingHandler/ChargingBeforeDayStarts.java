package chargingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.contrib.ev.MobsimScopeEventHandling;

import org.matsim.contrib.ev.fleet.Battery;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;

import com.google.inject.Inject;

import org.matsim.core.utils.misc.Time;

public class ChargingBeforeDayStarts implements  ActivityStartEventHandler, ActivityEndEventHandler,  MobsimScopeEventHandler {
	
	public static final String STARTING = "Home";
	public static final String ENDING = "Home";
	private Map<Id<Person>, Double> VehicleUsed = new HashMap<>();
	private Map<Id<ElectricVehicle>, ImmutablePair> Home_charge = new HashMap<>();
	private final ElectricFleet electricFleet;
	private List<HomeChargeLogEntry> logList = new ArrayList<>();
	
	@Inject
	public ChargingBeforeDayStarts( ElectricFleet electricFleet, MobsimScopeEventHandling events) {
		this.electricFleet = electricFleet;
		events.addMobsimScopeHandler(this);
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		String acttype = event.getActType();
		if(acttype == STARTING) {
			VehicleUsed.put(event.getPersonId(), event.getTime());
			Id<Person> vehicleId = event.getPersonId();
			if (vehicleId != null) {
				Id<ElectricVehicle> evId = Id.create(vehicleId, ElectricVehicle.class);
				if (electricFleet.getElectricVehicles().containsKey(evId)) {
				
					ElectricVehicle ev = electricFleet.getElectricVehicles().get(evId);
					Battery battery = ev.getBattery(); 
					if(battery.getSoc() < battery.getCapacity()) {
						// Home  charging speed at 2kWh
						double Charging_rate_perSec = EvUnits.kWh_to_J(2.0)/3600; //joule/sec
						double AvailableTimeForCharging = event.getTime();
						double LastSoc = battery.getSoc();
						double batteryToCharge = battery.getCapacity() - battery.getSoc();
						double TimetoCharge = batteryToCharge / Charging_rate_perSec;
						if(TimetoCharge < AvailableTimeForCharging) {
						double energycalc = TimetoCharge * Charging_rate_perSec;
						Home_charge.put(evId, new ImmutablePair<>(TimetoCharge, energycalc));
						
						}else {
								Home_charge.put(evId, new ImmutablePair<>(AvailableTimeForCharging, AvailableTimeForCharging*Charging_rate_perSec));
								} 
						
						double update_soc = (double) Home_charge.get(evId).getRight();
//						System.out.println("The battery capacity is : "+ EvUnits.J_to_kWh(battery.getCapacity())+ " The updated soc is :" + EvUnits.J_to_kWh(update_soc) );
						battery.changeSoc(update_soc);
						HomeChargeLogEntry loge = new HomeChargeLogEntry(evId,event.getTime(), (double)Home_charge.get(evId).getLeft(), (double)Home_charge.get(evId).getRight() , LastSoc, battery.getSoc());
						logList.add(loge);
					}
				}
			}
		}		
	}
		

	@Override
	public void handleEvent(ActivityStartEvent event) {
		String acttype = event.getActType();
		if(acttype == ENDING) {
			VehicleUsed.put(event.getPersonId(), event.getTime());
			Id<Person> vehicleId = event.getPersonId();
			if (vehicleId != null) {
				Id<ElectricVehicle> evId = Id.create(vehicleId, ElectricVehicle.class);
				if (electricFleet.getElectricVehicles().containsKey(evId)) {
				
					ElectricVehicle ev = electricFleet.getElectricVehicles().get(evId);
					Battery battery = ev.getBattery(); 
					if(battery.getSoc() < battery.getCapacity()) {
						// Home  charging speed at 2kWh
						double Charging_rate_perSec = EvUnits.kWh_to_J(2.0)/3600; //joule/sec
						// The time available for charging is day trip end time not the starting time
//						double AvailableTimeForCharging = 24*3600 - VehicleUsed.get(evId);
						double AvailableTimeForCharging = 24*3600 - event.getTime();
						double LastSoc = battery.getSoc();
						double batteryToCharge = battery.getCapacity() - battery.getSoc();
						double TimetoCharge = batteryToCharge / Charging_rate_perSec;
						if(AvailableTimeForCharging>0) {
							if(TimetoCharge < AvailableTimeForCharging) {
							double energycalc = TimetoCharge * Charging_rate_perSec;
							Home_charge.put(evId, new ImmutablePair<>(TimetoCharge, energycalc));
							
							}else if(AvailableTimeForCharging < 86400 ) {
								Home_charge.put(evId, new ImmutablePair<>(AvailableTimeForCharging, AvailableTimeForCharging*Charging_rate_perSec));
								}
							
							double update_soc = (double) Home_charge.get(evId).getRight();
	//						System.out.println("The battery capacity is : "+ EvUnits.J_to_kWh(battery.getCapacity())+ " The updated soc is :" + EvUnits.J_to_kWh(update_soc) );
							battery.changeSoc(update_soc);
							HomeChargeLogEntry loge = new HomeChargeLogEntry(evId,event.getTime(), (double)Home_charge.get(evId).getLeft(), (double)Home_charge.get(evId).getRight() , LastSoc, battery.getSoc());
							logList.add(loge);
						}
					}
				}
			}
		}
	}
	
	public List<HomeChargeLogEntry> getlog(){
		return logList;
	}
	
	public static class HomeChargeLogEntry implements Comparable<HomeChargeLogEntry> {
		private final double TimetoCharge;
		private final double Startingtime;
		private final double initialSoc;
		private final double finalSoc;
		private final double transmitted_Energy;
		private final Id<ElectricVehicle> vehicleId;
		static final String HEADER = "vehicleId;Startingtime;time;transmittedEnergy_kWh;initialSoc;finalSoc";

		public HomeChargeLogEntry(Id<ElectricVehicle> vehicleId,double Startingtime, double TimetoCharge, double transmitted_Energy, double initialSoc, double finalSoc) {
			this.Startingtime = Startingtime;
			this.TimetoCharge = TimetoCharge;
			this.initialSoc = initialSoc;
			this.finalSoc = finalSoc;
			this.transmitted_Energy = transmitted_Energy;
			this.vehicleId = vehicleId;
			
		}
		
		public Id<ElectricVehicle> getVehicleId() {
			return vehicleId;
		}
		
		public double getChargeTime() {
			return TimetoCharge;
		}
		
		public double getStartingtime() {
			return Startingtime;
		}
		
		public double initialSoc() {
			return initialSoc;
		}

		public double getTransmitted_Energy() {
			return transmitted_Energy;
		}
		
		public double finalSoc() {
			return finalSoc;
		}

		@Override
		public int compareTo(HomeChargeLogEntry o) {
			return Double.valueOf(TimetoCharge).compareTo(o.TimetoCharge) ;
		}
	}
}
