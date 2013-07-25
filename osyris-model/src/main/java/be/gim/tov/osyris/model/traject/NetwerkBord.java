package be.gim.tov.osyris.model.traject;

import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
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
	@Label("Bordtype")
	@Description("bordtype")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordTypeCode')}")
	private String bordType;

	@Label("Bordbase")
	@Description("Bordbase")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordBaseCode')}")
	private String bordBase;

	@NotSearchable
	@Label("Afbeeldingscode KP1")
	@Description("Afbeeldingscode knooppunt 1")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String kp1ImageCode;

	@NotSearchable
	@Label("Afbeeldingscode KP2")
	@Description("Afbeeldingscode knooppunt 2")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String kp2ImageCode;

	@NotSearchable
	@Label("Afbeeldingscode KP3")
	@Description("Afbeeldingscode knooppunt 3")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.imageCodes}")
	private String kp3ImageCode;

	@Label("KnooppuntNr 0")
	@Description("Knooppuntnummer 0")
	private Integer kpnr0;

	@Label("KnooppuntNr 1")
	@Description("Knooppuntnummer 1")
	private Integer kpnr1;

	@Label("KnooppuntNr 2")
	@Description("Knooppuntnummer 2")
	private Integer kpnr2;

	@Label("KnooppuntNr 3")
	@Description("Knooppuntnummer 3")
	private Integer kpnr3;

	@NotSearchable
	@Label("Knooppunt 0")
	@Description("Netwerkknooppunt 0")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpid0;

	@NotSearchable
	@Label("Knooppunt 1")
	@Description("Netwerkknooppunt 1")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpid1;

	@NotSearchable
	@Label("Knooppunt 2")
	@Description("Netwerkknooppunt 2")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpid2;

	@NotSearchable
	@Label("Knooppunt 3")
	@Description("Netwerkknooppunt 3")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpid3;

	@NotSearchable
	@Label("Segmenten")
	@Description("Netwerksegmenten")
	@ModelClassName("Traject")
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

	public List<ResourceIdentifier> getSegmenten() {
		return segmenten;
	}

	public void setSegmenten(List<ResourceIdentifier> segmenten) {
		this.segmenten = segmenten;
	}
}