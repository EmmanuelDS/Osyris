package be.gim.tov.osyris.model.controle;

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
public abstract class BordProbleem extends Probleem {

	// VARIABLES
	@ModelClassName("Bord")
	private ResourceIdentifier bord;

	// GETTERS AND SETTERS
	public ResourceIdentifier getBord() {
		return bord;
	}

	public void setBord(ResourceIdentifier bord) {
		this.bord = bord;
	}
}