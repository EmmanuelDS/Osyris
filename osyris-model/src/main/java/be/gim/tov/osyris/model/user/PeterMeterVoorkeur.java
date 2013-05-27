package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Search;
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
@Label("Voorkeur")
@Description("Voorkeur")
@Permissions({
		@Permission(profile = "group:PeterMeter", action = "search", allow = true),
		@Permission(profile = "group:PeterMeter", action = "view", allow = true) })
public class PeterMeterVoorkeur extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	// Changed back to ResourceIdentifier
	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	private ResourceIdentifier regio;

	@Label("Trajecttype")
	@Description("Trajecttype")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.trajectTypes}")
	private String trajectType;

	@Label("Trajectnaam")
	@Description("Trajectnaam")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('TrajectNaamCode')}")
	private String trajectNaam;

	@Label("Periode")
	@Description("Periode")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PeriodeCode')}")
	private String periode;

	@NotSearchable
	@Label("Maximale afstand")
	@Description("Maximale afstand")
	private float maxAfstand;

	// GETTERS AND SETTERS
	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public String getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(String trajectType) {
		this.trajectType = trajectType;
	}

	public String getTrajectNaam() {
		return trajectNaam;
	}

	public void setTrajectNaam(String trajectNaam) {
		this.trajectNaam = trajectNaam;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public float getMaxAfstand() {
		return maxAfstand;
	}

	public void setMaxAfstand(float maxAfstand) {
		this.maxAfstand = maxAfstand;
	}
}