package be.gim.tov.osyris.model.werk;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.core.model.AbstractModelObject;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class WerkHandeling extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('WerkHandelingCode')}")
	private String type;

	// GETTERS AND SETTERS
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}