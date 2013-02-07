package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class RouteBord extends Bord {

	// VARIABLES
	private String volg;
	@ModelClassName("Route")
	private ResourceIdentifier route;

	// GETTERS AND SETTERS
	public String getVolg() {
		return volg;
	}

	public void setVolg(String volg) {
		this.volg = volg;
	}

	public ResourceIdentifier getRoute() {
		return route;
	}

	public void setRoute(ResourceIdentifier route) {
		this.route = route;
	}
}