package evSimulation;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.charging.VehicleChargingHandler;
import org.matsim.contrib.ev.discharging.AuxEnergyConsumption;
import org.matsim.contrib.ev.routing.EvNetworkRoutingProvider;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;



public class Ev_main {
	final private static Logger LOG = Logger.getLogger(Ev_main.class);
	
	public static void main(String[] args) {
						LOG.info("Starting of main class of matsim program");
						int i =1;
						int j =0;
						String currentDir = "D:\\Floating car data\\charger_location\\";
						LOG.warn("this is main program directory"+ currentDir );
						LocalDate today = LocalDate.now( ZoneId.of( "Europe/Paris" ) ) ;
						String date = today.toString() ; 

						
//						String output = "D:\\thesis\\TORINO_MATSIM_SIMULATION\\workingDir\\output#.xml";
						EvConfigGroup evGroup = new EvConfigGroup();
						
						Config config = ConfigUtils.loadConfig(currentDir+"config.xml",evGroup);
						evGroup.setChargersFile(currentDir + "charger1.xml");
						evGroup.setVehiclesFile(currentDir + "evehicleswithfullbattery1.xml");
//						config.plans().setInputFile("D:\\Floating car data\\plans\\Weekly_plans\\Weekly_plans_1.xml");
						config.plans().setInputFile("D:\\Floating car data\\bicycle_slopetest\\plans.xml");
//						config.network().setInputFile("D:\\Floating car data\\Network\\IS_network.xml");
						config.network().setInputFile("D:\\\\Floating car data\\\\bicycle_slopetest\\network.xml");
						config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
						config.controler().setOutputDirectory("D:\\Floating car data\\Matsim_output\\ev_EXPERIMENTAL OUTPUTS\\EVOutput"+date);
						Scenario scenario = ScenarioUtils.loadScenario(config);
						
						Controler controler = new Controler(scenario);
						controler.addOverridingModule(new EvModule());

						controler.addOverridingModule(new AbstractModule() {
							@Override
							public void install() {
								bind(AuxEnergyConsumption.Factory.class).toInstance(electricVehicle -> (beginTime, duration, linkId) -> 0); //a dummy factory, as aux consumption is part of the drive consumption in the model
								addRoutingModuleBinding(TransportMode.car).toProvider(new EvNetworkRoutingProvider_new(TransportMode.car));
								
								installQSimModule(new AbstractQSimModule() {
									@Override
									protected void configureQSim() {
										bind(VehicleChargingHandler.class).asEagerSingleton();
//										addEventHandlerBinding().to(VehicleChargingHandler.class);
												
									}
								});
							}
						});
						controler.configureQSimComponents(components -> components.addNamedComponent(EvModule.EV_COMPONENT));
						controler.getConfig().controler().setLastIteration(j);

						controler.run();
								
			}

		}

