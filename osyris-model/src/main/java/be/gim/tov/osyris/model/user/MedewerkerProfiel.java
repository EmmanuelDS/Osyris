package be.gim.tov.osyris.model.user;

import java.util.List;

import org.conscientia.api.model.ModelAspect;
import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
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

		@Permission(profile = "group:Medewerker", action = "view", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true) })
public class MedewerkerProfiel extends AbstractModelObject implements
		StorableObject, ModelAspect {

	// VARIABLES
	@NotSearchable
	@NotViewable
	@NotEditable
	private ResourceIdentifier _for;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.trajectTypes}")
	private List<String> trajectType;

	// GETTERS AND SETTERS
	@Override
	public ResourceIdentifier getFor() {
		return _for;
	}

	@Override
	public void setFor(ResourceIdentifier _for) {
		this._for = _for;
	}

	public List<String> getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(List<String> trajectType) {
		this.trajectType = trajectType;
	}
}
