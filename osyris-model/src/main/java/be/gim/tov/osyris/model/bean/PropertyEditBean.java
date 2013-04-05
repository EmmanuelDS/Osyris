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

import be.gim.tov.osyris.model.annotation.EditInStatus;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyEditBean {

	private static final Log log = LogFactory.getLog(PropertyEditBean.class);

	@Inject
	private ModelRepository modelRepository;

	@Inject
	private Identity identity;

	public boolean isEditableProperty(ModelObject object, ModelProperty property) {

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
			fields = object.getClass().getSuperclass().getDeclaredFields();
		}

		for (Field field : fields) {

			// Get EditinStatus annotation if available for current property
			if (field.getName().equals(property.getName())) {
				EditInStatus editInStatusAnnotation = field
						.getAnnotation(EditInStatus.class);

				if (editInStatusAnnotation != null) {
					String[] values = editInStatusAnnotation.value();
					List<String> list = Arrays.asList(values);
					if (list.contains(status.toString())) {
						property.setEditable(true);
						return true;
					}
				}
			}
		}
		return false;
	}
}
