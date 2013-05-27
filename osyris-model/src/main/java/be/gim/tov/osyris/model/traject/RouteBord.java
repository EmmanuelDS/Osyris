package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;

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
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String imageCode;

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

	public String getImageCode() {
		return imageCode;
	}

	public void setImageCode(String imageCode) {
		this.imageCode = imageCode;
	}

	public ResourceIdentifier getRoute() {
		return route;
	}

	public void setRoute(ResourceIdentifier route) {
		this.route = route;
	}
}