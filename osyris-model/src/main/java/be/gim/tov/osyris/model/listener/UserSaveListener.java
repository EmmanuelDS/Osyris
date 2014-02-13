package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.core.search.QueryBuilder;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.codes.PeterMeterNaamCode;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;
import be.gim.tov.osyris.model.user.status.PeterMeterStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "User") })
public class UserSaveListener {

	private static final Log LOG = LogFactory.getLog(UserSaveListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException {

		User user = (User) event.getModelObject();

		ResourceName resourceName = new ResourceName("user", user.getUsername());

		UserProfile userProfile = (UserProfile) user.getAspect("UserProfile");

		PeterMeterProfiel pmProfiel = (PeterMeterProfiel) user
				.getAspect("PeterMeterProfiel");

		if (pmProfiel != null) {

			if (pmProfiel.getVoorkeuren() != null
					&& !pmProfiel.getVoorkeuren().isEmpty()) {

				checkVoorkeuren(pmProfiel);

				for (PeterMeterVoorkeur voorkeur : pmProfiel.getVoorkeuren()) {

					// Indien trajectType een Route is, set maxAfstand op 0
					if (voorkeur.getTrajectType().contains("Route")) {
						voorkeur.setMaxAfstand(0);
					}

					// Indien trajectType een Netwerk is, set routeNaam op
					// null
					if (voorkeur.getTrajectType().contains("Netwerk")) {
						voorkeur.setTrajectNaam(null);
					}
				}
			}

			// Current date indien actiefSinds leeg
			if (pmProfiel.getActiefSinds() == null) {
				pmProfiel.setActiefSinds(new Date());
			}

			// Status ACTIEF indien leeg
			if (pmProfiel.getStatus() == null) {
				pmProfiel.setStatus(PeterMeterStatus.ACTIEF);
			}

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
		}
	}

	/**
	 * Checken of Voorkeuren voldoen aan de voorwaarden voor opslag.
	 * 
	 * @return
	 * @throws IOException
	 */
	private void checkVoorkeuren(PeterMeterProfiel profiel) throws IOException {

		int counterLente = 0;
		int counterZomer = 0;
		int counterHerfst = 0;
		String message = "";

		if (profiel.getVoorkeuren() != null
				&& !profiel.getVoorkeuren().isEmpty()) {

			for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {

				if (voorkeur.getRegio() != null
						&& voorkeur.getPeriode() != null
						&& voorkeur.getTrajectType() != null) {

					if (voorkeur.getTrajectType().toLowerCase()
							.contains("netwerk")
							&& voorkeur.getMaxAfstand() == 0) {

						message = "Gelieve voor elke voorkeur van het type netwerk een maximale afstand groter dan 0 op te geven.";
						Beans.getReference(Messages.class).error(message);
						throw new IOException(message);
					}

					if (voorkeur.getPeriode().equals("1")) {
						counterLente++;
					}
					if (voorkeur.getPeriode().equals("2")) {
						counterZomer++;
					}
					if (voorkeur.getPeriode().equals("3")) {
						counterHerfst++;
					}
				} else {
					message = "Gelieve voor elke voorkeur een periode, trajectype en regio op te geven.";
					Beans.getReference(Messages.class).error(message);
					throw new IOException(message);
				}

			}

			if (counterLente > 3 || counterZomer > 3 || counterHerfst > 3) {
				message = "Per periode zijn er maximaal 3 voorkeuren toegelaten.";
				Beans.getReference(Messages.class).error(message);
				throw new IOException(message);
			}
		}
	}
}
