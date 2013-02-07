package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.StorableObject;
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
public class PeterMeterVoorkeur extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String trajectNaam;
	private String trajectType;
	private float maxAfstand;
	private short periode;
	@ModelClassName("Gemeente")
	private ResourceIdentifier gemeente;
	@ModelClassName("Regio")
	private ResourceIdentifier regio;

	// GETTERS AND SETTERS
	public String getTrajectNaam() {
		return trajectNaam;
	}

	public void setTrajectNaam(String trajectNaam) {
		this.trajectNaam = trajectNaam;
	}

	public String getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(String trajectType) {
		this.trajectType = trajectType;
	}

	public float getMaxAfstand() {
		return maxAfstand;
	}

	public void setMaxAfstand(float maxAfstand) {
		this.maxAfstand = maxAfstand;
	}

	public short getPeriode() {
		return periode;
	}

	public void setPeriode(short periode) {
		this.periode = periode;
	}

	public ResourceIdentifier getGemeente() {
		return gemeente;
	}

	public void setGemeente(ResourceIdentifier gemeente) {
		this.gemeente = gemeente;
	}

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}
}