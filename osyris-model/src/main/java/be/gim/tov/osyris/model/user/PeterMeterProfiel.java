package be.gim.tov.osyris.model.user;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.ModelAspect;
import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.api.model.annotation.View;
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
@Permissions({
		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),

		@Permission(profile = "group:PeterMeter", action = "search", allow = false),
		@Permission(profile = "group:PeterMeter", action = "view", allow = true),
		@Permission(profile = "group:PeterMeter", action = "edit", allow = true) })
public class PeterMeterProfiel extends AbstractModelObject implements
		StorableObject, ModelAspect {

	// VARIABLES
	@NotEditable
	@Label("Peter/Meter")
	@Description("Peter/Meter")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier _for;

	@Label("Status beschikbaarheid")
	@Description("Status beschikbaarheid")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisBean.peterMeterProfileStates}")
	private String status;

	@Label("Actief sinds")
	@Description("Actief sinds")
	private Date actiefSinds;

	@Label("Actief tot")
	@Description("Actief tot")
	private Date actiefTot;

	@Label("Voorkeuren")
	@Description("Voorkeuren")
	@Edit(type = "table")
	@View(type = "table")
	private List<PeterMeterVoorkeur> voorkeuren;

	// GETTERS AND SETTERS
	@Override
	public ResourceIdentifier getFor() {
		return _for;
	}

	@Override
	public void setFor(ResourceIdentifier _for) {
		this._for = _for;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getActiefSinds() {
		return actiefSinds;
	}

	public void setActiefSinds(Date actiefSinds) {
		this.actiefSinds = actiefSinds;
	}

	public Date getActiefTot() {
		return actiefTot;
	}

	public void setActiefTot(Date actiefTot) {
		this.actiefTot = actiefTot;
	}

	public List<PeterMeterVoorkeur> getVoorkeuren() {
		return voorkeuren;
	}

	public void setVoorkeuren(List<PeterMeterVoorkeur> voorkeuren) {
		this.voorkeuren = voorkeuren;
	}
}