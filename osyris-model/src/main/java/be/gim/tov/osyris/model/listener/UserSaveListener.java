package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
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

			// indien naamcode reeds bestaat, updaten
			if (Beans.getReference(OsyrisModelFunctions.class)
					.checkPeterMeterNaamCodeExists(resourceName)) {

				QueryBuilder builder = new QueryBuilder("PeterMeterNaamCode");
				builder.addFilter(FilterUtils.equal("code",
						resourceName.toString()));

				List<PeterMeterNaamCode> codes = (List<PeterMeterNaamCode>) modelRepository
						.searchObjects(builder.build(), false, false);

				for (PeterMeterNaamCode code : codes) {

					code.setCode(resourceName.toString());
					code.setLabel(userProfile.getLastName() + " "
							+ userProfile.getFirstName());
					modelRepository.saveObject(code);
				}
			}

			// Indien naamcode nog niet bestaat, aanmaken
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
