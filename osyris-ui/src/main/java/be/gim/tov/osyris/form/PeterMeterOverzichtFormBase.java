package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.document.Document;
import org.conscientia.api.group.Group;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.permission.Permissions;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.permission.DefaultPermission;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.user.UserUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Identity;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class PeterMeterOverzichtFormBase extends AbstractListForm<User> {

	private static final long serialVersionUID = 7761265026167905576L;
	private static final Log LOG = LogFactory
			.getLog(PeterMeterOverzichtFormBase.class);
	private static final String PERIODE_LENTE = "1";
	private static final String PERIODE_ZOMER = "2";
	private static final String PERIODE_HERFST = "3";

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected Identity identity;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected Messages messages;

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
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
	public void search() {
		results = getAllPetersMeters();
	}

	@SuppressWarnings("unchecked")
	public void saveNewPeterMeter() {
		try {
			UserProfile userProfile = (UserProfile) object
					.getAspect("UserProfile");

			if (!checkUsernameExists(object.getUsername())) {

				// Generate password
				String password = RandomStringUtils.randomAlphanumeric(8);
				object.setPassword(password);

				// Document
				ResourceName name = UserUtils.getUserDocumentName(object
						.getUsername());

				Document<User> document = (Document<User>) modelRepository
						.createObject("Document", name);
				document.setName(name);
				document.setSearchable(false);

				String firstName = userProfile.getFirstName();
				String lastName = userProfile.getLastName();
				if (StringUtils.isNotBlank(firstName)
						|| StringUtils.isNotBlank(lastName)) {
					document.setTitle(new DefaultInternationalString(firstName
							+ " " + lastName));
				} else {
					document.setTitle(new DefaultInternationalString(object
							.getUsername()));
				}
				document.setOwner(name);
				document.set("object", object);
				modelRepository.saveDocument(document);

				// User
				object.putAspect(userProfile);
				object.putAspect(object.getAspect("PeterMeterProfiel"));
				modelRepository.saveObject(object);

				// Set permissions
				setDocumentPermissions(name, document);

				// Assign to group
				ResourceName resourceName = new ResourceName("object",
						object.getUsername());
				addUserToPeterMeterGroup(resourceName);

				// Send mail

				sendCredentailsMail(
						object.getUsername(),
						object.getAspect("UserProfile").get("email").toString(),
						password);

				clear();
				search();

			} else {
				messages.error("Gebruikersnaam bestaat al.");
			}
		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
		} catch (Exception e) {
			LOG.error("Can not send mail.", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void delete() {

		try {
			Permissions permissions = (Permissions) modelRepository.loadAspect(
					modelRepository.getModelClass("Permissions"),
					new ResourceName("user", object.getUsername()),
					FilterUtils.equal("scope", "document"));

			// Delete user from PM group
			List<Group> groups = (List<Group>) modelRepository.searchObjects(
					new DefaultQuery("Group"), false, false);
			for (Group group : groups) {
				if (group.getGroupname().equals("PeterMeter")) {
					group.getMembers().remove(
							modelRepository.getResourceName(object));
				}
			}

			// Delete user and document permissions
			modelRepository.deleteObject(object);

			if (permissions != null) {
				modelRepository.deleteAspect(permissions);
			}

			clear();
			search();
		} catch (IOException e) {
			LOG.error("Can not delete model object.", e);
		}
	}

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

	@SuppressWarnings("unchecked")
	private void addUserToPeterMeterGroup(ResourceName resourceName) {
		try {
			Query query = new DefaultQuery("Group");
			query.addFilter(FilterUtils.equal("groupname", "PeterMeter"));
			List<Group> groups;
			groups = (List<Group>) modelRepository.searchObjects(query, true,
					true);
			Group group = (Group) ModelRepository.getUniqueResult(groups);
			group.getMembers().add(resourceName);
			modelRepository.saveObject(group);

		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void setDocumentPermissions(ResourceName name, Document document) {
		try {

			Permissions permissions = (Permissions) modelRepository.loadAspect(
					modelRepository.getModelClass("Permissions"), name,
					FilterUtils.equal("scope", "document"));

			if (permissions == null) {
				permissions = (Permissions) modelRepository.createAspect(
						modelRepository.getModelClass("Permissions"), name);
			}
			// Set scope
			permissions.set("scope", "document");

			// Add necessary permissions
			permissions.getPermissions().addAll(getAllowedPermissions(name));

			// Save permissions
			modelRepository.saveAspect(permissions);
		} catch (IOException e) {
			LOG.error("Can not load aspect.", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate aspect.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Can not access aspect.", e);
		}
	}

	private List<Permission> getAllowedPermissions(ResourceName name) {
		List<Permission> permissions = new ArrayList<Permission>();

		permissions.add(new DefaultPermission(name, "view", true));
		permissions.add(new DefaultPermission(name, "edit", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Routedokter"), "view", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Routedokter"), "edit", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Medewerker"), "view", true));
		permissions.add(new DefaultPermission(new ResourceName("group",
				"Medewerker"), "edit", true));

		return permissions;
	}

	@SuppressWarnings("unchecked")
	private List<User> getAllPetersMeters() {

		try {
			List<User> petersMeters = new ArrayList<User>();

			// Filter Peters and meters
			// TODO: possible to do it in a Query Filter?
			List<User> users = (List<User>) modelRepository.searchObjects(
					getQuery(), true, true, true);
			for (User user : users) {
				if (userRepository.listGroupnames(user).contains("PeterMeter")) {
					petersMeters.add(user);
				}
			}

			return petersMeters;
		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			return null;
		}
	}

	/**
	 * Toekennen van Peters en Meters aan Trajecten, voorlopig enkel voor
	 * PetersMeters met 1 specifieke voorkeur
	 * 
	 * TODO: toekenning in geval van meerdere voorkeuren TODO: toekenning aan
	 * lussen
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void kenPetersMetersToe() {

		DefaultQuery query = new DefaultQuery();
		query.setModelClassName("Traject");
		try {
			List<User> petersMeters = getAllPetersMeters();

			for (User peterMeter : petersMeters) {
				PeterMeterProfiel profiel = (PeterMeterProfiel) peterMeter
						.getAspect("PeterMeterProfiel");
				ResourceName resourceName = modelRepository
						.getResourceName(peterMeter);

				// Peters en Meters met 1 voorkeur krijgen voorrang
				if (profiel.getVoorkeuren().size() == 1) {
					for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {
						DefaultQuery q = new DefaultQuery();
						q.setModelClassName("Traject");
						q.addFilter(FilterUtils.equal("naam",
								voorkeur.getTrajectNaam()));

						// Zoeken naar voorkeurstraject
						List<Traject> t = (List<Traject>) modelRepository
								.searchObjects(q, false, false);
						if (t.size() == 1) {
							Traject tr = t.get(0);
							// Voor elke periode PM toekennen
							tr.setPeterMeter1(assignPeterMeter(tr,
									resourceName, voorkeur.getPeriode()));
							modelRepository.saveObject(tr);
						}
					}
				}
			}
		} catch (IOException e) {
			LOG.error("Can not search trajecten", e);
		}
	}

	/**
	 * Eigenlijke toekenning van de PeterMeter aan een traject in een bepaalde
	 * periode. In geval van conflict wordt de PeterMeter met langste staat van
	 * dienst gekozen.
	 * 
	 **/
	private ResourceName assignPeterMeter(Traject traject,
			ResourceName peterMeter1, String periode) {
		try {
			// Identificeren van de periode
			ResourceIdentifier compareToPeterMeterInPeriode = null;
			if (periode.equals(PERIODE_LENTE)) {
				compareToPeterMeterInPeriode = traject.getPeterMeter1();
			}
			if (periode.equals(PERIODE_ZOMER)) {
				compareToPeterMeterInPeriode = traject.getPeterMeter2();
			}
			if (periode.equals(PERIODE_HERFST)) {
				compareToPeterMeterInPeriode = traject.getPeterMeter3();
			}

			// Indien al een andere peterMeter is toegekend
			if (compareToPeterMeterInPeriode != null
					&& compareToPeterMeterInPeriode != peterMeter1) {

				PeterMeterProfiel profielPM1 = (PeterMeterProfiel) modelRepository
						.loadObject(peterMeter1).getAspect("PeterMeterProfiel");
				PeterMeterProfiel profielPM2 = (PeterMeterProfiel) modelRepository
						.loadObject(traject.getPeterMeter1()).getAspect(
								"PeterMeterProfiel");

				Calendar cal1 = Calendar.getInstance();
				Calendar cal2 = Calendar.getInstance();
				cal1.setTime(profielPM1.getActiefSinds());
				cal2.setTime(profielPM2.getActiefSinds());

				// Degene met de langste staat van dienst wordt toegekend
				if (cal1.before(cal2)) {
					return peterMeter1;
				} else {
					return (ResourceName) compareToPeterMeterInPeriode;
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
		return peterMeter1;
	}
}
