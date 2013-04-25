package be.gim.tov.osyris.model.traject;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
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
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier regio;

	@Label("Traject toewijzing")
	@Description("Traject toewijzing")
	private TrajectToewijzing trajectToewijzing;

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

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public TrajectToewijzing getTrajectToewijzing() {
		return trajectToewijzing;
	}

	public void setTrajectToewijzing(TrajectToewijzing trajectToewijzing) {
		this.trajectToewijzing = trajectToewijzing;
	}
}