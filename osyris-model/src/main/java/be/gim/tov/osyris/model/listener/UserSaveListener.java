package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;

import be.gim.commons.bean.Beans;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.codes.PeterMeterNaamCode;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "User") })
public class UserSaveListener {

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		User user = (User) event.getModelObject();

		ResourceName resourceName = new ResourceName("user", user.getUsername());

		UserProfile userProfile = (UserProfile) user.getAspect("UserProfile");

		PeterMeterProfiel profiel = (PeterMeterProfiel) user
				.getAspect("PeterMeterProfiel");

		if (profiel != null) {

			if (!Beans.getReference(OsyrisModelFunctions.class)
					.checkPeterMeterNaamCodeExists(resourceName)) {

				// Save new PM in codetabel
				PeterMeterNaamCode code = new PeterMeterNaamCode();
				code.setCode(resourceName.toString());
				code.setLabel(userProfile.getLastName() + " "
						+ userProfile.getFirstName());
				modelRepository.saveObject(code);
			}

			if (profiel.getVoorkeuren() != null
					&& !profiel.getVoorkeuren().isEmpty()) {

				for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {

					// Indien trajectType een Route is, set maxAfstand op 0
					if (voorkeur.getTrajectType().contains("Route")) {
						voorkeur.setMaxAfstand(0);
					}

					// Indien trajectType een Netwerk is, set routeNaam op null
					if (voorkeur.getTrajectType().contains("Netwerk")) {
						voorkeur.setTrajectNaam(null);
					}
				}
			}
		}
	}
}
