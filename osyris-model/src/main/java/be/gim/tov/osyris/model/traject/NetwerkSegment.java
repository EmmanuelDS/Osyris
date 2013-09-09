package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Required;
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
	@Label("Enkele richting")
	@Description("Enkele richting")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String enkeleRichting;

	@NotEditable
	@Label("Van kpNr")
	@Description("Knooppuntnummer van")
	private Integer vanKpNr;

	@NotEditable
	@Label("Naar kpNr")
	@Description("Knooppuntnummer naar")
	private Integer naarKpNr;

	@Label("Van knooppunt")
	@Description("van knooppunt")
	@ModelClassName("NetwerkKnooppunt")
	@Edit(type = "suggestions")
	@NotSearchable
	private ResourceIdentifier vanKnooppunt;

	@Label("Naar knooppunt")
	@Description("Naar knooppunt")
	@ModelClassName("NetwerkKnooppunt")
	@Edit(type = "suggestions")
	@NotSearchable
	private ResourceIdentifier naarKnooppunt;

	// GETTERS AND SETTERS
	@Override
	@LabelProperty
	@NotEditable
	@NotSearchable
	@Label("Segment id")
	@Description("Segment id")
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