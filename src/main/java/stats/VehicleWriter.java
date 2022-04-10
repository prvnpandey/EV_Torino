package stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;

import com.google.inject.Inject;

public class VehicleWriter extends MatsimXmlWriter {
	
	private String filename;
	@Inject
	private ElectricFleet electricFleet;
	

	public VehicleWriter(ElectricFleet electricFleet, String filename) {
		this.filename = filename;
		this.electricFleet = electricFleet;
	}
	public void WriteVehicle() {
		DecimalFormat format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setMinimumIntegerDigits(1);
		format.setMaximumFractionDigits(2);//XXX this is asymmetric to reading vehicles
		format.setGroupingUsed(false);
		
		openFile(filename);
		writeDoctype("vehicles", "http://matsim.org/files/dtd/electric_vehicles_v1.dtd");
		writeStartTag("vehicles", Collections.emptyList());	
		for(Entry<Id<ElectricVehicle>, ElectricVehicle> v : electricFleet.getElectricVehicles().entrySet() ) {
			List<Tuple<String, String>> atts = Arrays.asList(Tuple.of("id", v.getKey().toString()),
					Tuple.of("battery_capacity", format.format(EvUnits.J_to_kWh(v.getValue().getBattery().getCapacity())) + ""),
					//TODO consider renaming to initial_charge -- SOC suggest [%] not [kWh]
					Tuple.of("initial_soc", format.format(EvUnits.J_to_kWh(v.getValue().getBattery().getSoc())) + ""),
					Tuple.of("vehicle_type", v.getValue().getVehicleType()),
					Tuple.of("charger_types", String.join(",", v.getValue().getChargerTypes())));
			writeStartTag("vehicle", atts, true);
		}
		writeEndTag("vehicles");
		close();	
	}	
}
