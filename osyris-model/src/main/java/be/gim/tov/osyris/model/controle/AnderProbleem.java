package be.gim.tov.osyris.model.controle;

import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.SrsName;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class AnderProbleem extends Probleem {

	// VARIABLES
	@NotSearchable
	@SrsName("EPSG:31370")
	private Geometry geom;

	// GETTERS AND SETTERS
	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
}