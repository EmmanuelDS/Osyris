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

		// Set geom property to editable false
		if (property.getName().equals("geom")
				|| property.getName().equals("bord")) {
			return false;
		}

		boolean editable = false;
		// Fields not editable in certain groups, unless if problem is new.
		if (!property.getName().equals("status")) {
			if (identity.inGroup("Medewerker", "CUSTOM")
					|| identity.inGroup("Routedokter", "CUSTOM")
					|| identity.inGroup("admin", "CUSTOM")) {
				if (object.get("commentaar") == null
						|| StringUtils.isEmpty((String) object
								.get("commentaar"))) {
					editable = true;
				}
			} else {
				editable = true;
			}
		}

		if (property.getName().equals("status")) {
			// Status only editable for certain groups
			if (identity.inGroup("Medewerker", "CUSTOM")
					|| identity.inGroup("Routedokter", "CUSTOM")
					|| identity.inGroup("admin", "CUSTOM")) {

				// If new problem status is not editable
				if (object.get("commentaar") != null
						|| !StringUtils.isEmpty((String) object
								.get("commentaar"))) {
					editable = true;
				}
			}
		}
		return editable;
	}
}
