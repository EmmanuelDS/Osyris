package be.gim.tov.osyris.model.bean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;
import org.jboss.seam.security.Identity;
import org.picketlink.idm.api.Group;

import be.gim.tov.osyris.model.annotation.EditableInGroup;
import be.gim.tov.osyris.model.annotation.EditableInStatus;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyControleOpdrachtEditableBean {

	@Inject
	private Identity identity;

	public boolean isEditableInStatus(ModelObject object, ModelProperty property) {
		Object status = null;
		Field[] fields = null;

		// Routedokter and admin can edit all properties
		if (identity.inGroup("Routedokter", "CUSTOM")
				|| identity.inGroup("admin", "CUSTOM")) {
			return true;
		}

		// Get status of the object
		if (object.get("status") instanceof ControleOpdrachtStatus) {
			status = object.get("status");
		}

		fields = object.getClass().getSuperclass().getDeclaredFields();
		for (Field field : fields) {

			// Get EditinStatus annotation if available for current property
			if (field.getName().equals(property.getName())) {
				EditableInStatus editInStatusAnnotation = field
						.getAnnotation(EditableInStatus.class);

				if (editInStatusAnnotation == null) {
					return property.isEditable();
				}

				if (editInStatusAnnotation != null) {
					String[] values = editInStatusAnnotation.value();
					List<String> list = Arrays.asList(values);
					if (object.get("status") != null) {
						status = object.get("status");
						if (list.contains(status.toString())) {
							property.setEditable(true);
							return true;
						}
					}

					if (object.get("status") == null) {
						if (list.contains(StringUtils.EMPTY)) {
							property.setEditable(true);
							return true;
						}

					}

				}
			}
		}
		return false;
	}

	public boolean isEditableInGroup(ModelObject object, ModelProperty property) {
		Field[] fields = object.getClass().getSuperclass().getDeclaredFields();
		Set<Group> groups = identity.getGroups();

		// Routedokter and admin can edit all properties
		if (identity.inGroup("Routedokter", "CUSTOM")
				|| identity.inGroup("admin", "CUSTOM")) {
			return true;
		}

		// Status not editable field when ControleOpdracht is created
		if (property.getName().equals("status") && object.get("status") == null) {
			property.setEditable(false);
			return false;
		}

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
