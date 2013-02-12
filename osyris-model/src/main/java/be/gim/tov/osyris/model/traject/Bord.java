package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.SrsName;
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
public abstract class Bord extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String naam;
	private String straatnaam;
	private String wegBevoegd;
	private String paalConst;
	private int paalDia;
	private String paalBeugel;
	private String paalGrond;
	private String bordConst;
	private String bordBase;
	private String foto;
	private String fiche;
	private double x;
	private double y;
	@NotSearchable
	@SrsName("EPSG:31370")
	private Geometry geom;
	@ModelClassName("Regio")
	private ResourceIdentifier regio;
	@ModelClassName("Gemeente")
	private ResourceIdentifier gemeente;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getStraatnaam() {
		return straatnaam;
	}

	public void setStraatnaam(String straatnaam) {
		this.straatnaam = straatnaam;
	}

	public String getWegBevoegd() {
		return wegBevoegd;
	}

	public void setWegBevoegd(String wegBevoegd) {
		this.wegBevoegd = wegBevoegd;
	}

	public String getPaalConst() {
		return paalConst;
	}

	public void setPaalConst(String paalConst) {
		this.paalConst = paalConst;
	}

	public int getPaalDia() {
		return paalDia;
	}

	public void setPaalDia(int paalDia) {
		this.paalDia = paalDia;
	}

	public String getPaalBeugel() {
		return paalBeugel;
	}

	public void setPaalBeugel(String paalBeugel) {
		this.paalBeugel = paalBeugel;
	}

	public String getPaalGrond() {
		return paalGrond;
	}

	public void setPaalGrond(String paalGrond) {
		this.paalGrond = paalGrond;
	}

	public String getBordConst() {
		return bordConst;
	}

	public void setBordConst(String bordConst) {
		this.bordConst = bordConst;
	}

	public String getBordBase() {
		return bordBase;
	}

	public void setBordBase(String bordBase) {
		this.bordBase = bordBase;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public String getFiche() {
		return fiche;
	}

	public void setFiche(String fiche) {
		this.fiche = fiche;
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

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public ResourceIdentifier getGemeente() {
		return gemeente;
	}

	public void setGemeente(ResourceIdentifier gemeente) {
		this.gemeente = gemeente;
	}
}