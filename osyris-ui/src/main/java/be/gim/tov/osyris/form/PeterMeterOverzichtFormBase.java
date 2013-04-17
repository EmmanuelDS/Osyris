package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.handler.data.DataHandler;
import org.conscientia.api.model.ManagedObject;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.encoder.SCSVModelEncoder;
import org.conscientia.core.handler.data.annotation.ContentTypeLiteral;
import org.conscientia.core.handler.data.annotation.ExtensionLiteral;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.security.Identity;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.api.Encoder;
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

	private Query query;
	private List<?> results;
	private ModelObject object;

	@PostConstruct
	public void init() throws IOException {
		search();
	}

	// METHODS
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
			// User user = (User) object;
		} catch (InstantiationException e) {
			log.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			log.error("Illegal access at creation model object.", e);
		}
	}

	public void save() {
		try {
			modelRepository.saveObject((StorableObject) object);
			// TODO: aan groep PeterMeter toevoegen
			clear();
			search();
		} catch (IOException e) {
			log.error("Can not save model object.", e);
		}
	}

	public void delete() {
		try {
			modelRepository.deleteObject((StorableObject) object);
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
}
