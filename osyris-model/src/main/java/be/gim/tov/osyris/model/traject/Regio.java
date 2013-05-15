package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceKey;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@Label("Regio")
public class Regio extends AbstractModelObject implements StorableObject {

	// VARIABLES
	@Label("Naam")
	@Description("Naam")
	@LabelProperty
	private String naam;

	// @Search(type = "text:between")
	@Label("Oppervlakte")
	@Description("Oppervlakte")
	private float oppervlakte;

	// @Search(type = "text:between")
	@Label("Omtrek")
	@Description("Omtrek")
	private float omtrek;

	@NotSearchable
	@NotViewable
	@NotEditable
	@SrsName("EPSG:31370")
	private Geometry geom;

	@Label("Uitvoerder")
	@Description("Uitvoerder")
	@ModelClassName("UitvoerderBedrijf")
	private ResourceKey uitvoerder;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public float getOppervlakte() {
		return oppervlakte;
	}

	public void setOppervlakte(float oppervlakte) {
		this.oppervlakte = oppervlakte;
	}

	public float getOmtrek() {
		return omtrek;
	}

	public void setOmtrek(float omtrek) {
		this.omtrek = omtrek;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}

	public ResourceKey getUitvoerder() {
		return uitvoerder;
	}

	public void setUitvoerder(ResourceKey uitvoerder) {
		this.uitvoerder = uitvoerder;
	}
}