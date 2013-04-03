package be.gim.tov.osyris.model.werk.handler;

import java.io.IOException;
import java.util.Set;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.core.permission.DefaultPermissionHandler;
import org.picketlink.idm.api.Group;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.werk.StockMateriaal;

@Handler(type = "permission")
@For("StockMateriaal")
public class StockMateriaalPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// Load StockMateriaal
		if (identifier != null) {
			StockMateriaal stockMateriaal = (StockMateriaal) modelRepository
					.loadObject(identifier);

			if (stockMateriaal != null) {

				// Get groups
				Set<Group> groups = identity.getGroups();
				String user = identity.getUser().getId();

				for (Group group : groups) {
					if (group.getName().equals("Uitvoerder")
							&& !stockMateriaal.getMagazijn().toLowerCase()
									.equals(user)) {

						// Override permissions for action
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
