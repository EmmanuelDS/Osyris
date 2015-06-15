package be.gim.tov.osyris.model.bean;

import javax.inject.Named;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;

import be.gim.tov.osyris.model.traject.NetwerkLus;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyNotEditableForModelClassBean {

	public boolean isEditable(ModelObject object, ModelProperty property) {

		// Naam niet editeerbaar voor NetwerkLus
		if (property.getName().equals("naam") && object instanceof NetwerkLus) {
			return false;
		}

		// Lengte nooit editeerbaar
		else if (property.getName().equals("lengte")
				&& object instanceof NetwerkLus) {
			return false;
		}
		return property.isEditable();
	}
}
