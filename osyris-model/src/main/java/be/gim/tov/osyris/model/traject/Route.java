package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class Route extends Traject {

	// VARIABLES
	@Label("Gemeente")
	@Description("Gemeente")
	@ModelClassName("Gemeente")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier gemeente;

	// GETTERS AND SETTERS
	public ResourceIdentifier getGemeente() {
		return gemeente;
	}

	public void setGemeente(ResourceIdentifier gemeente) {
		this.gemeente = gemeente;
	}
}