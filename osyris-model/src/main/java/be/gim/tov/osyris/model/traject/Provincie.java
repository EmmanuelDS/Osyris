package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.StorableObject;
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
public class Provincie extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String naam;

	@NotSearchable
	@NotViewable
	@NotEditable
	@SrsName("EPSG:31370")
	private Geometry geom;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
}
