package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.search.QueryBuilder;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.user.TrajectVerantwoordelijkheid;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class TrajectVerantwoordelijkheidForm extends
		AbstractListForm<TrajectVerantwoordelijkheid> implements Serializable {

	private static final long serialVersionUID = -5418643443924362331L;

	private static final Log LOG = LogFactory
			.getLog(TrajectVerantwoordelijkheidForm.class);

	@Inject
	protected Messages messages;

	private boolean hasErrors;

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "TrajectVerantwoordelijkheid";
	}

	@Override
	public void save() {

		hasErrors = true;

		try {
			// TODO: CHECKEN VAN DE VERANTWOORDELIJKHEDEN
			// TODO: ALGORTIME AUTOMATISCH TOEWIJZEN MEDEWERKER IN
			// OSYRISMODELFUNCTIONS

			if (checkVerantwoordelijkheden()) {

				hasErrors = false;
				modelRepository.saveObject(object);
				messages.info("Trajectverantwoordelijkheid succesvol aangepast.");
				clear();
				search();
			}
		} catch (IOException e) {
			messages.error("Fout bij het bewaren van de trajectverantwoordelijkheid: "
					+ e.getMessage());
			LOG.error("Can not save model object.", e);
		}
	}

	/**
	 * Checken van de verantwoordelijkheden bij het bewaren van het
	 * TrajectVerantwoordelijkheid object dat men wil bewaren. Voor de routes
	 * mag er slechts 1 medewerker per type opgegeven worden. Voor de
	 * fietsnetwerken mag er per regio 1 medewerker worden opgegeven. Voor de
	 * wandelnetwerken mag er 1 medewerker per distinct trajectNaam worden
	 * opgegeven. Als fallback is er een cascading systeem voorzien. Is er bv
	 * geen medewerker voor een wandelnetwerk met trajectnaam, wordt er een
	 * niveau hoger gekeken nl regio etc..
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean checkVerantwoordelijkheden() {

		// Check of het trajectType veld is ingevuld
		if (object.getTrajectType() == null) {
			messages.warn("Gelieve een trajectType te selecteren.");
			return false;
		}
		// Check of het medewerker veld is ingevuld
		if (object.getMedewerker() == null) {
			messages.warn("Gelieve een medewerker te selecteren.");
			return false;
		}

		// Check of trajectType netwerk steeds een regio heeft ingevuld
		if (object.getTrajectType().toLowerCase().contains("netwerk")
				&& object.getRegio() == null) {
			messages.warn("Gelieve voor de trajecten van het type netwerk ook een regio op te geven.");
			return false;
		}

		QueryBuilder builder = new QueryBuilder(object.getModelClass()
				.getName());

		try {
			// Check of er al een verantwoordelijke is voor een trajectNaam
			if (object.getTrajectNaam() != null) {
				builder.addFilter(FilterUtils.equal("trajectNaam",
						object.getTrajectNaam()));

				List<TrajectVerantwoordelijkheid> result = (List<TrajectVerantwoordelijkheid>) modelRepository
						.searchObjects(builder.build(), false, false);

				if (!result.isEmpty()) {
					messages.warn("Er bestaat reeds een verantwoordelijke voor de geselecteerde trajectnaam.");
					return false;
				}
			}

			// Check of er een verantwoordelijke is voor een regio van het
			// geselecteerde trajectType waar de trajectNaam NULL is
			else if (object.getTrajectType() != null
					&& object.getRegio() != null
					&& object.getTrajectNaam() == null) {

				builder.addFilter(FilterUtils.equal("trajectType",
						object.getTrajectType()));
				builder.addFilter(FilterUtils.equal("regio", object.getRegio()));
				builder.addFilter(FilterUtils.isNull("trajectNaam"));

				List<TrajectVerantwoordelijkheid> result = (List<TrajectVerantwoordelijkheid>) modelRepository
						.searchObjects(builder.build(), false, false);

				if (!result.isEmpty()) {
					messages.warn("Er bestaat reeds een verantwoordelijke voor deze regio van het geselecteerde trajectType.");
					return false;
				}
			}

			else if (object.getTrajectType() != null) {
				builder.addFilter(FilterUtils.equal("trajectType",
						object.getTrajectType()));

				List<TrajectVerantwoordelijkheid> result = (List<TrajectVerantwoordelijkheid>) modelRepository
						.searchObjects(builder.build(), false, false);

				if (!result.isEmpty()) {
					messages.warn("Er bestaat reeds een verantwoordelijke voor het geselecteerde trajectType.");
					return false;
				}
			}
		} catch (IOException e) {
			LOG.error("Can not check TrajectVerantwoordelijkheden.");
		}

		return true;

	}

	public void resetParameters() {
		object.setRegio(null);
		object.setTrajectNaam(null);
		object.setMedewerker(null);
	}
}
