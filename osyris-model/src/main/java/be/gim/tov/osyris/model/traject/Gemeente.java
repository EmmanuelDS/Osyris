package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.core.model.AbstractModelObject;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class Gemeente extends AbstractModelObject {

	// VARIABLES
	@LabelProperty
	private String naam;

	private String niscode;

	private String eucode;

	private float perimeter;

	@NotViewable
	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	@Index
	private Geometry geom;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getNiscode() {
		return niscode;
	}

	public void setNiscode(String niscode) {
		this.niscode = niscode;
	}

	public String getEucode() {
		return eucode;
	}

	public void setEucode(String eucode) {
		this.eucode = eucode;
	}

	public float getPerimeter() {
		return perimeter;
	}

	public void setPerimeter(float perimeter) {
		this.perimeter = perimeter;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
}