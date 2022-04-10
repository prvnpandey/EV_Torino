package testing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.cadyts.car.CadytsCarModule;
import org.matsim.contrib.cadyts.car.CadytsContext;
import org.matsim.contrib.cadyts.general.CadytsConfigGroup;
import org.matsim.contrib.cadyts.general.CadytsScoring;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.scoring.functions.ScoringParameters;
import javax.inject.Inject;

/**
 * Script-in-java to include cadyts into a matsim run.
 * <p></p>
 * For the listing click on the class name above.
 * 
 * @author nagel
 *
 */
public class Calibration {

	public static void main(String[] args) {
		args=new String[1];
		args[0]="D:/Floating car data/Config/config1.xml";
//		args[0]="D:\\Floating car data\\cadyts example\\config.xml";
		final Config config = ConfigUtils.loadConfig( args[0] ) ;
		config.plans().setInputFile("D:\\Floating car data\\plans\\Day Plans\\Monday.xml");
		config.network().setInputFile("D:\\Floating car data\\Network\\Final network\\NEtwork XMl files\\Final_network.xml");
		config.qsim().setFlowCapFactor(0.1);
		config.qsim().setStorageCapFactor(0.15);
		final Scenario scenario = ScenarioUtils.loadScenario(config) ;
		
		// ---

		final Controler controler = new Controler( scenario ) ;
		controler.addOverridingModule(new CadytsCarModule());

		// include cadyts into the plan scoring (this will add the cadyts corrections to the scores):
		controler.setScoringFunctionFactory(new ScoringFunctionFactory() {
			@Inject CadytsContext cadytsContext;
			@Inject ScoringParametersForPerson parameters;
			@Override
			public ScoringFunction createNewScoringFunction(Person person) {
				final ScoringParameters params = parameters.getScoringParameters(person);
				
				SumScoringFunction scoringFunctionAccumulator = new SumScoringFunction();
				scoringFunctionAccumulator.addScoringFunction(new CharyparNagelLegScoring(params, controler.getScenario().getNetwork(), config.transit().getTransitModes()));
				scoringFunctionAccumulator.addScoringFunction(new CharyparNagelActivityScoring(params)) ;
				scoringFunctionAccumulator.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

				final CadytsScoring<Link> scoringFunction = new CadytsScoring<>(person.getSelectedPlan(), config, cadytsContext);
				scoringFunction.setWeightOfCadytsCorrection(30. * config.planCalcScore().getBrainExpBeta()) ;
				scoringFunctionAccumulator.addScoringFunction(scoringFunction );

				return scoringFunctionAccumulator;
			}
		}) ;
		controler.getConfig().controler().setOutputDirectory("D:\\Floating car data\\Matsim_output\\Calibration\\New_network\\cadyts_test");
		controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		controler.run() ;
	}

}