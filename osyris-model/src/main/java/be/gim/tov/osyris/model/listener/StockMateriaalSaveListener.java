package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.UserProfile;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.werk.StockMateriaal;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "StockMateriaal") })
public class StockMateriaalSaveListener {

	private static final Log LOG = LogFactory
			.getLog(StockMateriaalSaveListener.class);

	// VARIABLES
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected ModelRepository modelRepository;
	@Inject
	private OsyrisModelFunctions osyrisModelFunctions;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		StockMateriaal stockMateriaal = (StockMateriaal) event.getModelObject();
		// Check of StockMateriaal onder minimum aantal zit
		if (stockMateriaal.getInStock() <= stockMateriaal.getMin()) {
			// Stuur email naar Routedokters
			sendMail(stockMateriaal);
		}
	}

	/**
	 * Stuurt een email naar de Routedokter wanneer een stock item onder de
	 * minimum limiet zit.
	 * 
	 * @param stockMateriaal
	 */
	private void sendMail(StockMateriaal stockMateriaal) {

		try {
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("preferences", preferences);
			variables.put("magazijn", stockMateriaal.getMagazijn());
			variables.put("categorie", stockMateriaal.getCategorie());
			variables.put("subCategorie", stockMateriaal.getSubCategorie());
			variables.put("type", stockMateriaal.getType());
			variables.put("naam", stockMateriaal.getNaam());
			variables.put("nummer", stockMateriaal.getNummer());
			variables.put("inStock", stockMateriaal.getInStock());
			variables.put("min", stockMateriaal.getMin());
			variables.put("max", stockMateriaal.getMax());

			// Ophalen emailadres Routedokters
			List<ResourceIdentifier> users = osyrisModelFunctions
					.getUsersInGroup("RouteDokter");

			for (ResourceIdentifier user : users) {
				UserProfile profiel = (UserProfile) modelRepository.loadAspect(
						modelRepository.getModelClass("UserProfile"),
						modelRepository.loadObject(user));

				// mailSender.sendMail(preferences.getNoreplyEmail(),
				// Collections.singleton(profiel.getEmail()),
				// "/META-INF/resources/core/mails/stockAlert.fmt",
				// variables);
			}

			// DEBUG ONLY
			String testEmail = "kristof.spiessens@gim.be";
			mailSender.sendMail(preferences.getNoreplyEmail(),
					Collections.singleton(testEmail),
					"/META-INF/resources/core/mails/stockAlert.fmt", variables);

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		} catch (Exception e) {
			LOG.error("Can not send email.", e);
		}
	}
}
