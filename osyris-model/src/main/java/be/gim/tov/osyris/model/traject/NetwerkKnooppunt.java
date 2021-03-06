package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.GeometryType;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Maximum;
import org.conscientia.api.model.annotation.Minimum;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.Target;
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
public abstract class NetwerkKnooppunt extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@LabelProperty
	@Required
	@Minimum(1)
	@Maximum(999)
	private Integer nummer;

	@Edit(type = "suggestions")
	@Search(type = "suggestions:like-wildcard-nocase")
	private String naam;

	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getRegiosOostVlaanderen()}")
	@Target("_blank")
	private ResourceIdentifier regio;

	@NotEditable
	@NotSearchable
	private double x;

	@NotEditable
	@NotSearchable
	private double y;

	@NotViewable
	@NotSearchable
	@NotEditable
	@GeometryType("POINT")
	@SrsName("EPSG:31370")
	@Index
	private Geometry geom;

	// GETTERS AND SETTERS
	@Override
	@NotEditable
	public Long getId() {
		return (Long) super.getId();
	}

	public Integer getNummer() {
		return nummer;
	}

	public void setNummer(Integer nummer) {
		this.nummer = nummer;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
}