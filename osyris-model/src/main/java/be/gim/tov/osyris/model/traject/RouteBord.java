package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
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
	@Label("Volgnummer")
	@Description("Volgnummer")
	private String volg;

	@Label("Afbeeldingscode")
	@Description("Afbeeldingscode")
	private short imageCode;

	@Label("Route")
	@Description("Route")
	@ModelClassName("Route")
	private ResourceIdentifier route;

	// GETTERS AND SETTERS
	public String getVolg() {
		return volg;
	}

	public void setVolg(String volg) {
		this.volg = volg;
	}

	public short getImageCode() {
		return imageCode;
	}

	public void setImageCode(short imageCode) {
		this.imageCode = imageCode;
	}

	public ResourceIdentifier getRoute() {
		return route;
	}

	public void setRoute(ResourceIdentifier route) {
		this.route = route;
	}
}