package evSimulation;
import org.matsim.contrib.ev.MobsimScopeEventHandling;
import org.matsim.contrib.ev.charging.ChargingModule;
import org.matsim.contrib.ev.discharging.DischargingModule;
import org.matsim.contrib.ev.fleet.ElectricFleetModule;
import org.matsim.contrib.ev.infrastructure.ChargingInfrastructureModule;
import org.matsim.contrib.ev.stats.EvStatsModule;
import org.matsim.core.controler.AbstractModule;

public class EvModule1 extends AbstractModule {
	public static final String EV_COMPONENT = "EV_COMPONENT";

	@Override
	public void install() {
		bind(MobsimScopeEventHandling.class).asEagerSingleton();
		addControlerListenerBinding().to(MobsimScopeEventHandling.class);

		install(new ElectricFleetModule());
		install(new ChargingInfrastructureModule());
		install(new ChargingModule());
		install(new DischargingModule());
		install(new EvStatsModule());
	}
}
