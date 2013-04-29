package be.gim.tov.osyris.model.werk.handler;

import java.io.IOException;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.permission.Permission;
import org.conscientia.core.permission.DefaultPermissionHandler;
import org.conscientia.core.user.UsernameLiteral;

import be.gim.commons.bean.Beans;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.tov.osyris.model.user.UitvoerderProfiel;
import be.gim.tov.osyris.model.werk.StockMateriaal;

@Handler(type = "permission")
@For("StockMateriaal")
public class StockMateriaalPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		String username = Beans.getReference(String.class,
				UsernameLiteral.INSTANCE);

		// Load uitvoerderPofiel
		UitvoerderProfiel profiel = (UitvoerderProfiel) modelRepository
				.loadAspect(modelRepository.getModelClass("UitvoerderProfiel"),
						new ResourceKey("User", "6"));

		// Load StockMateriaal
		if (identifier != null) {
			StockMateriaal stockMateriaal = (StockMateriaal) modelRepository
					.loadObject(identifier);

			if (stockMateriaal != null) {

				if (identity.inGroup("Uitvoerder", "CUSTOM")
						&& !stockMateriaal
								.getMagazijn()
								.toLowerCase()
								.equals(profiel.getBedrijf().getNaam()
										.toLowerCase())) {

					// Override permissions for action
					if (action.equals(Permission.VIEW_ACTION)) {
						return false;
					}
				}

			}
		}
		return super.hasPermission(action, identifier, modelClass, isOwner);
	}
}
