package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.encoder.SCSVModelEncoder;
import org.conscientia.core.handler.data.annotation.ContentTypeLiteral;
import org.conscientia.core.handler.data.annotation.ExtensionLiteral;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.security.Identity;
import org.jboss.solder.servlet.http.RequestParam;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.api.Encoder;
import be.gim.commons.filter.FilterUtils;
import be.gim.peritia.codec.EncodableContent;
import be.gim.peritia.io.content.Content;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class ControleOpdrachtOverzichtFormBase implements Serializable {

	private static final Log LOG = LogFactory
			.getLog(ControleOpdrachtOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected ModelRepository modelRepository;
	@Inject
	protected Identity identity;
	@Inject
	protected UserRepository userRepository;

	@Inject
	@RequestParam
	protected String name;

	protected Query query;
	protected List<?> results;
	protected ModelObject object;

	@PostConstruct
	public void init() throws IOException {
		name = "ControleOpdracht";
		search();
	}

	// GETTERS AND SETTERS
	public Query getQuery() {

		if (query == null) {
			query = new DefaultQuery(name);
		}
		try {
			if (identity.inGroup("Medewerker", "CUSTOM")) {

				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceKey(userRepository.loadUser(identity
								.getUser().getId()))));
				return query;
			}
			if (identity.inGroup("PeterMeter", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("peterMeter", modelRepository
						.getResourceKey(userRepository.loadUser(identity
								.getUser().getId()))));
				return query;
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}
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

	public String getFileName() {
		return name + ".csv";
	}

	public ModelClass getModelClass() {
		return modelRepository.getModelClass(name);
	}

	public String getAction() {
		return Permission.SEARCH_ACTION;
	}

	public boolean hasPermission() {
		return identity.hasPermission(getModelClass(), getAction());
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

	public boolean isCanUpload() {
		return isCanEdit(object)
				&& identity.hasPermission(getModelClass(),
						Permission.CREATE_ACTION)
				&& getModelClass().getHandler(DataHandler.class) != null;
	}

	public boolean isCanDownload() {
		return identity
				.hasPermission(getModelClass(), Permission.SEARCH_ACTION);
	}

	public void search() throws IOException {
		try {
			results = modelRepository.searchObjects(getQuery(), true, true);
		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			results = null;
		}
	}

	public void create() {
		try {
			object = modelRepository.createObject(getModelClass(), null);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at creation model object.", e);
		}
	}

	public void save() {
		try {
			modelRepository.saveObject((StorableObject) object);
			clear();
			search();
		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
		}
	}

	public void delete() {
		try {
			modelRepository.deleteObject((StorableObject) object);
			clear();
			search();
		} catch (IOException e) {
			LOG.error("Can not delete model object.", e);
		}
	}

	public void clear() throws IOException {
		object = null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> report() {
		DefaultModelObjectList objectList = new DefaultModelObjectList<ModelObject>(
				getModelClass(), (List<ModelObject>) getResults());
		return new EncodableContent<ModelObjectList>(
				(Encoder) new SCSVModelEncoder(), objectList);
	}
}
