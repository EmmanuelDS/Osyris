package be.gim.tov.osyris.model.traject;

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
public abstract class NetwerkSegment extends Traject {

	// VARIABLES
	@Required
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String enkeleRichting;

	@NotEditable
	@NotViewable
	private Integer vanKpNr;

	@NotEditable
	@NotViewable
	private Integer naarKpNr;

	@ModelClassName("NetwerkKnooppunt")
	// @NotEditable
	@Edit(type = "suggestions")
	@NotSearchable
	@Target("_blank")
	private ResourceIdentifier vanKnooppunt;

	@ModelClassName("NetwerkKnooppunt")
	// @NotEditable
	@Edit(type = "suggestions")
	@NotSearchable
	@Target("_blank")
	private ResourceIdentifier naarKnooppunt;

	// GETTERS AND SETTERS
	@Override
	// @LabelProperty
	@NotEditable
	public Long getId() {
		return (Long) super.getId();
	}

	public String getEnkeleRichting() {
		return enkeleRichting;
	}

	public void setEnkeleRichting(String enkeleRichting) {
		this.enkeleRichting = enkeleRichting;
	}

	public Integer getVanKpNr() {
		return vanKpNr;
	}

	public void setVanKpNr(Integer vanKpNr) {
		this.vanKpNr = vanKpNr;
	}

	public Integer getNaarKpNr() {
		return naarKpNr;
	}

	public void setNaarKpNr(Integer naarKpNr) {
		this.naarKpNr = naarKpNr;
	}

	public ResourceIdentifier getVanKnooppunt() {
		return vanKnooppunt;
	}

	public void setVanKnooppunt(ResourceIdentifier vanKnooppunt) {
		this.vanKnooppunt = vanKnooppunt;
	}

	public ResourceIdentifier getNaarKnooppunt() {
		return naarKnooppunt;
	}

	public void setNaarKnooppunt(ResourceIdentifier naarKnooppunt) {
		this.naarKnooppunt = naarKnooppunt;
	}
}