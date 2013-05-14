package be.gim.tov.osyris.form;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ManagedObject;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.permission.Permission;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.search.DefaultQuery;

import be.gim.commons.filter.FilterUtils;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class ControleOpdrachtOverzichtFormBase extends AbstractOverzichtForm {

	private static final Log log = LogFactory
			.getLog(ControleOpdrachtOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected UserRepository userRepository;

	// METHODS
	@Override
	@PostConstruct
	public void init() throws IOException {
		name = "ControleOpdracht";
		search();
	}

	@Override
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(name);
	}

	@Override
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
			log.error("Can not load user.", e);
		}
		return query;
	}

	@Override
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

	@Override
	public void search() {
		try {
			results = modelRepository.searchObjects(getQuery(), true, true,
					PAGE_SIZE);
		} catch (IOException e) {
			log.error("Can not get search results.", e);
			results = null;
		}
	}
}
