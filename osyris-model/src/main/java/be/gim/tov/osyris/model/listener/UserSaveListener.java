package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;

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

		PeterMeterProfiel profiel = (PeterMeterProfiel) user
				.getAspect("PeterMeterProfiel");

		if (profiel != null) {

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
