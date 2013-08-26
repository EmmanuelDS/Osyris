package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.SrsName;
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
	@Label("Nummer")
	@Description("Nummer")
	@LabelProperty
	private Integer nummer;

	@Label("Naam")
	@Description("Naam")
	private String naam;

	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getRegiosOostVlaanderen()}")
	private ResourceIdentifier regio;

	@NotEditable
	@NotSearchable
	@Label("X")
	@Description("X")
	private double x;

	@NotEditable
	@NotSearchable
	@Label("Y")
	@Description("Y")
	private double y;

	@NotViewable
	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	@Index
	private Geometry geom;

	// GETTERS AND SETTERS
	@Override
	@NotEditable
	@NotSearchable
	@Label("Knooppunt id")
	@Description("Knooppunt id")
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