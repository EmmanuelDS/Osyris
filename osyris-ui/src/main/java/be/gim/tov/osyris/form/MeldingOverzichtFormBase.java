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
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class MeldingOverzichtFormBase extends AbstractListForm<Melding> {
	private static final long serialVersionUID = -3077755833706449795L;

	private static final Log LOG = LogFactory
			.getLog(MeldingOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected UserRepository userRepository;

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "Melding";
	}

	@Override
	public Query getQuery() {

		if (query == null) {
			query = getDefaultQuery();
		}

		if (identity.inGroup("Routedokter", "CUSTOM")) {
			return query;
		}
		if (identity.inGroup("Medewerker", "CUSTOM")) {
			try {
				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceIdentifier(userRepository.loadUser(identity
								.getUser().getId()))));
			} catch (IOException e) {
				LOG.error("Can not load user.", e);
			}
		}
		return query;
	}

	public String getTrajectNaam() {
		if (object != null) {
			try {
				Melding melding = object;
				Traject traject = (Traject) modelRepository.loadObject(melding
						.getTraject());
				Regio regio = (Regio) modelRepository.loadObject(traject
						.getRegio());
				return traject.getNaam() + "(" + regio.getNaam() + ")";
			} catch (IOException e) {
				LOG.error("Can not load Traject.", e);
			}
		}
		return null;
	}
}