package be.gim.tov.osyris.model.utils;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class GeometryValidator extends AbstractModelObject implements
		StorableObject {

	private static final long serialVersionUID = 7098326729798494857L;

	@NotSearchable
	@NotEditable
	private Long trajectId;

	@NotSearchable
	@NotEditable
	private String naam;

	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	private Geometry geom;

	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	private Geometry validGeom;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date tijdstip;

	public Long getTrajectId() {
		return trajectId;
	}

	public void setTrajectId(Long trajectId) {
		this.trajectId = trajectId;
	}

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

	public Geometry getValidGeom() {
		return validGeom;
	}

	public void setValidGeom(Geometry validGeom) {
		this.validGeom = validGeom;
	}

	public Date getTijdstip() {
		return tijdstip;
	}

	public void setTijdstip(Date tijdstip) {
		this.tijdstip = tijdstip;
	}
}
