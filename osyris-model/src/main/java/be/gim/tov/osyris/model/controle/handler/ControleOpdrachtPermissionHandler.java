package be.gim.tov.osyris.model.controle.handler;

import java.io.IOException;
import java.util.Set;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.core.permission.DefaultPermission;
import org.conscientia.core.permission.DefaultPermissionHandler;
import org.picketlink.idm.api.Group;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.ControleOpdrachtStatus;

@Handler(type = "permission")
@For("ControleOpdracht")
public class ControleOpdrachtPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// Ophalen controleOpdracht object via identifier
		// Object accessen via modelRepository
		// Status eruit halen en via identity groep achterhalen

		// Object nog meegegeven in ListForm klasse aan canEdit en canDelete en
		// aanpassen list.xhtml

		if (identifier != null) {
			ControleOpdracht controleOpdracht = (ControleOpdracht) modelRepository
					.loadObject(identifier);

			if (controleOpdracht != null) {
				ControleOpdrachtStatus status = (ControleOpdrachtStatus) controleOpdracht
						.get("status");

				// Groepen ophalen
				Set<Group> groups = identity.getGroups();

				for (Group group : groups) {

					if (group.getName().equals("PeterMeter")) {
						if (status.equals(ControleOpdrachtStatus.UIT_TE_VOEREN)) {

							Permission permission = new DefaultPermission(
									new ResourceName("group:PeterMeter"),
									"edit", true);
							return permission.isAllow();
						}
					}

					if (group.getName().equals("Routedokter")) {
						Permission permission = new DefaultPermission(
								new ResourceName("group:Routedokter"),
								"create", true);
						return permission.isAllow();
					}
				}
			}
		}
		return super.hasPermission(action, identifier, modelClass, isOwner);
	}
}
