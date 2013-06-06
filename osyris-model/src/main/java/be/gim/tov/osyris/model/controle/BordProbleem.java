package be.gim.tov.osyris.model.controle;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotViewable;
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
public abstract class BordProbleem extends Probleem {

	// VARIABLES
	@Label("Type")
	@Description("Type")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('BordProbleemCode')}")
	private String type;

	@NotViewable
	@NotEditable
	@Label("Bord")
	@Description("Bord")
	@ModelClassName("Bord")
	private ResourceIdentifier bord;

	// GETTERS AND SETTERS
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ResourceIdentifier getBord() {
		return bord;
	}

	public void setBord(ResourceIdentifier bord) {
		this.bord = bord;
	}
}