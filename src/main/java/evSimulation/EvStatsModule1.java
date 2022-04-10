package evSimulation;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.stats.ChargerOccupancyTimeProfileCollectorProvider;
import org.matsim.contrib.ev.stats.ChargerOccupancyXYDataProvider;
import org.matsim.contrib.ev.stats.ChargerPowerCollector;
import org.matsim.contrib.ev.stats.EvMobsimListener;
import org.matsim.contrib.ev.stats.IndividualSocTimeProfileCollectorProvider;
import org.matsim.contrib.ev.stats.SocHistogramTimeProfileCollectorProvider;
import org.matsim.contrib.ev.stats.VehicleTypeAggregatedSocTimeProfileCollectorProvider;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

import com.google.inject.Inject;

/**
 * @author Michal Maciejewski (michalm)
 */
public class EvStatsModule1 extends AbstractModule {
	@Inject
	private EvConfigGroup evCfg;

	@Override
	public void install() {
		installQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				if (evCfg.getTimeProfiles()) {
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							SocHistogramTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							IndividualSocTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							ChargerOccupancyTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(ChargerOccupancyXYDataProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(
							VehicleTypeAggregatedSocTimeProfileCollectorProvider.class);
					addQSimComponentBinding(EvModule.EV_COMPONENT).to(EvMobsimListener.class);
					bind(ChargerPowerCollector.class).asEagerSingleton();
					// add more time profiles if necessary
				}
			}
		});
	}
}

