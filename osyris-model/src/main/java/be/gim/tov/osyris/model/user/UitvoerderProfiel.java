package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.core.model.AbstractModelObject;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class UitvoerderProfiel extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private UitvoerderBedrijf bedrijf;

	// GETTERS AND SETTERS
	public UitvoerderBedrijf getBedrijf() {
		return bedrijf;
	}

	public void setBedrijf(UitvoerderBedrijf bedrijf) {
		this.bedrijf = bedrijf;
	}
}