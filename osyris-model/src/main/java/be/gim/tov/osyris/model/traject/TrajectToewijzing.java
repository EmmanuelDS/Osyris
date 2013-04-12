package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@Label("Toewijzing")
public class TrajectToewijzing extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Jaar")
	@Description("Jaar")
	private short jaar;

	@Label("Peter/Meter 1")
	@Description("Peter/Meter 1")
	@ModelClassName("User")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier peterMeter1;

	@Label("Peter/Meter 2")
	@Description("Peter/Meter 2")
	@ModelClassName("User")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier peterMeter2;

	@Label("Peter/Meter 3")
	@Description("Peter/Meter 3")
	@ModelClassName("User")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier peterMeter3;

	// GETTERS AND SETTERS
	public short getJaar() {
		return jaar;
	}

	public void setJaar(short jaar) {
		this.jaar = jaar;
	}

	public ResourceIdentifier getPeterMeter1() {
		return peterMeter1;
	}

	public void setPeterMeter1(ResourceIdentifier peterMeter1) {
		this.peterMeter1 = peterMeter1;
	}

	public ResourceIdentifier getPeterMeter2() {
		return peterMeter2;
	}

	public void setPeterMeter2(ResourceIdentifier peterMeter2) {
		this.peterMeter2 = peterMeter2;
	}

	public ResourceIdentifier getPeterMeter3() {
		return peterMeter3;
	}

	public void setPeterMeter3(ResourceIdentifier peterMeter3) {
		this.peterMeter3 = peterMeter3;
	}
}