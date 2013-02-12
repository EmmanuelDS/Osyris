package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
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
public abstract class NetwerkKnooppunt extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private int nummer;
	private String naam;
	private double x;
	private double y;
	@NotSearchable
	@SrsName("EPSG:31370")
	private Geometry geom;

	// GETTERS AND SETTERS
	public int getNummer() {
		return nummer;
	}

	public void setNummer(int nummer) {
		this.nummer = nummer;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
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