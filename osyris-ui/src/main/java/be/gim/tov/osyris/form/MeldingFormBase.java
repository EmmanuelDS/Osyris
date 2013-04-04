package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;

import be.gim.commons.localization.bundle.Messages;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.status.MeldingStatus;

/**
 * 
 * @author kristof
 * 
 */
public class MeldingFormBase implements Serializable {

	private static final Log log = LogFactory.getLog(MeldingFormBase.class);

	// VARIABLES
	@Inject
	private ModelRepository modelRepository;

	@Inject
	private Preferences preferences;

	@Inject
	private MailSender mailSender;

	@Inject
	private Messages messages;

	private Melding melding;

	// GETTERS AND SETTERS
	public Melding getMelding() {

		if (melding == null) {
			melding = createMelding();
		}
		return melding;
	}

	public void setMelding(Melding melding) {
		this.melding = melding;
	}

	// METHODS
	public Melding createMelding() {
		Melding melding = null;

		try {
			String name = "Melding";
			melding = (Melding) modelRepository.createObject(
					modelRepository.getModelClass(name),
					(ResourceName) ResourceName.fromString(name));

		} catch (InstantiationException e) {
			log.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			log.error("Illegal access at creation model object.", e);
		}
		return melding;
	}

	public void saveMelding() {

		try {
			Melding melding = getMelding();
			melding.set("status", MeldingStatus.GEMELD);
			melding.set("datumGemeld", new Date());

			// TODO: Automatisch toewijzen aan Medewerker TOV

			// Save Melding
			modelRepository.saveObject(melding);

			// Email bevestiging sturen naar gebruiker
			sendConfirmationMail(melding);
			log.info("Email sent to: " + melding.getEmail());

		} catch (IOException e) {
			log.error("Can not save model object.", e);
		} catch (Exception e) {
			log.error("Can not send mail.", e);
			throw new RuntimeException(e);
		}
	}

	private void sendConfirmationMail(Melding melding) throws Exception {

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);

		// TODO: Melding properties toevoegen
		variables.put("melding", melding);

		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(melding.getEmail()),
				"/META-INF/resources/core/mails/confirmMelding.fmt", variables);
	}

	// TODO: Uploaden foto bij een probleem
}
