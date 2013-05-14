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
import be.gim.tov.osyris.model.user.UitvoerderProfiel;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class StockMateriaalOverzichtFormBase extends AbstractOverzichtForm {

	private static final Log LOG = LogFactory
			.getLog(StockMateriaalOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private UserRepository userRepository;

	// METHODS
	@Override
	@PostConstruct
	public void init() throws IOException {
		name = "StockMateriaal";
		search();
	}

	@Override
	public Query getQuery() {

		try {
			UitvoerderProfiel profiel = (UitvoerderProfiel) modelRepository
					.loadAspect(modelRepository
							.getModelClass("UitvoerderProfiel"),
							modelRepository.getResourceKey(userRepository
									.loadUser(identity.getUser().getId())));

			if (query == null) {
				query = new DefaultQuery(name);
			}

			if (identity.inGroup("Uitvoerder", "CUSTOM")
					&& !profiel.getBedrijf().getNaam().equals("TOV")) {
				query.addFilter(FilterUtils.equal("magazijn", profiel
						.getBedrijf().getNaam()));
			}
		} catch (IOException e) {
			LOG.error("Can not load aspect.", e);
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
