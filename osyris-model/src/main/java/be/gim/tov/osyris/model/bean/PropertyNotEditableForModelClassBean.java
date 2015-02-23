package be.gim.tov.osyris.model.bean;

import java.lang.reflect.Field;

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

		// Get properties on level of Traject
		Field[] fields = object.getClass().getSuperclass().getSuperclass()
				.getDeclaredFields();

		if (property.getName().equals("naam") && object instanceof NetwerkLus) {

			return false;
		}

		else if (property.getName().equals("lengte")
				&& object instanceof NetwerkLus) {

			property.setViewable(false);
		}
		return property.isEditable();
	}
}
