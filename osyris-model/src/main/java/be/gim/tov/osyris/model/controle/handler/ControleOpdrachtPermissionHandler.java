package be.gim.tov.osyris.model.controle.handler;

import java.io.IOException;
import java.util.Set;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.core.permission.DefaultPermissionHandler;
import org.picketlink.idm.api.Group;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

@Handler(type = "permission")
@For("ControleOpdracht")
public class ControleOpdrachtPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// Load ControleOpdracht
		if (identifier != null) {
			ControleOpdracht controleOpdracht = (ControleOpdracht) modelRepository
					.loadObject(identifier);

			// Get status
			if (controleOpdracht != null) {
				ControleOpdrachtStatus status = controleOpdracht.getStatus();

				// Get groups
				Set<Group> groups = identity.getGroups();
				for (Group group : groups) {

					// PETER EN METER
					if (group.getName().equals("PeterMeter")
							&& status
									.equals(ControleOpdrachtStatus.UIT_TE_VOEREN)) {

						// Override permissions on action
						if (action.equals(Permission.EDIT_ACTION)
								|| action.equals(Permission.VIEW_ACTION)) {
							return true;
						}
					}

					// MEDEWEKER
					if (group.getName().equals("Medewerker")) {

						if (action.equals(Permission.CREATE_ACTION)) {
							return true;
						}
						if (status
								.equals(ControleOpdrachtStatus.TE_CONTROLEREN)
								|| status
										.equals(ControleOpdrachtStatus.GEANNULEERD)) {

							if (action.equals(Permission.EDIT_ACTION)
									|| action.equals(Permission.VIEW_ACTION)) {
								return true;
							}
						} else {
							return false;
						}
					}
				}
			}
		}
		return super.hasPermission(action, identifier, modelClass, isOwner);
	}
}
