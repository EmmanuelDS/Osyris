package be.gim.tov.osyris.form;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.controle.ControleOpdracht;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class ControleOpdrachtOverzichtFormBase extends
		AbstractListForm<ControleOpdracht> {
	private static final long serialVersionUID = -86881009141250710L;

	private static final Log LOG = LogFactory
			.getLog(ControleOpdrachtOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected UserRepository userRepository;

	protected String controleOpdrachtType;

	public String getControleOpdrachtType() {
		return controleOpdrachtType;
	}

	public void setControleOpdrachtType(String controleOpdrachtType) {
		this.controleOpdrachtType = controleOpdrachtType;
	}

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "ControleOpdracht";
	}

	@Override
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(getName());
	}

	@Override
	public Query getQuery() {

		if (query == null) {
			query = getDefaultQuery();
		}

		try {
			if (identity.inGroup("Medewerker", "CUSTOM")) {

				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceIdentifier(userRepository.loadUser(identity
								.getUser().getId()))));
				return query;
			}
			if (identity.inGroup("PeterMeter", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("peterMeter", modelRepository
						.getResourceIdentifier(userRepository.loadUser(identity
								.getUser().getId()))));
				return query;
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}

		return query;
	}

	@Override
	public void create() {
		try {

			object = null;
			if (controleOpdrachtType.equals("route")) {
				object = (ControleOpdracht) modelRepository.createObject(
						modelRepository.getModelClass("RouteControleOpdracht"),
						null);
			} else if (controleOpdrachtType.equals("netwerk")) {
				object = (ControleOpdracht) modelRepository
						.createObject(modelRepository
								.getModelClass("NetwerkControleOpdracht"), null);
			}
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at creation model object.", e);
		}
	}
}
