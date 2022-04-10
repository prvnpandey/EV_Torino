package stats;

import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.MobsimScopeEventHandler;
import org.matsim.contrib.ev.MobsimScopeEventHandling;
import org.matsim.contrib.ev.charging.ChargingEndEvent;
import org.matsim.contrib.ev.charging.ChargingEndEventHandler;
import org.matsim.contrib.ev.charging.ChargingStartEvent;
import org.matsim.contrib.ev.charging.ChargingStartEventHandler;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.ev.infrastructure.Charger;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructure;
import org.matsim.core.utils.misc.Time;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChargingPowerCollector
		implements ActivityStartEventHandler, ChargingStartEventHandler, ChargingEndEventHandler, MobsimScopeEventHandler {
	
	public static final String CHARGING_IDENTIFIER = " charging interaction";
	private final ChargingInfrastructure chargingInfrastructure;
	private final ElectricFleet fleet;
	private Map<Id<ElectricVehicle>, ImmutablePair> chargeBeginSoc = new HashMap<>();
	private Map<Id<Person>, Double> waitingBeforeCharge = new HashMap<>();

	private List<ChargingLogEntry> logList = new ArrayList<>();

	@Inject
	public ChargingPowerCollector(ElectricFleet fleet, ChargingInfrastructure chargingInfrastructure,
			MobsimScopeEventHandling events) {
		this.fleet = fleet;
		this.chargingInfrastructure = chargingInfrastructure;
		events.addMobsimScopeHandler(this);
	}

	@Override
	public void handleEvent(ChargingEndEvent event) {
		Double BeginTime = waitingBeforeCharge.remove(event.getVehicleId());
		ImmutablePair<Double, Double> chargeStart = chargeBeginSoc.remove(event.getVehicleId());
		if (chargeStart != null) {
			double energy = this.fleet.getElectricVehicles().get(event.getVehicleId()).getBattery().getSoc()
					- chargeStart.getValue();
			double waitingTime = chargeStart.getKey() - BeginTime;
			ChargingLogEntry loge = new ChargingLogEntry(chargeStart.getKey(), waitingTime, event.getTime(),
					chargingInfrastructure.getChargers().get(event.getChargerId()), energy, event.getVehicleId(), chargeStart.getValue());
			logList.add(loge);
		} else
			throw new NullPointerException(event.getVehicleId().toString() + " has never started charging");
	}

	@Override
	public void handleEvent(ChargingStartEvent event) {
		ElectricVehicle ev = this.fleet.getElectricVehicles().get(event.getVehicleId());
		if (ev != null) {
			this.chargeBeginSoc.put(event.getVehicleId(),
					new ImmutablePair<>(event.getTime(), ev.getBattery().getSoc()));
		} else
			throw new NullPointerException(event.getVehicleId().toString() + " is not in list");

	}
	
	@Override
	public void handleEvent(ActivityStartEvent event) {
		if (event.getActType().endsWith(CHARGING_IDENTIFIER)) {
			ElectricVehicle ev = this.fleet.getElectricVehicles().get(event.getPersonId());
			if (ev != null) {
				this.waitingBeforeCharge.put(event.getPersonId(), event.getTime());
			} else
				throw new NullPointerException(event.getPersonId().toString() + " is not in list");
		}
	}

	public List<ChargingLogEntry> getLogList() {
		return logList;
	}

	public static class ChargingLogEntry implements Comparable<ChargingLogEntry> {
		private final double chargeStart;
		private final double waitingTime;
		private final double chargeEnd;
		private final Charger charger;
		private final double transmitted_Energy;
		private final Id<ElectricVehicle> vehicleId;
		private double BeginSoc;
		static final String HEADER = "chargerId;chargingStart;chargingEnd;chargingDuration;waitingDuration;chargerX;chargerY;vehicleId;transmittedEnergy_kWh;chargeBeginSoc";

		public ChargingLogEntry(double chargeStart,double waitingTime, double chargeEnd, Charger charger, double transmitted_Energy,
				Id<ElectricVehicle> vehicleId, double BeginSoc) {
			this.chargeStart = chargeStart;
			this.waitingTime = waitingTime;
			this.chargeEnd = chargeEnd;
			this.charger = charger;
			this.transmitted_Energy = transmitted_Energy;
			this.vehicleId = vehicleId;
			this.BeginSoc = BeginSoc;
		}

		public double getChargeStart() {
			return chargeStart;
		}
		
		public double getWaitingTime() {
			return waitingTime;
		}

		public double getChargeEnd() {
			return chargeEnd;
		}

		public Charger getCharger() {
			return charger;
		}

		public double getTransmitted_Energy() {
			return transmitted_Energy;
		}

		@Override
		public String toString() {
			double energyKWh = Math.round(EvUnits.J_to_kWh(transmitted_Energy) * 10.) / 10.;
			return charger.getId().toString()
					+ ";"
					+ Time.writeTime(chargeStart)
					+ ";"
					+ Time.writeTime(chargeEnd)
					+ ";"
					+ Time.writeTime(chargeEnd - chargeStart)
					+ ";"
					+ charger.getCoord().getX()
					+ ";"
					+ charger.getCoord().getY()
					+ ";"
					+ vehicleId.toString()
					+ ";"
					+ energyKWh
					+ ";"
					+ BeginSoc
					+ ";"
					+ Time.writeTime(waitingTime);
		}

		@Override
		public int compareTo(ChargingLogEntry o) {
			return Double.valueOf(chargeStart).compareTo(o.chargeStart);
		}

		public Id<ElectricVehicle> getVehicleId() {
			return vehicleId;
		}
		
		public double chargeBeginSoc() {
			return BeginSoc;
		}
	}
}
