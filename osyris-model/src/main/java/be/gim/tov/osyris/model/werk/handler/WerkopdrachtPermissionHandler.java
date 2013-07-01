package be.gim.tov.osyris.model.werk.handler;

import java.io.IOException;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.core.permission.DefaultPermissionHandler;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.werk.WerkOpdracht;

/**
 * 
 * @author kristof
 * 
 */
@Handler(type = "permission")
@For("Werkopdracht")
public class WerkopdrachtPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// Routedokter has permissions on all records
		if (identity.inGroup("Routedokter", "CUSTOM")) {
			return true;
		}

		if (identifier != null) {
			WerkOpdracht werkOpdracht = (WerkOpdracht) modelRepository
					.loadObject(identifier);
			if (werkOpdracht != null) {
				String medewerkerName = modelRepository
						.getObjectName(werkOpdracht.getMedewerker());

				if (identity.inGroup("Medewerker", "CUSTOM")
						&& medewerkerName != null
						&& action.equals(Permission.VIEW_ACTION)) {
					return true;
				}

				if (!medewerkerName.equals(identity.getUser().getId())) {
					if (action.equals(Permission.EDIT_ACTION)) {
						return false;
					}
				}
			}
		}

		return super.hasPermission(action, identifier, modelClass, isOwner);
	}
}
