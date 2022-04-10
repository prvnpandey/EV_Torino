package evSimulation;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
	import org.matsim.api.core.v01.Id;
	import org.matsim.api.core.v01.Scenario;
	import org.matsim.api.core.v01.TransportMode;
    import org.matsim.contrib.ev.EvConfigGroup;
	import org.matsim.contrib.ev.EvModule;
	import org.matsim.contrib.ev.charging.VehicleChargingHandler;
	import org.matsim.contrib.ev.discharging.AuxEnergyConsumption;
	import org.matsim.contrib.ev.discharging.DriveEnergyConsumption;
	import org.matsim.contrib.ev.discharging.VehicleTypeSpecificDriveEnergyConsumptionFactory;
	import org.matsim.contrib.ev.infrastructure.LTHConsumptionModelReader;
	import org.matsim.core.config.Config;
	import org.matsim.core.config.ConfigGroup;
	import org.matsim.core.config.ConfigUtils;
	import org.matsim.core.controler.AbstractModule;
	import org.matsim.core.controler.Controler;
	import org.matsim.core.controler.OutputDirectoryHierarchy;
	import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;
	import org.matsim.vehicles.VehicleType;

import chargingHandler.ChargingBeforeDayStarts;
import stats.ChargingPowerCollector;
import stats.EV_stats;
	/**
	 * Runs a sample EV run using a vehicle consumption model designed at LTH in Lund which takes the speed and the slope of a link into account.
	 * Link slopes may be added using a double array on the network.
	 * The consumption maps are based on Domingues, Gabriel. / Modeling, Optimization and Analysis of Electromobility Systems. Lund : Department of Biomedical Engineering, Lund university, 2018. 169 p., PhD thesis
	 */
	public class EV_LTHCONSUMPTION {
		private static final Logger LOG = Logger.getLogger(EV_LTHCONSUMPTION.class);
		
		public static void main(String[] args) {
			
//			List<String> weekday = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
			List<String> weekday = Arrays.asList("Monday");
			Iterator<String> dayIterator = weekday.iterator();
			while(dayIterator.hasNext()) {
				String Day = dayIterator.next();
			LOG.warn("The Current Simulation day is " + Day);
			
			final String  CitycarMap = "D:\\Floating car data\\charger_location\\city_consumption maps\\citycar.csv";
			final String  MidCarMap = "D:\\Floating car data\\charger_location\\city_consumption maps\\midcar.csv";
			final String  SuvCarMap = "D:\\Floating car data\\charger_location\\city_consumption maps\\suvmap.csv";

			LOG.info("Starting of main class of matsim program");
			int i =0;
			int j =10;
			String currentDir = "D:\\Floating car data\\charger_location\\";
			
			LOG.warn("this is main program directory"+ currentDir );
			EvConfigGroup evGroup = new EvConfigGroup();
			Config config = ConfigUtils.loadConfig(currentDir+"config.xml",evGroup);
			evGroup.setChargersFile(currentDir + "new_charger_locations\\charger.xml");
			String Vehcilefolder = "Vehicle file 10pct\\Random_plans_selected\\";
			
//			String Vehcilefile = "Monday_10pct";
			if(Day.equals("Monday")){
				
				evGroup.setVehiclesFile(currentDir + Vehcilefolder + Day + "_10pct" + ".xml");
				}else {
					evGroup.setVehiclesFile("D:\\Floating car data\\charger_location\\Vehicle file 10pct\\Random_plans_selected\\ev.xml" );
				}

//			String Day = "Tuesday";
//			evGroup.setVehiclesFile(currentDir + Vehcilefolder +"dummy_veh.xml");
//			evGroup.setVehiclesFile(currentDir + Vehcilefolder + Day + "_10pct" + ".xml");
			
//			config.plans().setInputFile("D:\\Floating car data\\plans\\Day Plans\\" + Day + ".xml");
			config.plans().setInputFile("D:\\Floating car data\\plans\\Day Plans\\Monday_withActDur_10pct.xml");
			config.network().setInputFile("D:\\Floating car data\\Network\\Final network\\NEtwork XMl files\\Final_network_withSlope.xml.gz");
			config.qsim().setEndTime(24*3600);
//			config.qsim().setSnapshotPeriod(1*3600);
//			config.qsim().setFlowCapFactor(0.1);
//			config.qsim().setStorageCapFactor(0.15);
			config.controler().setLastIteration(j);
			config.controler().setWritePlansInterval(1);
			config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
//			final String test_dir = "D:\\Floating car data\\Matsim_output\\Final Simulation\\test\\";
			final String Output_dir = "D:\\Floating car data\\Matsim_output\\Final Simulation\\";
			String folder = "EV_simulation_reducedspeed\\Simspeedcheking\\";
			config.controler().setOutputDirectory(Output_dir + folder + Day);
			Scenario scenario = ScenarioUtils.loadScenario(config);
			scenario.getNetwork().getNodes().forEach((node_id,node) -> {TimeVariantLinkImpl.changenetwork(node_id, node);});

			VehicleTypeSpecificDriveEnergyConsumptionFactory driveEnergyConsumptionFactory = new VehicleTypeSpecificDriveEnergyConsumptionFactory();
			driveEnergyConsumptionFactory.addEnergyConsumptionModelFactory("A", new LTHConsumptionModelReader(Id.create("A", VehicleType.class)).readURL(ConfigGroup.getInputFileURL(config.getContext(), CitycarMap)));
			driveEnergyConsumptionFactory.addEnergyConsumptionModelFactory("B", new LTHConsumptionModelReader(Id.create("B", VehicleType.class)).readURL(ConfigGroup.getInputFileURL(config.getContext(), CitycarMap)));
			driveEnergyConsumptionFactory.addEnergyConsumptionModelFactory("C", new LTHConsumptionModelReader(Id.create("C", VehicleType.class)).readURL(ConfigGroup.getInputFileURL(config.getContext(), MidCarMap)));
			driveEnergyConsumptionFactory.addEnergyConsumptionModelFactory("D", new LTHConsumptionModelReader(Id.create("D", VehicleType.class)).readURL(ConfigGroup.getInputFileURL(config.getContext(), SuvCarMap)));
			driveEnergyConsumptionFactory.addEnergyConsumptionModelFactory("E", new LTHConsumptionModelReader(Id.create("E", VehicleType.class)).readURL(ConfigGroup.getInputFileURL(config.getContext(), SuvCarMap)));
			driveEnergyConsumptionFactory.addEnergyConsumptionModelFactory("F", new LTHConsumptionModelReader(Id.create("F", VehicleType.class)).readURL(ConfigGroup.getInputFileURL(config.getContext(), SuvCarMap)));

			Controler controler = new Controler(scenario);
			controler.addOverridingModule(new EvModule());
			controler.addOverridingModule(new AbstractModule() {
				@Override
				public void install() {

					bind(DriveEnergyConsumption.Factory.class).toInstance(driveEnergyConsumptionFactory);
					bind(AuxEnergyConsumption.Factory.class).toInstance(electricVehicle -> (beginTime, duration, linkId) -> 0); //a dummy factory, as aux consumption is part of the drive consumption in the model
					addRoutingModuleBinding(TransportMode.car).toProvider(new EvNetworkRoutingProvider_new(TransportMode.car));
					
					installQSimModule(new AbstractQSimModule() {
						@Override
						protected void configureQSim() {
							bind(ChargingBeforeDayStarts.class).asEagerSingleton();
							bind(VehicleChargingHandler.class).asEagerSingleton();
							addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
									IndividualSocTimeProfileCollectorProvider1.class);
							addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
									ChargerOccupancyTimeProfileCollectorProvider1.class);
							addQSimComponentBinding(EvModule.EV_COMPONENT).to(EV_stats.class);
							bind(ChargingPowerCollector.class).asEagerSingleton();
//							bind(TripHandler.class).asEagerSingleton();
						}
					});
				}
			});
			controler.configureQSimComponents(components -> components.addNamedComponent(EvModule.EV_COMPONENT));

			controler.run();
		}
		
	}
}

