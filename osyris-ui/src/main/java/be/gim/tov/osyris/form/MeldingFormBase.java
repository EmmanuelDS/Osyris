package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.core.form.AbstractListForm;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.controle.Melding;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class MeldingFormBase extends AbstractListForm<Melding> {
	private static final long serialVersionUID = -8052917916776585407L;

	private static final Log LOG = LogFactory.getLog(MeldingFormBase.class);

	// VARIABLES
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected Messages messages;

	@PostConstruct
	public void init() throws IOException {
		search();
	}

	// GETTERS AND SETTERS
	@Override
	public String getName() {
		return "Melding";
	}

	public Melding getMelding() {

		if (object == null) {
			object = createMelding();
		}
		return object;
	}

	public void setMelding(Melding melding) {
		this.object = melding;
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
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at creation model object.", e);
		}
		return melding;
	}

	public void saveMelding() {

		try {
			// Save Melding
			modelRepository.saveObject(getMelding());
			messages.info("Melding sucessvol verzonden naar TOV.");

			// Email bevestiging sturen naar gebruiker
			// sendConfirmationMail(melding);
			messages.info("Er is een bevestigingsmail gestuurd naar "
					+ object.getEmail() + ".");
			object = createMelding();
		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
			messages.error("Melding niet verzonden");
		} catch (Exception e) {
			LOG.error("Can not send mail.", e);
			messages.error("Bevestigingsmail niet verstuurd.");
			throw new RuntimeException(e);
		}
	}

	private void sendConfirmationMail(Melding melding) throws Exception {

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);

		// TODO: Extra melding properties toevoegen?
		variables.put("firstname", melding.getVoornaam());
		variables.put("lastname", melding.getNaam());
		variables.put("phone", melding.getTelefoon());
		variables.put("status", melding.getStatus());
		variables.put("problem", melding.getProbleem());

		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(melding.getEmail()),
				"/META-INF/resources/core/mails/confirmMelding.fmt", variables);
	}
}
