package be.gim.tov.osyris.model.bean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;
import org.jboss.seam.security.Identity;
import org.picketlink.idm.api.Group;

import be.gim.tov.osyris.model.annotation.EditableInGroup;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyPeterMeterProfielEditableBean {

	@Inject
	private Identity identity;

	public boolean isEditableInGroup(ModelObject object, ModelProperty property) {
		Field[] fields = object.getClass().getDeclaredFields();
		Set<Group> groups = identity.getGroups();

		for (Field field : fields) {
			if (field.getName().equals(property.getName())) {
				EditableInGroup editInGroupAnnotation = field
						.getAnnotation(EditableInGroup.class);

				if (editInGroupAnnotation == null) {
					return property.isEditable();
				}

				if (editInGroupAnnotation != null) {
					String[] values = editInGroupAnnotation.value();
					List<String> list = Arrays.asList(values);

					for (Group group : groups) {
						if (list.contains(group.getName())) {
							property.setEditable(true);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
