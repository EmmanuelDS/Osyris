package org.conscientia.core.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import be.gim.peritia.codec.EncodableContent;
import be.gim.peritia.io.content.Content;

@Named
@ViewScoped
public class ListForm extends AbstractForm implements Serializable {
	private static final long serialVersionUID = 3676671150227033873L;
	private static final Log log = LogFactory.getLog(ListForm.class);

	// VARIABLES
	@Inject
	private ModelRepository modelRepository;
	@Inject
	protected Identity identity;

	@Inject
	@RequestParam
	private String name;

	private Query query;
	private List<?> results;
	private ModelObject object;

	// GETTERS AND SETTERS
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	@SuppressWarnings("unchecked")
	public List<?> getSearchResults() {

		if (results == null) {
			try {
				if (query != null) {
					// List<ModelObject> objects = (List<ModelObject>)
					// modelRepository
					// .searchObjects(query, false, false);
					// query.addFilter(FilterUtils
					// .id(findViewableObjectIds(objects)));
					results = modelRepository.searchObjects(query, true, true);
				} else {
					// List<ModelObject> objects = (List<ModelObject>)
					// modelRepository
					// .searchObjects(new DefaultQuery(name), false, false);
					Query q = new DefaultQuery(name);
					// q.addFilter(FilterUtils.id(findViewableObjectIds(objects)));
					results = modelRepository.searchObjects(q, true, true);
				}
			} catch (IOException e) {
				log.error("Can not get search results.", e);
			}
		}
		return results;
	}

	public void setSearchResults(List<?> searchResults) {
		this.results = searchResults;
	}

	public ModelObject getObject() {
		if (object == null) {
			createObject();
		}
		return object;
	}

	public void setObject(ModelObject object) {
		this.object = object;
	}

	public String getFileName() {
		return name + ".csv";
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

	public ModelClass getModelClass() {
		return modelRepository.getModelClass(name);
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
				&& identity.hasPermission(getModelClass(), "create")
				&& getModelClass().getHandler(DataHandler.class) != null;
	}

	public boolean isCanDownload() {
		return identity.hasPermission(getModelClass(), "search");
	}

	public void searchObjects() throws IOException {
		this.results = null;
	}

	public void createObject() {
		try {
			object = modelRepository.createObject(getModelClass(), null);
		} catch (InstantiationException e) {
			log.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			log.error("Illegal access at creation model object.", e);
		}
	}

	public void saveObject() {
		try {
			modelRepository.saveObject((StorableObject) object);
			clear();
		} catch (IOException e) {
			log.error("Can not save model object.", e);
		}
	}

	public void deleteObject() {
		try {
			modelRepository.deleteObject((StorableObject) object);
			clear();
		} catch (IOException e) {
			log.error("Can not delete model object.", e);
		}
	}

	public void clear() throws IOException {
		object = null;
		results = null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> downloadReport() {

		DefaultModelObjectList objectList = new DefaultModelObjectList<ModelObject>(
				getModelClass(), (List<ModelObject>) getSearchResults());
		return new EncodableContent<ModelObjectList>(
				(Encoder) new SCSVModelEncoder(), objectList);
	}

	private List<String> findViewableObjectIds(List<ModelObject> objects) {
		List<String> ids = new ArrayList<String>();
		for (ModelObject object : objects) {
			if (isCanView(object)) {
				ids.add(object.get("id").toString());
			}
		}
		return ids;
	}
}
