package be.gim.tov.osyris.model.traject;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@SubClassPersistence(UNION)
public abstract class Traject extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Naam")
	@Description("Naam")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('TrajectNaamCode')}")
	private String naam;

	@Label("Lengte")
	@Description("Lengte")
	private float lengte;

	@NotViewable
	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	private Geometry geom;

	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	@Edit(type = "panels")
	private ResourceKey regio;

	@Label("Peter/Meter Lente")
	@Description("Peter/Meter Lente")
	@ModelClassName("User")
	@Edit(type = "panels")
	private ResourceIdentifier peterMeter1;

	@Label("Peter/Meter Zomer")
	@Description("Peter/Meter Zomer")
	@ModelClassName("User")
	@Edit(type = "panels")
	private ResourceIdentifier peterMeter2;

	@Label("Peter/Meter Herfst")
	@Description("Peter/Meter Herfst")
	@ModelClassName("User")
	@Edit(type = "panels")
	private ResourceIdentifier peterMeter3;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public float getLengte() {
		return lengte;
	}

	public void setLengte(float lengte) {
		this.lengte = lengte;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public ResourceKey getRegio() {
		return regio;
	}

	public void setRegio(ResourceKey regio) {
		this.regio = regio;
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