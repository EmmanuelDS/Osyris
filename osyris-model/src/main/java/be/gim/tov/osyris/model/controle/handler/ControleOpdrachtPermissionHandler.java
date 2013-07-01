package be.gim.tov.osyris.model.controle.handler;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.core.permission.DefaultPermissionHandler;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Handler(type = "permission")
@For("ControleOpdracht")
public class ControleOpdrachtPermissionHandler extends DefaultPermissionHandler {

	@Inject
	protected CacheProducer cacheProducer;

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// Routedokter has permissions on all records
		if (identity.inGroup("Routedokter", "CUSTOM")) {
			return true;
		}

		// Load ControleOpdracht
		if (identifier != null) {
			ControleOpdracht controleOpdracht = (ControleOpdracht) modelRepository
					.loadObject(identifier);

			// Get status
			if (controleOpdracht != null) {
				ControleOpdrachtStatus status = controleOpdracht.getStatus();

				if (!identity.inGroup("PeterMeter", "CUSTOM")
						&& status.equals(ControleOpdrachtStatus.UIT_TE_VOEREN)
						&& action.equals(Permission.EDIT_ACTION)) {
					return false;
				}

				// PETER EN METER
				if (identity.inGroup("PeterMeter", "CUSTOM")
						&& status.equals(ControleOpdrachtStatus.UIT_TE_VOEREN)) {

					// Override permissions on action
					if (action.equals(Permission.EDIT_ACTION)
							|| action.equals(Permission.VIEW_ACTION)) {
						return true;
					}
				}

				// MEDEWEKER
				if (identity.inGroup("Medewerker", "CUSTOM")) {

					if (action.equals(Permission.CREATE_ACTION)) {
						return true;
					}
					if (status.equals(ControleOpdrachtStatus.TE_CONTROLEREN)
							|| status
									.equals(ControleOpdrachtStatus.GEANNULEERD)
							|| status
									.equals(ControleOpdrachtStatus.GERAPPORTEERD)) {

						if (action.equals(Permission.EDIT_ACTION)
								|| action.equals(Permission.VIEW_ACTION)) {
							return true;
						}
					} else {
						if (!action.equals(Permission.VIEW_ACTION)) {
							return false;
						}
					}
				}
			}
		}

		return super.hasPermission(action, identifier, modelClass, isOwner);
	}
}
