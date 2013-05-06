package be.gim.tov.osyris.model.controle.handler;

import java.io.IOException;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.user.User;
import org.conscientia.core.permission.DefaultPermissionHandler;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.controle.Melding;

/**
 * 
 * @author kristof
 * 
 */
@Handler(type = "permission")
@For("Melding")
public class MeldingPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// Routedokter has permissions on all records
		if (identity.inGroup("Routedokter", "CUSTOM")) {
			return true;
		}

		// Only give permission to view/edit melding for medewerker
		// if melding is assigned to this medewerker
		// Load Melding
		if (identifier != null) {
			Melding melding = (Melding) modelRepository.loadObject(identifier);

			// Get status
			if (melding != null) {

				User toegewezenMedewerker = (User) modelRepository
						.loadObject(melding.getMedewerker());

				// MEDEWERKER
				if (identity.inGroup("Medewerker", "CUSTOM")
						&& toegewezenMedewerker != null) {

					if (!toegewezenMedewerker.getUsername().equals(
							identity.getUser().getId())) {

						// Override permissions on actions
						if (action.equals(Permission.EDIT_ACTION)
								|| action.equals(Permission.VIEW_ACTION)) {
							return false;
						}
					}
				}
			}
		}
		return super.hasPermission(action, identifier, modelClass, isOwner);
	}
}
