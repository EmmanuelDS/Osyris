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
public abstract class Route extends Traject {

	// VARIABLES
	@ModelClassName("Gemeente")
	private ResourceIdentifier gemeente;

	// GETTERS AND SETTERS
	public ResourceIdentifier getGemeente() {
		return gemeente;
	}

	public void setGemeente(ResourceIdentifier gemeente) {
		this.gemeente = gemeente;
	}
}