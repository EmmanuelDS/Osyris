package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.ModelAspect;
import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
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
@For("User")
@Permissions({
		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),
		@Permission(profile = "group:Routedokter", action = "delete", allow = true),

		@Permission(profile = "group:Uitvoerder", action = "view", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "edit", allow = true) })
public class UitvoerderProfiel extends AbstractModelObject implements
		StorableObject, ModelAspect {

	// VARIABLES
	@Label("Uitvoerder")
	@Description("Uitvoerder")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier _for;

	@Label("Bedrijf")
	@Description("Bedrijf")
	@ModelClassName("UitvoerderBedrijf")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
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