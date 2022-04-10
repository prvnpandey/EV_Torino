package evSimulation;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;
import org.matsim.core.scenario.ScenarioUtils;

import com.google.inject.Inject;

public class TimeVariantLinkImpl {
	

	final static double val1 = 0.15;
	final static double val2 = 0.3;
	final static double val3 = 0.4;
	final static double val4 = 1.0;
	// outer network
	final static double val_1 = 0.2;
	final static double val_2 = 0.5;
	final static double val_3 = 0.7;
	final static double val_4 = 1.0;
	
	static double center_x = 1394743.8157114468;
	static double center_y = 4991523.933084457;
	static double radius = 10000.0;
	static double x_min = center_x - radius; 
	static double x_max = center_x + radius;
	static double y_min = center_y - radius; 
	static double y_max = center_y + radius ;
	
	static Id<Node> node_id;
	static Node node;
	@Inject
	static
	Scenario scenario;
	public static void changenetwork(Id<Node> node_id, Node node) {
		
		if(((x_min <= node.getCoord().getX())&& (x_max >= node.getCoord().getX())) && 
				((y_min <= node.getCoord().getY())&& (y_max >= node.getCoord().getY()))) {
			node.getInLinks().forEach((link_id,link)-> {
		if(link.getFreespeed()>2.9 & link.getFreespeed() <= 12.5) {link.setFreespeed(link.getFreespeed()*val1);}
		if(link.getFreespeed()>12.6 & link.getFreespeed() <= 19.5) {link.setFreespeed(link.getFreespeed()*val2);}
		if((link.getFreespeed()>19.) & (link.getFreespeed() <= 27.8)) {link.setFreespeed(link.getFreespeed()*val3);}
		else {link.setFreespeed(link.getFreespeed()*val4);}	
			});
			} else {
				node.getInLinks().forEach((link_id,link)-> {
					if(link.getFreespeed()>2.9 && link.getFreespeed() <= 12.5) {link.setFreespeed(link.getFreespeed()*val_1);}
					else if(link.getFreespeed()>12.5 && link.getFreespeed() <= 19.5) {link.setFreespeed(link.getFreespeed()*val_2);}
					else if((link.getFreespeed()>19.5) && (link.getFreespeed() <= 27.8)) {link.setFreespeed(link.getFreespeed()*val_3);}
					else {link.setFreespeed(link.getFreespeed()*val_4);}			
						});
			}
	}
	
	public static NetworkChangeEvent timeVariantnetwork(Id<Node> node_id, Node node) {
		NetworkChangeEvent event = new NetworkChangeEvent(7.*3600.) ;
		if(((x_min <= node.getCoord().getX())&& (x_max >= node.getCoord().getX())) && 
				((y_min <= node.getCoord().getY())&& (y_max >= node.getCoord().getY()))) {
			node.getInLinks().forEach((link_id,link)-> {
				
				if(link.getFreespeed()>2.9 & link.getFreespeed() <= 12.5) { event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val1 ));} 
				if(link.getFreespeed()>12.6 & link.getFreespeed() <= 19.5) {event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val2 ));}
				if((link.getFreespeed()>19.) & (link.getFreespeed() <= 27.8)) {event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val3 ));}
				else {event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val4 ));}	
				event.addLink(link);
				});
			
			} else { 
					
				node.getInLinks().forEach((link_id,link)-> {
					
					if(link.getFreespeed()>2.9 & link.getFreespeed() <= 12.5) { event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val_1 ));} 
					if(link.getFreespeed()>12.6 & link.getFreespeed() <= 19.5) {event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val_2 ));}
					if((link.getFreespeed()>19.) & (link.getFreespeed() <= 27.8)) {event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val_3 ));}
					else {event.setFreespeedChange(new ChangeValue( ChangeType.ABSOLUTE_IN_SI_UNITS,  link.getFreespeed()*val_4 ));}
					
					}); 
					
			}
		
		return event;
	} 
}
		
