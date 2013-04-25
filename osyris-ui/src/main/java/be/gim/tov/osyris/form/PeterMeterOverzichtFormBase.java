package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.document.Document;
import org.conscientia.api.group.Group;
import org.conscientia.api.handler.data.DataHandler;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ManagedObject;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.permission.Permissions;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.encoder.SCSVModelEncoder;
import org.conscientia.core.handler.data.annotation.ContentTypeLiteral;
import org.conscientia.core.handler.data.annotation.ExtensionLiteral;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.permission.DefaultPermission;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.user.UserUtils;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Identity;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.api.Encoder;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.resource.ResourceName;
import be.gim.peritia.codec.EncodableContent;
import be.gim.peritia.io.content.Content;

/**
 * 
 * @author kristof
 * 
 */
public class PeterMeterOverzichtFormBase implements Serializable {

	private static final Log log = LogFactory
			.getLog(PeterMeterOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private ModelRepository modelRepository;

	@Inject
	private UserRepository userRepository;

	@Inject
	private Identity identity;

	@Inject
	private Preferences preferences;

	@Inject
	private MailSender mailSender;

	@Inject
	private Messages messages;

	private Query query;
	private List<?> results;
	private ModelObject object;
	private User user;

	@PostConstruct
	public void init() throws IOException {
		search();
	}

	// GETTERS AND SETTERS
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public List<?> getResults() {
		return results;
	}

	public ModelObject getObject() {
		return object;
	}

	public void setObject(ModelObject object) {
		this.object = object;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	// METHODS
	public ModelClass getModelClass() {
		return modelRepository.getModelClass("User");
	}

	public String getFileName() {
		return getModelClass().getName() + ".csv";
	}

	public String getAction() {
		return Permission.SEARCH_ACTION;
	}

	public boolean hasPermission() {
		return identity.hasPermission(getModelClass(), getAction());
	}

	@SuppressWarnings("unchecked")
	public void search() throws IOException {
		try {
			if (query != null) {
				// results = modelRepository.searchObjects(query, true, true);
				List<User> users = new ArrayList<User>();
				List<User> test = new ArrayList<User>();
				users = (List<User>) modelRepository.searchObjects(query, true,
						true);
				for (User user : users) {
					if (userRepository.listGroupnames(user).contains(
							"PeterMeter")) {
						test.add(user);
					}
				}
				results = test;

			} else {
				// results = modelRepository.searchObjects(new
				// DefaultQuery(getModelClass().getName()), true, true);
				List<User> users = new ArrayList<User>();
				List<User> test = new ArrayList<User>();
				users = (List<User>) modelRepository
						.searchObjects(new DefaultQuery(getModelClass()
								.getName()), true, true);
				for (User user : users) {
					if (userRepository.listGroupnames(user).contains(
							"PeterMeter")) {
						test.add(user);
					}
				}
				results = test;
			}
		} catch (IOException e) {
			log.error("Can not get search results.", e);
			results = null;
		}
	}

	public boolean isCanView(ModelObject object) {
		return identity.hasPermission(object, Permission.VIEW_ACTION);
	}

	public boolean isCanCreate() {
		return isCanEdit(object)
				&& identity.hasPermission(getModelClass(),
						Permission.CREATE_ACTION);
	}

	public boolean isCanEdit(ModelObject object) {

		if (object != null) {
			return (getModelClass().hasInterface(StorableObject.class) || getModelClass()
					.hasInterface(ManagedObject.class))
					&& identity.hasPermission(object, Permission.EDIT_ACTION);
		} else {
			return (getModelClass().hasInterface(StorableObject.class) || getModelClass()
					.hasInterface(ManagedObject.class))
					&& identity.hasPermission(getModelClass(),
							Permission.EDIT_ACTION);
		}
	}

	public boolean isCanDelete(ModelObject object) {
		return isCanEdit(object)
				&& identity.hasPermission(getModelClass(),
						Permission.DELETE_ACTION);
	}

	public boolean isCanDownload() {
		return identity
				.hasPermission(getModelClass(), Permission.SEARCH_ACTION);
	}

	public void create() {
		try {
			object = modelRepository.createObject(getModelClass(), null);
		} catch (InstantiationException e) {
			log.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			log.error("Illegal access at creation model object.", e);
		}
	}

	public void save() {
		try {
			user = (User) object;
			modelRepository.saveObject(user);
			clear();
			search();
		} catch (IOException e) {
			log.error("Can not save model object.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void saveNewPeterMeter() {
		try {
			user = (User) object;
			UserProfile userProfile = (UserProfile) user
					.getAspect("UserProfile");

			// user.setUsername(userProfile.getFirstName().toLowerCase() + "."
			// + userProfile.getLastName().toLowerCase());

			if (!checkUsernameExists(user.getUsername())) {

				// Generate password
				String password = RandomStringUtils.randomAlphanumeric(8);
				user.setPassword(password);

				// Document
				ResourceName name = UserUtils.getUserDocumentName(user
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
					document.setTitle(new DefaultInternationalString(user
							.getUsername()));
				}
				document.setOwner(name);
				document.set("object", user);
				modelRepository.saveDocument(document);

				// User
				user.putAspect(userProfile);
				user.putAspect(user.getAspect("PeterMeterProfiel"));
				modelRepository.saveObject(user);

				// Set permissions
				setDocumentPermissions(name, document);

				// Assign to group
				ResourceName resourceName = new ResourceName("user",
						user.getUsername());
				addUserToPeterMeterGroup(resourceName);

				// Send mail
				sendCredentailsMail(user.getUsername(),
						user.getAspect("UserProfile").get("email").toString(),
						password);
				clear();
				search();

			} else {
				messages.error("Gebruikersnaam bestaat al.");
			}
		} catch (IOException e) {
			log.error("Can not save model object.", e);
		} catch (Exception e) {
			log.error("Can not send mail.", e);
			throw new RuntimeException(e);
		}
	}

	public void delete() {
		try {

			User user = (User) object;
			Permissions permissions = (Permissions) modelRepository.loadAspect(
					modelRepository.getModelClass("Permissions"),
					new ResourceName("user", user.getUsername()),
					FilterUtils.equal("scope", "document"));

			// Delete user and document permissions
			modelRepository.deleteObject((StorableObject) object);

			if (permissions != null) {
				modelRepository.deleteAspect(permissions);
			}
			clear();
			search();
		} catch (IOException e) {
			log.error("Can not delete model object.", e);
		}
	}

	public void clear() throws IOException {
		object = null;
	}

	public void onFileUpload(FileUploadEvent event) throws Exception {

		UploadedFile file = event.getUploadedFile();
		try {
			String fileName = file.getName();
			String contentType = file.getContentType();
			String extension = FilenameUtils.getExtension(fileName);

			DataHandler<?> dataHandler = Beans.getReference(DataHandler.class,
					new ContentTypeLiteral(contentType), new ExtensionLiteral(
							extension));
			if (dataHandler == null) {
				dataHandler = Beans.getReference(DataHandler.class,
						new ExtensionLiteral(extension));
			}
			if (dataHandler == null) {
				dataHandler = Beans.getReference(DataHandler.class,
						new ContentTypeLiteral(contentType));
			}
			if (dataHandler == null) {
				dataHandler = Beans.getReference(DataHandler.class);
			}

			this.object = dataHandler.create(fileName, contentType,
					file.getInputStream());
		} finally {
			file.delete();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> report() {
		DefaultModelObjectList objectList = new DefaultModelObjectList<ModelObject>(
				getModelClass(), (List<ModelObject>) getResults());
		return new EncodableContent<ModelObjectList>(
				(Encoder) new SCSVModelEncoder(), objectList);
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
				Collections.singleton(user.getAspect("UserProfile")
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
			log.error("Can not count objects.", e);
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
			log.error("Can not search objects.", e);
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
			for (DefaultPermission p : getAllowedPermissions(name)) {
				permissions.addPermission(p);
			}

			// Save
			modelRepository.saveAspect(permissions);

		} catch (IOException e) {
			log.error("Can not load aspect.", e);
		} catch (InstantiationException e) {
			log.error("Can not instantiate aspect.", e);
		} catch (IllegalAccessException e) {
			log.error("Can not access aspect.", e);
		}
	}

	private List<DefaultPermission> getAllowedPermissions(ResourceName name) {

		DefaultPermission p1 = new DefaultPermission(name,
				Permission.VIEW_ACTION, true);

		DefaultPermission p2 = new DefaultPermission(name,
				Permission.EDIT_ACTION, true);

		DefaultPermission p3 = new DefaultPermission(new ResourceName("group",
				"Routedokter"), Permission.VIEW_ACTION, true);

		DefaultPermission p4 = new DefaultPermission(new ResourceName("group",
				"Routedokter"), Permission.EDIT_ACTION, true);

		DefaultPermission p5 = new DefaultPermission(new ResourceName("group",
				"Medewerker"), Permission.VIEW_ACTION, true);

		DefaultPermission p6 = new DefaultPermission(new ResourceName("group",
				"Medewerker"), Permission.EDIT_ACTION, true);

		List<DefaultPermission> permissionList = new ArrayList<DefaultPermission>();
		permissionList.add(p1);
		permissionList.add(p2);
		permissionList.add(p3);
		permissionList.add(p4);
		permissionList.add(p5);
		permissionList.add(p6);

		for (DefaultPermission p : permissionList) {
			p.setModelClassLoader(modelRepository.getModelContext());
		}

		return permissionList;
	}
}
