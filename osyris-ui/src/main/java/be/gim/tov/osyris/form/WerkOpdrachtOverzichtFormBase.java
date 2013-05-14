package be.gim.tov.osyris.form;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelClass;
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
public class WerkOpdrachtOverzichtFormBase extends AbstractOverzichtForm {

	private static final Log LOG = LogFactory
			.getLog(WerkOpdrachtOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private UserRepository userRepository;

	// METHODS
	@Override
	@PostConstruct
	public void init() throws IOException {
		name = "WerkOpdracht";
		search();
	}

	@Override
	public Query getQuery() {

		if (query == null) {
			query = new DefaultQuery(name);
		}
		try {
			if (identity.inGroup("Uitvoerder", "CUSTOM")) {

				query.addFilter(FilterUtils.equal("uitvoerder", modelRepository
						.getResourceKey(userRepository.loadUser(identity
								.getUser().getId()))));
				return query;
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}
		return query;
	}

	@Override
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(name);
	}

	@Override
	public void search() {
		try {
			results = modelRepository.searchObjects(getQuery(), true, true,
					PAGE_SIZE);
		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			results = null;
		}
	}
}
