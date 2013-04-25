package be.gim.tov.osyris.model.bean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;
import org.conscientia.api.repository.ModelRepository;
import org.jboss.seam.security.Identity;

import be.gim.tov.osyris.model.annotation.EditableInStatus;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyMeldingEditableBean {

	private static final Log log = LogFactory
			.getLog(PropertyMeldingEditableBean.class);

	@Inject
	private ModelRepository modelRepository;

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

		fields = object.getClass().getDeclaredFields();

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
				}
			}
		}
		return false;
	}
}
