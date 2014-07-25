package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.search.DefaultQuery;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.user.UitvoerderBedrijf;
import be.gim.tov.osyris.model.user.UitvoerderProfiel;
import be.gim.tov.osyris.model.werk.GebruiktMateriaal;
import be.gim.tov.osyris.model.werk.StockMateriaal;
import be.gim.tov.osyris.model.werk.WerkOpdracht;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class StockMateriaalOverzichtFormBase extends
		AbstractListForm<StockMateriaal> implements Serializable {
	private static final long serialVersionUID = -2212073755366628143L;

	private static final Log LOG = LogFactory
			.getLog(StockMateriaalOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private UserRepository userRepository;

	protected int aantalGebruikt;
	protected Integer hoeveelheid;
	protected String keuzeAanpassing;

	// GETTERS AND SETTERS
	public int getAantalGebruikt() {
		return aantalGebruikt;
	}

	public void setAantalGebruikt(int aantalGebruikt) {
		this.aantalGebruikt = aantalGebruikt;
	}

	public Integer getHoeveelheid() {
		return hoeveelheid;
	}

	public void setHoeveelheid(Integer hoeveelheid) {
		this.hoeveelheid = hoeveelheid;
	}

	public String getKeuzeAanpassing() {
		return keuzeAanpassing;
	}

	public void setKeuzeAanpassing(String keuzeAanpassing) {
		this.keuzeAanpassing = keuzeAanpassing;
	}

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
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		try {
			UitvoerderProfiel profiel = (UitvoerderProfiel) modelRepository
					.loadAspect(modelRepository
							.getModelClass("UitvoerderProfiel"),
							modelRepository.getResourceKey(userRepository
									.loadUser(identity.getUser().getId())));

			if (identity.inGroup("Uitvoerder", "CUSTOM")
					&& profiel.getBedrijf() != null) {
				UitvoerderBedrijf bedrijf = (UitvoerderBedrijf) modelRepository
						.loadObject(profiel.getBedrijf());
				query.addFilter(FilterUtils.equal("magazijn", bedrijf.getNaam()));
			}
		} catch (IOException e) {
			LOG.error("Can not load aspect.", e);
		}

		return query;
	}

	/**
	 * Aanmaken GebruiktMateriaal bij het rapporteren van een WerkOpdracht.
	 * 
	 * @param opdracht
	 */
	public void createGebruiktMateriaal(WerkOpdracht opdracht) {
		try {

			// Aanmaken gebruikt materiaal en toevoegen aan Werkopdracht
			GebruiktMateriaal gebruiktMateriaal = (GebruiktMateriaal) modelRepository
					.createObject("GebruiktMateriaal", null);
			gebruiktMateriaal.setStockMateriaal(object);
			gebruiktMateriaal.setAantal(aantalGebruikt);

			// GebruiktMateriaal toevoegen en saven indien niet 0
			if (aantalGebruikt != 0) {
				opdracht.getMaterialen().add(gebruiktMateriaal);
			}

			// reset aantal gebruikt
			setAantalGebruikt(0);
			search();
		} catch (InstantiationException e) {
			messages.error("Fout bij het toevoegen van stockmateriaal: "
					+ e.getMessage());
			LOG.error("Can not instantiate GebruiktMateriaal.", e);
		} catch (IllegalAccessException e) {
			messages.error("Fout bij het toevoegen van stockmateriaal: "
					+ e.getMessage());
			LOG.error("Illegal access at object.", e);
		}
	}

	@Override
	public void save() {
		try {
			if (keuzeAanpassing != null) {
				if (keuzeAanpassing.equals("plus")) {
					object.setInStock(object.getInStock() + hoeveelheid);
				} else if (keuzeAanpassing.equals("min")) {
					object.setInStock(object.getInStock() - hoeveelheid);
				} else if (keuzeAanpassing.equals("direct")) {
					object.setInStock(hoeveelheid);
				}
			}
			modelRepository.saveObject(object);
			messages.info("Stockmateriaal succesvol bewaard.");
			clear();
			search();
		} catch (IOException e) {
			messages.error("Fout bij het bewaren van stockmateriaal: "
					+ e.getMessage());
			LOG.error("Can not save model object.", e);
		}
	}

	@Override
	public void clear() {
		object = null;
		setHoeveelheid(null);
	}
}
