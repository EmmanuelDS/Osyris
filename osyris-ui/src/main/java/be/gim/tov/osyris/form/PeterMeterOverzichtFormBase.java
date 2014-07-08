package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.document.Document;
import org.conscientia.api.group.Group;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.security.annotation.RunPrivileged;
import org.jboss.seam.security.Identity;

import be.gim.commons.collections.CollectionUtils;
import be.gim.commons.encoder.api.Encoder;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.peritia.codec.EncodableContent;
import be.gim.peritia.io.content.Content;
import be.gim.tov.osyris.model.encoder.PeterMeterCSVModelEncoder;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class PeterMeterOverzichtFormBase extends AbstractListForm<User>
		implements Serializable {
	private static final long serialVersionUID = 7761265026167905576L;

	private static final Log LOG = LogFactory
			.getLog(PeterMeterOverzichtFormBase.class);

	private static final String PERIODE_LENTE = "1";
	private static final String PERIODE_ZOMER = "2";
	private static final String PERIODE_HERFST = "3";

	// VARIABLES
	protected Date actiefSinds;
	protected Date actiefTot;

	@Inject
	protected UserRepository userRepository;
	@Inject
	protected Identity identity;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;

	protected boolean hasErrors;

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	public Date getActiefSinds() {
		return actiefSinds;
	}

	public void setActiefSinds(Date actiefSinds) {
		this.actiefSinds = actiefSinds;
	}

	public Date getActiefTot() {
		return actiefTot;
	}

	public void setActiefTot(Date actiefTot) {
		this.actiefTot = actiefTot;
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	@Override
	public String getName() {
		return "User";
	}

	public User getUser() {
		return object;
	}

	public void setUser(User user) {
		this.object = user;
	}

	@Override
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		try {
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", "PeterMeter"));

			query.addFilter(FilterUtils.in("username", CollectionUtils
					.transform(group.getMembers(), new Transformer() {

						@Override
						public Object transform(Object member) {
							return ((ResourceName) member).getNamePart();
						}
					})));
		} catch (IOException e) {
			LOG.error("Can not load group PetersMeters.", e);
		}

		return query;
	}

	/**
	 * Bewaren van nieuw aangemaakte PeterMeter.
	 * 
	 */
	@RunPrivileged
	public void saveNewPeterMeter() {

		try {
			User object = getObject();

			if (!checkUsernameExists(object.getUsername())) {
				// Generate password
				String password = RandomStringUtils.randomAlphanumeric(8);
				object.setPassword(password);

				setHasErrors(false);

				object.getAspect("PeterMeterProfiel", modelRepository, true);

				// Save User UserProfile PeterMeterProfiel and
				// PeterMeterNaamCode via Listener
				modelRepository.saveObject(object);

				// Set Document owner naar nieuwe PM
				Document<User> document = (Document<User>) object.getAspect(
						"Document", modelRepository, true);
				document.setOwner(modelRepository.getResourceName(object));
				modelRepository.saveDocument(document);

				// Assign to group
				addUserToPeterMeterGroup(modelRepository
						.getResourceName(object));

				// Send mail
				String mailServiceStatus = DefaultConfiguration.instance()
						.getString("service.mail.peterMeter", "on");

				if (mailServiceStatus.equalsIgnoreCase("on")) {
					sendCredentailsMail(object.getUsername(), (String) object
							.getAspect("UserProfile").get("email"), password);
				}

				messages.info("Nieuwe peter/meter succesvol aangemaakt.");

				clear();
				search();
			} else {
				setHasErrors(true);
				messages.error("Gebruikersnaam bestaat al. Gelieve een andere gebruikersnaam te kiezen.");
			}
		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
		} catch (Exception e) {
			messages.error("Peter/Meter niet bewaard: fout bij het versturen van confirmatie e-mail.");
			LOG.error("Can not send mail.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Verwijderen van PeterMeter.
	 * 
	 */
	@RunPrivileged
	@Override
	public void delete() {
		try {

			// Delete trajectToewijzingen PM en delete uit groep gebeurt via
			// UserDeleteListener
			// Delete user and document permissions
			modelRepository.deleteObject(object);

			clear();
			search();
			messages.info("Peter/Meter succesvol verwijderd.");
		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van Peter/Meter: "
					+ e.getMessage());
			LOG.error("Can not delete model object.", e);
		}
	}

	/**
	 * Sturen van email naar nieuwe PeterMeter met login credentials.
	 * 
	 * @param username
	 * @param email
	 * @param password
	 * @throws Exception
	 */
	private void sendCredentailsMail(String username, String email,
			String password) throws Exception {

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);

		variables.put("username", username);
		variables.put("password", password);
		variables.put("group", userRepository.listGroupnames(username));

		mailSender.sendMail(
				preferences.getNoreplyEmail(),
				Collections.singleton(object.getAspect("UserProfile")
						.get("email").toString()),
				"/META-INF/resources/core/mails/newPeterMeter.fmt", variables);
	}

	/**
	 * Checken of gebruikersnaam reeds bestaat in het systeem.
	 * 
	 * @param username
	 * @return
	 */
	private boolean checkUsernameExists(String username) {

		try {
			Query query = new DefaultQuery("User");
			query.addFilter(FilterUtils.equal("username", username));
			if (modelRepository.countObjects(query, false) == 0) {
				return false;
			}
		} catch (IOException e) {
			LOG.error("Can not count objects.", e);
		}

		return true;
	}

	/**
	 * Toevoegen van Gebruiker aan PeterMeter groep.
	 * 
	 * @param resourceName
	 */
	@RunPrivileged
	private void addUserToPeterMeterGroup(ResourceName resourceName) {

		try {
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", "PeterMeter"));
			group.getMembers().add(resourceName);
			modelRepository.saveObject(group);
		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
		}
	}

	@Override
	public void save() {

		try {
			checkVoorkeuren();

			hasErrors = false;

			modelRepository.saveObject(object);

			messages.info(documentMessages.documentSaveSuccess(ObjectUtils
					.toString(modelRepository.getResourceIdentifier(object))));

			clear();
			search();
		} catch (IOException e) {
			hasErrors = true;
			// messages.error(documentMessages.documentSaveFailed(e.getMessage()));
			LOG.error("Can not save PeterMeter.", e);
		}
	}

	/**
	 * Checken of Voorkeuren voldoen aan de voorwaarden voor opslag.
	 * 
	 * @return
	 * @throws IOException
	 */
	private void checkVoorkeuren() throws IOException {

		PeterMeterProfiel profiel = (PeterMeterProfiel) object
				.getAspect("PeterMeterProfiel");

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
						messages.error(message);
						throw new IOException(message);
					}

					if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
						counterLente++;
					}
					if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
						counterZomer++;
					}
					if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
						counterHerfst++;
					}
				} else {
					message = "Bewaren profiel niet gelukt: Gelieve voor elke voorkeur een periode, trajectype en regio op te geven.";
					messages.error(message);
					throw new IOException(message);
				}

			}

			if (counterLente > 3 || counterZomer > 3 || counterHerfst > 3) {
				message = "Bewaren profiel niet gelukt: Per periode zijn er maximaal 3 voorkeuren toegelaten.";
				messages.error(message);
				throw new IOException(message);
			}
		}
	}

	/**
	 * Custom PeterMeter CSV export
	 * 
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> report() {

		DefaultModelObjectList objectList = new DefaultModelObjectList<User>(
				getModelClass(), getResults());
		return new EncodableContent<ModelObjectList>(
				(Encoder) new PeterMeterCSVModelEncoder(), objectList);
	}
}
