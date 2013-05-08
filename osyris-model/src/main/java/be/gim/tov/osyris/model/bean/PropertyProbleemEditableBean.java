package be.gim.tov.osyris.model.bean;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;
import org.jboss.seam.security.Identity;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyProbleemEditableBean {

	@Inject
	private Identity identity;

	public boolean isEditable(ModelObject object, ModelProperty property) {

		// Fields not editable in certain groups, unless if problem is new.
		if (!property.getName().equals("status")) {
			if (identity.inGroup("Medewerker", "CUSTOM")
					|| identity.inGroup("Routedokter", "CUSTOM")
					|| identity.inGroup("admin", "CUSTOM")) {
				if (object.get("commentaar") == null
						|| StringUtils.isEmpty((String) object
								.get("commentaar"))) {
					property.setEditable(true);
					return true;
				} else {
					property.setEditable(false);
					return false;
				}
			} else {
				return true;
			}
		}

		if (property.getName().equals("status")) {
			// Status only editable for certain groups
			if (identity.inGroup("Medewerker", "CUSTOM")
					|| identity.inGroup("Routedokter", "CUSTOM")
					|| identity.inGroup("admin", "CUSTOM")) {

				// If new problem status is not editable
				if (object.get("commentaar") == null
						|| StringUtils.isEmpty((String) object
								.get("commentaar"))) {
					property.setEditable(false);
					return false;
				} else {
					property.setEditable(true);
					return true;
				}
			} else {
				property.setEditable(false);
				return false;
			}
		}
		return property.isEditable();
	}
}
