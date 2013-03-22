package org.conscientia.core.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ManagedObject;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.search.Query;
import org.conscientia.core.encoder.SCSVModelEncoder;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.security.Identity;
import org.jboss.solder.servlet.http.RequestParam;

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
	@RequestParam
	private String name;

	private Query query;
	private List<?> searchResults;
	private ModelObject object;

	// GETTERS AND SETTERS
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public List<?> getSearchResults() {

		if (searchResults == null) {
			try {
				if (query != null) {
					searchResults = modelRepository.searchObjects(query, true,
							true);
				} else {
					searchResults = modelRepository.searchObjects(
							new DefaultQuery(name), true, true);
				}
			} catch (IOException e) {
				log.error("Can not get search results.", e);
			}
		}
		return searchResults;
	}

	public void setSearchResults(List<?> searchResults) {
		this.searchResults = searchResults;
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

	// METHODS
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(name);
	}

	public boolean isCanCreate() {
		return isCanEdit(object)
				&& Beans.getReference(Identity.class).hasPermission(
						getModelClass(), "create");
	}

	public boolean isCanEdit(ModelObject object) {

		if (object != null) {
			return (getModelClass().hasInterface(StorableObject.class) || getModelClass()
					.hasInterface(ManagedObject.class))
					&& Beans.getReference(Identity.class).hasPermission(object,
							"edit");
		} else {
			return (getModelClass().hasInterface(StorableObject.class) || getModelClass()
					.hasInterface(ManagedObject.class))
					&& Beans.getReference(Identity.class).hasPermission(
							getModelClass(), "edit");
		}
	}

	public boolean isCanDelete(ModelObject object) {
		return isCanEdit(object)
				&& Beans.getReference(Identity.class).hasPermission(
						getModelClass(), "delete");
	}

	public void searchObjects() throws IOException {
		this.searchResults = null;
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
		searchResults = null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> downloadReport() {

		DefaultModelObjectList objectList = new DefaultModelObjectList<ModelObject>(
				getModelClass(), (List<ModelObject>) getSearchResults());
		return new EncodableContent<ModelObjectList>(
				(Encoder) new SCSVModelEncoder(), objectList);
	}
}
