package be.gim.tov.osyris.model.werk.handler;

import java.io.IOException;

import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.core.permission.DefaultPermissionHandler;

import be.gim.commons.resource.ResourceIdentifier;

@Handler(type = "permission")
@For("Werkopdracht")
public class WerkopdrachtPermissionHandler extends DefaultPermissionHandler {

	@Override
	public Boolean hasPermission(String action, ResourceIdentifier identifier,
			ModelClass modelClass, boolean isOwner) throws IOException {

		// TODO: Uitvoerders mogen enkel hun eigen werkopdrachten zien
		return super.hasPermission(action, identifier, modelClass, isOwner);

	}
}
