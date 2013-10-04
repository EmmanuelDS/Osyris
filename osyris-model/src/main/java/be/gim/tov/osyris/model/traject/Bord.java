package be.gim.tov.osyris.model.traject;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import javax.persistence.Transient;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.External;
import org.conscientia.api.model.annotation.Index;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.SrsName;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Target;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.annotation.Width;
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
	@NotEditable
	@NotSearchable
	private Long id;

	@ModelClassName("Regio")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.regiosOostVlaanderen}")
	private ResourceIdentifier regio;

	// @Type(value = ModelPropertyType.ENUM)
	// @ValuesExpression("#{osyrisModelFunctions.getTrajectNamen(parents[0], parents[1])}")
	// @Parents({ "className", "regio" })
	@Edit(type = "suggestions")
	@Search(type = "suggestions:like-wildcard-nocase")
	private String naam;

	// @Edit(type = "suggestions")
	@Edit(type = "menu")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.gemeentes}")
	private String gemeente;

	private String volg;

	@NotSearchable
	@NotEditable
	@NotViewable
	private Long sequentie;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String actief;

	@Edit(type = "suggestions")
	@Search(type = "suggestions:like-wildcard-nocase")
	private String straatnaam;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('WegbevoegdCode')}")
	private String wegBevoegd;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalConstructieCode')}")
	private String paalConst;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalDiameterCode')}")
	private String paalDia;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalBeugelCode')}")
	private String paalBeugel;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PaalGrondCode')}")
	private String paalGrond;

	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordConstructieCode')}")
	private String bordConst;

	@External
	@View(type = "image")
	@Width(150)
	@NotSearchable
	@ModelClassName("File")
	@Target("_blank")
	private ResourceIdentifier foto;

	@NotSearchable
	@NotEditable
	private double x;

	@NotSearchable
	@NotEditable
	private double y;

	@NotViewable
	@NotSearchable
	@NotEditable
	@SrsName("EPSG:31370")
	@Index
	private Geometry geom;

	// GETTERS AND SETTERS
	@Override
	public Long getId() {
		return (Long) super.getId();
	}

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

	public ResourceIdentifier getFoto() {
		return foto;
	}

	public void setFoto(ResourceIdentifier foto) {
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