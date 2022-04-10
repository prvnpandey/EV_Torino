/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package evSimulation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.discharging.AuxEnergyConsumption;
import org.matsim.contrib.ev.discharging.DriveEnergyConsumption;
import org.matsim.contrib.ev.fleet.Battery;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricFleetSpecification;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.ev.fleet.ElectricVehicleImpl;
import org.matsim.contrib.ev.fleet.ElectricVehicleSpecification;
import org.matsim.contrib.ev.infrastructure.ChargerSpecification;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructureSpecification;
import org.matsim.contrib.util.StraightLineKnnFinder;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.Facility;

 

/**
 * This network Routing module adds stages for re-charging into the Route.
 * This wraps a "computer science" {@link LeastCostPathCalculator}, which routes from a node to another node, into something that
 * routes from a {@link Facility} to another {@link Facility}, as we need in MATSim.
 *
 * @author jfbischoff
 */

public final class EvNetworkRoutingModule_new implements RoutingModule {

	private final String mode;

	private final Network network;
	private final RoutingModule delegate;
	private final ElectricFleetSpecification electricFleet;
	private final ChargingInfrastructureSpecification chargingInfrastructureSpecification;
	private final Random random = MatsimRandom.getLocalInstance();
	private final TravelTime travelTime;
	private final DriveEnergyConsumption.Factory driveConsumptionFactory;
	private final AuxEnergyConsumption.Factory auxConsumptionFactory;
	private final String stageActivityType;
	private final String vehicleSuffix;
	private final EvConfigGroup evConfigGroup;
    
     	
	storingEnergyValues storeEnergy = new storingEnergyValues();
	public Map< Id<ElectricVehicle>, List<String> > trips = new HashMap < Id<ElectricVehicle>, List<String>>();
	public Map< String, List<Integer> > totalTrips = new HashMap < String, List<Integer>  >();
    
	
	public class consumptionValues{
		Double lastSoc;
		Map<Link, Double> estimatedEnergyConsumption;

	}

	public EvNetworkRoutingModule_new(final String mode, final Network network, RoutingModule delegate,
			ElectricFleetSpecification electricFleet,
			ChargingInfrastructureSpecification chargingInfrastructureSpecification, TravelTime travelTime,
			DriveEnergyConsumption.Factory driveConsumptionFactory, AuxEnergyConsumption.Factory auxConsumptionFactory,
			EvConfigGroup evConfigGroup) {
		this.travelTime = travelTime;
		Gbl.assertNotNull(network);
		this.delegate = delegate;
		this.network = network;
		this.mode = mode;
		this.electricFleet = electricFleet;
		this.chargingInfrastructureSpecification = chargingInfrastructureSpecification;
		this.driveConsumptionFactory = driveConsumptionFactory;
		this.auxConsumptionFactory = auxConsumptionFactory;
		stageActivityType = mode + VehicleChargingHandler.CHARGING_IDENTIFIER;
		this.evConfigGroup = evConfigGroup;
		this.vehicleSuffix = mode.equals(TransportMode.car) ? "" : "_" + mode;
		
	}

	@Override
	public List<? extends PlanElement> calcRoute(final Facility fromFacility, final Facility toFacility,
			final double departureTime, final Person person) {
		
		List<? extends PlanElement> basicRoute = delegate.calcRoute(fromFacility, toFacility, departureTime, person);
		Id<ElectricVehicle> evId = Id.create(person.getId() + vehicleSuffix, ElectricVehicle.class);
		if (!electricFleet.getVehicleSpecifications().containsKey(evId)) {
			return basicRoute;
		} else {
			Leg basicLeg = (Leg)basicRoute.get(0);
			ElectricVehicleSpecification ev = electricFleet.getVehicleSpecifications().get(evId);
			
			Map<Id<ElectricVehicle>, ElectricVehicle> veh = new HashMap<>();
			List<String[]> tripEnergy = new ArrayList<>();
			
			Map<Link, Double> estimatedEnergyConsumption = estimateConsumption(ev, basicLeg);

			double estimatedOverallConsumption = estimatedEnergyConsumption.values()
					.stream()
					.mapToDouble(Number::doubleValue)
					.sum();
			
// ------------------------------------------------------------------------------------------------------>				
			if (!totalTrips.containsKey("count"))
			{
				totalTrips.put("count", new ArrayList<Integer>());
				totalTrips.get("count").add(0);
				totalTrips.put("currentCount", new ArrayList<Integer>());
				totalTrips.get("currentCount").add(0);
				totalTrips.put("popCount", new ArrayList<Integer>());
				String currentDir = "D:\\Floating car data\\charger_location\\";
		        Config config = ConfigUtils.loadConfig( currentDir+"config.xml") ;
				Scenario scenario = ScenarioUtils.loadScenario(config);
				int pop = scenario.getPopulation().getPersons().size();
				totalTrips.get("popCount").add(pop);				
//				System.out.println(totalTrips.get("popCount").toString());
			}
			
			if (trips.containsKey(evId))
			{
				String leg = basicLeg.getDepartureTime() +"_"+EvUnits.J_to_kWh(estimatedOverallConsumption);
	            trips.get(evId).add(leg);
				int curCnt = totalTrips.get("currentCount").get(0) + 1;				
				totalTrips.get("currentCount").set(0, curCnt);				
			}
			else
			{
				trips.put(evId, new ArrayList<String>());
//				trips.get(evId).add((EvUnits.J_to_kWh(estimatedOverallConsumption)));
				int curCnt = totalTrips.get("currentCount").get(0) + 1;				
				totalTrips.get("currentCount").set(0, curCnt);
				int nCarLegs =0;
	            Plan plan = person.getSelectedPlan() ;
	            for ( Leg leg : TripStructureUtils.getLegs( plan ) ) {
	                if ( TransportMode.car.equals( leg.getMode() ) ) {
	                    nCarLegs++ ;
	                }
	            }
	            int tmpCount = (totalTrips.get("count").get(0) + nCarLegs - 1);
//	            trips.get(evId).add((double) (tmpCount));
	            
	            String leg = basicLeg.getDepartureTime() +"_"+EvUnits.J_to_kWh(estimatedOverallConsumption);
	            trips.get(evId).add(leg);
				totalTrips.get("count").set(0, tmpCount);
			}
			if((trips.size()==totalTrips.get("popCount").get(0)) | (totalTrips.get("currentCount").get(0)>=totalTrips.get("count").get(0))) {
//				Path p1 = Paths.get("D:/Floating car data/tripslog.csv");
				Path p2 = Paths.get("D:/Floating car data/Matsim_output/Final Simulation/KWH_COMP/tripslog_reduced_speed.txt");				
				
//				try(Writer writer = Files.newBufferedWriter(p1, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
//				    trips.forEach((key, value) -> {
//				        try { writer.write(key + ":" + value + System.lineSeparator()); }
//				        catch (IOException ex) { throw new UncheckedIOException(ex); }
//				    });
//				} catch(IOException ex) {  } 
				try(Writer writer = Files.newBufferedWriter(p2, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
				    trips.forEach((key, value) -> {
				        try { writer.write(key + ":" + value + System.lineSeparator()); }
				        catch (IOException ex) { throw new UncheckedIOException(ex); }
				    });
				} catch(IOException ex) {  } 
				trips.clear();
			}
			else {
//				System.out.println(trips.keySet());
//				System.out.println(trips.size());
//				System.out.println(totalTrips.get("count"));
//				System.out.println(totalTrips.get("currentCount"));
//				System.out.println(totalTrips.get("popCount"));
			}
// ------------------------------------------------------------------------------------------------------>			

			double capacity = ev.getInitialSoc() * (0.8 + random.nextDouble() * 0.18) ;
			boolean isKeyPresent = storeEnergy.getRemainingEnergy().containsKey(evId); 
			if(!isKeyPresent) {					
				storeEnergy.setRemainingEnergy(evId, capacity-estimatedOverallConsumption);
//				System.out.println("Value not present for vehicle:"+evId.toString()+
//									" created new entry with capacity:"+ storeEnergy.getRemainingEnergy().get(evId).toString());
			}
			else {
//				System.out.println("Value already present for vehicle:"+evId.toString()
//									+" "+storeEnergy.getRemainingEnergy().get(evId).toString());
				double curEnergy = storeEnergy.getRemainingEnergy().get(evId) ;
				capacity = curEnergy;				
				storeEnergy.remainingEnergy.replace(evId, capacity-estimatedOverallConsumption);
//				System.out.println("Value updated for vehicle:"+evId.toString()
//									+" "+storeEnergy.getRemainingEnergy().get(evId).toString());
			}
			
//			for (Id<ElectricVehicle> name: storeEnergy.getRemainingEnergy().keySet()){
//	            String key = name.toString();
//	            String value = storeEnergy.getRemainingEnergy().get(name).toString();  
//	            System.out.println(key + " " + value);  
//	            System.out.println(storeEnergy.toString());
//	} 
//			important line for SOC calculation
			double curEnergy = storeEnergy.getRemainingEnergy().get(evId) ;
			double BattToSocRatio = Math.abs(curEnergy/ev.getBatteryCapacity());
			//	        System.out.print(ev.getId() + " " + BattToSocRatio);
			double numberOfStops = Math.floor(estimatedOverallConsumption / capacity);
			//			if ((numberOfStops < 1) || (BattToSocRatio > 0.5 ) ) {
			if (numberOfStops < 1) {
				return basicRoute;
			} else {
				List<Link> stopLocations = new ArrayList<>();
				double currentConsumption = 0;
				for (Map.Entry<Link, Double> e : estimatedEnergyConsumption.entrySet()) {
					currentConsumption += e.getValue();
					if (currentConsumption > capacity) {
						stopLocations.add(e.getKey());
						currentConsumption = 0;
					}
				}
				List<PlanElement> stagedRoute = new ArrayList<>();
				Facility lastFrom = fromFacility;
				double lastArrivaltime = departureTime;
				for (Link stopLocation : stopLocations) {

					StraightLineKnnFinder<Link, ChargerSpecification> straightLineKnnFinder = new StraightLineKnnFinder<>(
							2, l -> l, s -> network.getLinks().get(s.getLinkId()));
					List<ChargerSpecification> nearestChargers = straightLineKnnFinder.findNearest(stopLocation,
							chargingInfrastructureSpecification.getChargerSpecifications()
									.values()
									.stream()
									.filter(charger -> ev.getChargerTypes().contains(charger.getChargerType())));
					ChargerSpecification selectedCharger = nearestChargers.get(random.nextInt(1));
					Link selectedChargerLink = network.getLinks().get(selectedCharger.getLinkId());
					Facility nexttoFacility = new LinkWrapperFacility(selectedChargerLink);
					if (nexttoFacility.getLinkId().equals(lastFrom.getLinkId())) {
						continue;
					}
					List<? extends PlanElement> routeSegment = delegate.calcRoute(lastFrom, nexttoFacility,
							lastArrivaltime, person);
					Leg lastLeg = (Leg)routeSegment.get(0);
					lastArrivaltime = lastLeg.getDepartureTime() + lastLeg.getTravelTime();
					stagedRoute.add(lastLeg);
					Activity chargeAct = PopulationUtils.createActivityFromCoordAndLinkId(stageActivityType,
							selectedChargerLink.getCoord(), selectedChargerLink.getId());
					double maxPowerEstimate = Math.min(selectedCharger.getPlugPower(), ev.getBatteryCapacity() / 3.6);
					double estimatedChargingTime = (ev.getBatteryCapacity() * 1.5) / maxPowerEstimate;
//					if(departureTime<24*3600) {}
					chargeAct.setMaximumDuration(Math.max(evConfigGroup.getMinimumChargeTime(), estimatedChargingTime));
					lastArrivaltime += chargeAct.getMaximumDuration();
					stagedRoute.add(chargeAct);
					lastFrom = nexttoFacility;}
				stagedRoute.addAll(delegate.calcRoute(lastFrom, toFacility, lastArrivaltime, person));

				return stagedRoute;

			}

		}
	}

	private Map<Link, Double> estimateConsumption(ElectricVehicleSpecification ev, Leg basicLeg) {
//		consumptionValues vs = new consumptionValues();
		Map<Link, Double> consumptions = new LinkedHashMap<>();
		NetworkRoute route = (NetworkRoute)basicLeg.getRoute();
		List<Link> links = NetworkUtils.getLinks(network, route.getLinkIds());
		ElectricVehicle pseudoVehicle = ElectricVehicleImpl.create(ev, driveConsumptionFactory, auxConsumptionFactory,
				v -> charger -> {
					throw new UnsupportedOperationException();
				});
		DriveEnergyConsumption driveEnergyConsumption = pseudoVehicle.getDriveEnergyConsumption();
		AuxEnergyConsumption auxEnergyConsumption = pseudoVehicle.getAuxEnergyConsumption();
//		double batterySoC = pseudoVehicle.getBattery().getSoc();
		double lastSoc = pseudoVehicle.getBattery().getSoc();
		for (Link l : links) {
			double travelT = travelTime.getLinkTravelTime(l, basicLeg.getDepartureTime(), null, null);

			double consumption = driveEnergyConsumption.calcEnergyConsumption(l, travelT, Time.getUndefinedTime())
					+ auxEnergyConsumption.calcEnergyConsumption(basicLeg.getDepartureTime(), travelT, l.getId());
			pseudoVehicle.getBattery().changeSoc(-consumption);
			double currentSoc = pseudoVehicle.getBattery().getSoc();
			// to accomodate for ERS, where energy charge is directly implemented in the consumption model
			double consumptionDiff = (lastSoc - currentSoc);
			lastSoc = currentSoc;
			consumptions.put(l, consumptionDiff);
		}
//		vs.lastSoc = batterySoC;
//		vs.estimatedEnergyConsumption = consumptions;
		return consumptions;
	}
	
	@Override
	public String toString() {
		return "[NetworkRoutingModule: mode=" + this.mode + "]";
	}

	

}
