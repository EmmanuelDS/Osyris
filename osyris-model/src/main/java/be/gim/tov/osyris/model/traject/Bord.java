package be.gim.tov.osyris.model.traject;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.SubClassPersistence;
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
public abstract class Bord extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Naam")
	@Description("Naam")
	private String naam;

	@Label("Straatnaam")
	@Description("Straatnaam")
	private String straatnaam;

	@Label("Wegbevoegd")
	@Description("Wegbevoegd")
	private String wegBevoegd;

	@Label("Paalconstructie")
	@Description("Paalconstructie")
	private String paalConst;

	@Label("Paaldiameter")
	@Description("Paaldiameter")
	private int paalDia;

	@Label("Paalbeugel")
	@Description("Paalbeugel")
	private String paalBeugel;

	@Label("Paalgrond")
	@Description("Paalgrond")
	private String paalGrond;

	@Label("Bordconstructie")
	@Description("Bordconstructie")
	private String bordConst;

	@Label("Foto")
	@Description("Foto")
	private String foto;

	@Label("Fiche")
	@Description("Fiche")
	private String fiche;

	@Label("X")
	@Description("X")
	private double x;

	@Label("Y")
	@Description("Y")
	private double y;

	@NotViewable
	@NotSearchable
	@SrsName("EPSG:31370")
	private Geometry geom;

	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	private ResourceIdentifier regio;

	@Label("Gemeente")
	@Description("Gemeente")
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