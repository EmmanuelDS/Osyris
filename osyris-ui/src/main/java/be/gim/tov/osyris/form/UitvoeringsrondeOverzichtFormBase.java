package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class UitvoeringsrondeOverzichtFormBase extends
		AbstractListForm<Uitvoeringsronde> {

	private static final long serialVersionUID = 3771393152252852618L;

	private static final Log LOG = LogFactory
			.getLog(UitvoeringsrondeOverzichtFormBase.class);

	private static final String GEOMETRY_LAYER_NAME = "geometry";

	// VARIABLES
	@Inject
	protected UserRepository userRepository;

	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected ResourceIdentifier werkOpdracht;
	protected ResourceIdentifier uitvoerder;
	protected ResourceIdentifier medewerker;

	// GETTERS AND SETTERS
	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public String getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(String trajectType) {
		this.trajectType = trajectType;
	}

	public String getTrajectNaam() {
		return trajectNaam;
	}

	public void setTrajectNaam(String trajectNaam) {
		this.trajectNaam = trajectNaam;
	}

	public ResourceIdentifier getWerkOpdracht() {
		return werkOpdracht;
	}

	public void setWerkOpdracht(ResourceIdentifier werkOpdracht) {
		this.werkOpdracht = werkOpdracht;
	}

	public ResourceIdentifier getUitvoerder() {
		return uitvoerder;
	}

	public void setUitvoerder(ResourceIdentifier uitvoerder) {
		this.uitvoerder = uitvoerder;
	}

	public ResourceIdentifier getMedewerker() {
		return medewerker;
	}

	public void setMedewerker(ResourceIdentifier medewerker) {
		this.medewerker = medewerker;
	}

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "Uitvoeringsronde";
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

		if (werkOpdracht != null) {
			query.addFilter(FilterUtils.equal("opdrachten", werkOpdracht));
		}
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void search() {
		try {
			List<Uitvoeringsronde> list = (List<Uitvoeringsronde>) modelRepository
					.searchObjects(getQuery(), true, true, true);
			List<Uitvoeringsronde> filteredList = new ArrayList<Uitvoeringsronde>();

			if (uitvoerder != null) {
				results = searchUitvoeringronde(uitvoerder, list);
				filteredList = results;
			}

			if (medewerker != null) {
				if (filteredList.isEmpty()) {
					results = searchUitvoeringronde(medewerker, list);
				} else {
					results = searchUitvoeringronde(medewerker, filteredList);
				}
			}

			if (uitvoerder == null && medewerker == null) {
				results = list;
			}
		} catch (IOException e) {
			LOG.error("Can not search Uitvoeringsronde.", e);
		}
	}

	/**
	 * Temporary Experimental search helper method
	 * 
	 * @param id
	 * @param list
	 * @return
	 */
	public List<Uitvoeringsronde> searchUitvoeringronde(ResourceIdentifier id,
			List<Uitvoeringsronde> list) {
		List<Uitvoeringsronde> result = new ArrayList<Uitvoeringsronde>();
		try {
			boolean flag = false;
			for (Uitvoeringsronde ronde : list) {
				for (ResourceIdentifier opdrachtId : ronde.getOpdrachten()) {
					WerkOpdracht opdracht = (WerkOpdracht) modelRepository
							.loadObject(opdrachtId);
					if (uitvoerder != null) {
						if (opdracht.getUitvoerder().equals(uitvoerder)) {
							flag = true;
						}
					} else if (medewerker != null) {
						if (opdracht.getMedewerker().equals(medewerker)) {
							flag = true;
						}
					}
				}
				if (flag) {
					result.add(ronde);
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht.", e);
		}
		return result;
	}

	/**
	 * Zoekt alle werkopdrachten in een uitvoeringsronde.
	 * 
	 * @return
	 */
	public List<WerkOpdracht> getWerkOpdrachtenInUitvoeringsronde(
			Uitvoeringsronde ronde) {
		try {
			List<WerkOpdracht> opdrachten = new ArrayList<WerkOpdracht>();
			for (ResourceIdentifier id : ronde.getOpdrachten()) {
				WerkOpdracht opdracht = (WerkOpdracht) modelRepository
						.loadObject(id);
				opdrachten.add(opdracht);
			}
			return opdrachten;
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht", e);
		}
		return Collections.emptyList();
	}

	@Override
	public void delete() {

		try {
			// Set WerkOpdrachten inRonde flag to false
			for (WerkOpdracht opdracht : getWerkOpdrachtenInUitvoeringsronde(object)) {
				opdracht.setInRonde("0");
			}
			// Delete Uitvoeringsronde
			modelRepository.deleteObject(object);
			clear();
			search();
		} catch (IOException e) {
			LOG.error("Can not delete model object.", e);
		}
	}
}
