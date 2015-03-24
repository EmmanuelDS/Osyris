package be.gim.tov.osyris.model.traject;

import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
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
@Edit(type = "netwerkLus")
public abstract class NetwerkLus extends Traject {

	// VARIABLES
	@NotSearchable
	private boolean actief;

	@NotSearchable
	@ModelClassName("Traject")
	@Target("_blank")
	private List<ResourceIdentifier> segmenten;

	@NotEditable
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String lusGesloten;

	// GETTERS AND SETTERS
	public boolean isActief() {
		return actief;
	}

	public void setActief(boolean actief) {
		this.actief = actief;
	}

	public List<ResourceIdentifier> getSegmenten() {
		return segmenten;
	}

	public void setSegmenten(List<ResourceIdentifier> segmenten) {
		this.segmenten = segmenten;
	}

	public String getLusGesloten() {
		return lusGesloten;
	}

	public void setLusGesloten(String lusGesloten) {
		this.lusGesloten = lusGesloten;
	}
}