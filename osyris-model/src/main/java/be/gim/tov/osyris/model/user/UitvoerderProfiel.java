package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.ModelAspect;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@For("User")
public class UitvoerderProfiel extends AbstractModelObject implements
		StorableObject, ModelAspect {

	// VARIABLES
	@Label("Uitvoerder")
	@Description("Uitvoerder")
	private ResourceIdentifier _for;

	@Label("Bedrijf")
	@Description("Bedrijf")
	@ModelClassName("UitvoerderBedrijf")
	private ResourceIdentifier bedrijf;

	// GETTERS AND SETTERS
	@Override
	public ResourceIdentifier getFor() {
		return _for;
	}

	@Override
	public void setFor(ResourceIdentifier _for) {
		this._for = _for;
	}

	public ResourceIdentifier getBedrijf() {
		return bedrijf;
	}

	public void setBedrijf(ResourceIdentifier bedrijf) {
		this.bedrijf = bedrijf;
	}
}