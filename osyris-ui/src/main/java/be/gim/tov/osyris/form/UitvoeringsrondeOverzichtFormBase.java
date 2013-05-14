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

/**
 * 
 * @author kristof
 * 
 */
@Named
public class UitvoeringsrondeOverzichtFormBase extends AbstractOverzichtForm {

	private static final Log LOG = LogFactory
			.getLog(UitvoeringsrondeOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private UserRepository userRepository;

	// METHODS
	@Override
	@PostConstruct
	public void init() throws IOException {
		name = "Uitvoeringsronde";
		search();
	}

	@Override
	public Query getQuery() {

		if (query == null) {
			query = new DefaultQuery(name);
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
