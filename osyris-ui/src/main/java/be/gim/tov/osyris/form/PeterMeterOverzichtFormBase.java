package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.document.Document;
import org.conscientia.api.group.Group;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.permission.Permissions;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.model.AbstractModelObject;
import org.conscientia.core.permission.DefaultPermission;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.user.UserUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Identity;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.resource.ResourceName;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PeterMeterOverzichtFormBase extends AbstractListForm<User> {

	private static final long serialVersionUID = 7761265026167905576L;
	private static final Log LOG = LogFactory
			.getLog(PeterMeterOverzichtFormBase.class);

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
	@SuppressWarnings("unchecked")
	public void search() {
		try {
			results = new ArrayList<User>();

			// Filter Peters and meters
			List<User> users = new ArrayList<User>();
			// TODO: possible to do it in a Query Filter?
			users = (List<User>) modelRepository.searchObjects(getQuery(),
					true, true, PAGE_SIZE);
			for (User user : users) {
				if (userRepository.listGroupnames(user).contains("PeterMeter")) {
					((List<User>) results).add(user);
				}
			}
		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			results = null;
		}
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
			modelRepository.deleteObject((StorableObject) object);

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

		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(email),
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

		for (Permission permission : permissions) {
			((AbstractModelObject) permission)
					.setModelClassLoader(modelRepository.getModelContext());
		}

		return permissions;
	}
}
