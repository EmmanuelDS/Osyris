package be.gim.tov.osyris.model.traject;

import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Target;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class NetwerkBord extends Bord {

	// VARIABLES
	@Required
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordTypeCode')}")
	private String bordType;

	@NotEditable
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordBaseCode')}")
	private String bordBase;

	@NotSearchable
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String kp1ImageCode;

	@NotSearchable
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String kp2ImageCode;

	@NotSearchable
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String kp3ImageCode;

	@NotEditable
	@NotViewable
	private Integer kpnr0;

	@NotEditable
	@NotViewable
	private Integer kpnr1;

	@NotEditable
	@NotViewable
	private Integer kpnr2;

	@NotEditable
	@NotViewable
	private Integer kpnr3;

	@NotSearchable
	@Edit(type = "suggestions")
	@ModelClassName("NetwerkKnooppunt")
	@Target("_blank")
	private ResourceIdentifier kpid0;

	@NotSearchable
	@Edit(type = "suggestions")
	@ModelClassName("NetwerkKnooppunt")
	@Target("_blank")
	private ResourceIdentifier kpid1;

	@NotSearchable
	@Edit(type = "suggestions")
	@ModelClassName("NetwerkKnooppunt")
	@Target("_blank")
	private ResourceIdentifier kpid2;

	@NotSearchable
	@Edit(type = "suggestions")
	@ModelClassName("NetwerkKnooppunt")
	@Target("_blank")
	private ResourceIdentifier kpid3;

	private RichtingEnum richting;

	@NotSearchable
	@ModelClassName("Traject")
	@Target("_blank")
	private List<ResourceIdentifier> segmenten;

	// GETTERS AND SETTERS
	public String getBordType() {
		return bordType;
	}

	public void setBordType(String bordType) {
		this.bordType = bordType;
	}

	public String getBordBase() {
		return bordBase;
	}

	public void setBordBase(String bordBase) {
		this.bordBase = bordBase;
	}

	public String getKp1ImageCode() {
		return kp1ImageCode;
	}

	public void setKp1ImageCode(String kp1ImageCode) {
		this.kp1ImageCode = kp1ImageCode;
	}

	public String getKp2ImageCode() {
		return kp2ImageCode;
	}

	public void setKp2ImageCode(String kp2ImageCode) {
		this.kp2ImageCode = kp2ImageCode;
	}

	public String getKp3ImageCode() {
		return kp3ImageCode;
	}

	public void setKp3ImageCode(String kp3ImageCode) {
		this.kp3ImageCode = kp3ImageCode;
	}

	public Integer getKpnr0() {
		return kpnr0;
	}

	public void setKpnr0(Integer kpnr0) {
		this.kpnr0 = kpnr0;
	}

	public Integer getKpnr1() {
		return kpnr1;
	}

	public void setKpnr1(Integer kpnr1) {
		this.kpnr1 = kpnr1;
	}

	public Integer getKpnr2() {
		return kpnr2;
	}

	public void setKpnr2(Integer kpnr2) {
		this.kpnr2 = kpnr2;
	}

	public Integer getKpnr3() {
		return kpnr3;
	}

	public void setKpnr3(Integer kpnr3) {
		this.kpnr3 = kpnr3;
	}

	public ResourceIdentifier getKpid0() {
		return kpid0;
	}

	public void setKpid0(ResourceIdentifier kpid0) {
		this.kpid0 = kpid0;
	}

	public ResourceIdentifier getKpid1() {
		return kpid1;
	}

	public void setKpid1(ResourceIdentifier kpid1) {
		this.kpid1 = kpid1;
	}

	public ResourceIdentifier getKpid2() {
		return kpid2;
	}

	public void setKpid2(ResourceIdentifier kpid2) {
		this.kpid2 = kpid2;
	}

	public ResourceIdentifier getKpid3() {
		return kpid3;
	}

	public void setKpid3(ResourceIdentifier kpid3) {
		this.kpid3 = kpid3;
	}

	public RichtingEnum getRichting() {
		return richting;
	}

	public void setRichting(RichtingEnum richting) {
		this.richting = richting;
	}

	public List<ResourceIdentifier> getSegmenten() {
		return segmenten;
	}

	public void setSegmenten(List<ResourceIdentifier> segmenten) {
		this.segmenten = segmenten;
	}
}