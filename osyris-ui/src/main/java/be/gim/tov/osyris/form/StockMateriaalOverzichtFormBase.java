package be.gim.tov.osyris.form;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.user.UitvoerderBedrijf;
import be.gim.tov.osyris.model.user.UitvoerderProfiel;
import be.gim.tov.osyris.model.werk.StockMateriaal;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class StockMateriaalOverzichtFormBase extends
		AbstractListForm<StockMateriaal> {
	private static final long serialVersionUID = -2212073755366628143L;

	private static final Log LOG = LogFactory
			.getLog(StockMateriaalOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private UserRepository userRepository;

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "StockMateriaal";
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
				query = getDefaultQuery();
			}

			if (identity.inGroup("Uitvoerder", "CUSTOM")
					&& profiel.getBedrijf() != null) {
				UitvoerderBedrijf bedrijf = (UitvoerderBedrijf) modelRepository
						.loadObject(profiel.getBedrijf());
				if (!bedrijf.getNaam().equals("TOV")) {
					query.addFilter(FilterUtils.equal("magazijn",
							bedrijf.getNaam()));
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load aspect.", e);
		}

		return query;
	}
}
