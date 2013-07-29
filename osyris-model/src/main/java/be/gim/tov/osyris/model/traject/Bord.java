package be.gim.tov.osyris.model.traject;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import javax.persistence.Transient;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Parents;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;

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
	@Label("Regio")
	@Description("Regio")
	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.regiosOostVlaanderen}")
	private ResourceIdentifier regio;

	@Label("Trajectnaam")
	@Description("Trajectnaam")
	// @Edit(type = "suggestions")
	// @ValuesExpression("#{osyrisModelFunctions.getCodeList('TrajectNaamCode')}")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getTrajectNamen(parents[0], parents[1])}")
	@Parents({ "className", "regio" })
	private String naam;

	@Label("Gemeente")
	@Description("Gemeente")
	// @Edit(type = "suggestions")
	@Edit(type = "menu")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.gemeentes}")
	private String gemeente;

	@NotSearchable
	@Label("Volgnummer")
	@Description("Volgnummer")
	private String volg;

	@NotSearchable
	@NotEditable
	@NotViewable
	@Label("SequentieNr")
	@Description("SequentieNr")
	private Long sequentie;

	@Label("Actief")
	@Description("Actief")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String actief;

	@Label("Straatnaam")
	@Description("Straatnaam")
	@Edit(type = "suggestions")
	private String straatnaam;

	@Label("Wegbevoegdheid")
	@Description("Wegbevoegdheid")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('WegbevoegdCode')}")
	private String wegBevoegd;

	@Label("Paalconstructie")
	@Description("Paalconstructie")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalConstructieCode')}")
	private String paalConst;

	@Label("Paaldiameter")
	@Description("Paaldiameter")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalDiameterCode')}")
	private String paalDia;

	@Label("Paalbeugel")
	@Description("Paalbeugel")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalBeugelCode')}")
	private String paalBeugel;

	@Label("Paalgrond")
	@Description("Paalgrond")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalGrondCode')}")
	private String paalGrond;

	@Label("Bordconstructie")
	@Description("Bordconstructie")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordConstructieCode')}")
	private String bordConst;

	@Edit(type = "panels")
	@NotSearchable
	@Label("Foto")
	@Description("Foto")
	@ModelClassName("File")
	private ResourceKey foto;

	@NotSearchable
	@NotEditable
	@Label("X")
	@Description("X")
	private double x;

	@NotSearchable
	@NotEditable
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
	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getGemeente() {
		return gemeente;
	}

	public void setGemeente(String gemeente) {
		this.gemeente = gemeente;
	}

	public String getVolg() {
		return volg;
	}

	public void setVolg(String volg) {
		this.volg = volg;
	}

	public Long getSequentie() {
		return sequentie;
	}

	public void setSequentie(Long sequentie) {
		this.sequentie = sequentie;
	}

	public String getActief() {
		return actief;
	}

	public void setActief(String actief) {
		this.actief = actief;
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

	public String getPaalDia() {
		return paalDia;
	}

	public void setPaalDia(String paalDia) {
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

	public ResourceKey getFoto() {
		return foto;
	}

	public void setFoto(ResourceKey foto) {
		this.foto = foto;
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

	@Transient
	@NotSearchable
	@NotEditable
	@NotViewable
	public ModelClass getClassName() {
		return this.getModelClass();
	}
}