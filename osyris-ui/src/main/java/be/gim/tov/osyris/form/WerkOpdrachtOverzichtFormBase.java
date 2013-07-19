package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.jsf.component.ComponentUtils;
import org.quartz.xml.ValidationException;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.GebruiktMateriaal;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.UitvoeringsrondeStatus;
import be.gim.tov.osyris.model.werk.status.ValidatieStatus;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class WerkOpdrachtOverzichtFormBase extends
		AbstractListForm<WerkOpdracht> {
	private static final long serialVersionUID = -7478667205313972513L;

	private static final String GEOMETRY_LAYER_NAME = "geometry";

	private static final Log LOG = LogFactory
			.getLog(WerkOpdrachtOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected MapFactory mapFactory;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;

	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected Date vanDatum;
	protected Date totDatum;
	protected String gemeente;
	protected WerkOpdracht[] selectedOpdrachten;
	protected ValidatieStatus validatieStatus;

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

	public Date getVanDatum() {
		return vanDatum;
	}

	public void setVanDatum(Date vanDatum) {
		this.vanDatum = vanDatum;
	}

	public Date getTotDatum() {
		return totDatum;
	}

	public void setTotDatum(Date totDatum) {
		this.totDatum = totDatum;
	}

	public String getGemeente() {
		return gemeente;
	}

	public void setGemeente(String gemeente) {
		this.gemeente = gemeente;
	}

	public WerkOpdracht[] getSelectedOpdrachten() {
		return selectedOpdrachten;
	}

	public void setSelectedOpdrachten(WerkOpdracht[] selectedOpdrachten) {
		this.selectedOpdrachten = selectedOpdrachten;
	}

	public ValidatieStatus getValidatieStatus() {
		return validatieStatus;
	}

	public void setValidatieStatus(ValidatieStatus validatieStatus) {
		this.validatieStatus = validatieStatus;
	}

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "WerkOpdracht";
	}

	@Override
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(getName());
	}

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
	}

	public boolean isBordProbleem(Probleem probleem) {
		if (probleem instanceof BordProbleem) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Query getQuery() {

		if (query == null) {
			query = getDefaultQuery();
		}

		try {
			if (identity.inGroup("Uitvoerder", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("uitvoerder", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
				query.addFilter(FilterUtils.equal("status",
						WerkopdrachtStatus.UIT_TE_VOEREN));
				return query;
			}

			if (identity.inGroup("Medewerker", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
				return query;
			}

		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}

		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void search() {
		try {
			List<WerkOpdracht> list = (List<WerkOpdracht>) modelRepository
					.searchObjects(getQuery(), true, true, true);
			List<WerkOpdracht> filteredList = new ArrayList<WerkOpdracht>();

			// Filteren transient properties. Kan dit op een betere manier
			// gebeuren?
			if (gemeente != null) {
				for (WerkOpdracht opdracht : list) {
					if (opdracht.getGemeente().equals(gemeente)) {
						filteredList.add(opdracht);
					}
				}
				results = filteredList;
			}

			if (vanDatum != null && totDatum != null) {
				if (filteredList.isEmpty()) {
					results = findWerkOpdrachtenBetweenDates(list);
				} else {
					results = findWerkOpdrachtenBetweenDates(filteredList);
				}
			}

			if (gemeente == null && vanDatum == null && totDatum == null) {
				results = list;
			}
		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			results = null;
		}
	}

	/**
	 * Zoekt WerkOpdrachten tussen een begin en een einddatum.
	 * 
	 * @param opdrachten
	 * @return
	 */
	public List<WerkOpdracht> findWerkOpdrachtenBetweenDates(
			List<WerkOpdracht> opdrachten) {
		List<WerkOpdracht> result = new ArrayList<WerkOpdracht>();
		for (WerkOpdracht opdracht : opdrachten) {
			if (opdracht.getDatumLaatsteWijziging().before(totDatum)
					&& opdracht.getDatumLaatsteWijziging().after(vanDatum)) {
				result.add(opdracht);
			}
		}
		return result;
	}

	/**
	 * Map Configuratie
	 * 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public MapConfiguration getConfiguration() throws IOException,
			InstantiationException, IllegalAccessException {

		MapContext context = (MapContext) modelRepository
				.loadObject(new ResourceKey("Form@12"));

		if (context != null) {
			MapConfiguration configuration = mapFactory
					.getConfiguration(context);

			mapFactory.createGeometryLayer(configuration.getContext(),
					GEOMETRY_LAYER_NAME, null, Point.class, null, true,
					"single", null, null);

			// Reset context
			configuration.setContext(context);

			// Reset layers
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.setFilter(null);
				layer.setHidden(true);
				layer.setSelection(Collections.EMPTY_LIST);
			}

			List<String> bordSelection = new ArrayList<String>();
			List<Geometry> anderProbleemGeoms = new ArrayList<Geometry>();

			GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) mapFactory
					.createGeometryLayer(configuration.getContext(),
							"geometry", null, Point.class, null, true,
							"single", null, null);

			// Get traject
			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			// Load layer depending on traject type
			FeatureMapLayer trajectLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(traject.getModelClass()
							.getName()));

			if (trajectLayer != null) {
				trajectLayer.setHidden(false);
				trajectLayer.setFilter(FilterUtils.equal("naam",
						traject.getNaam()));

				Envelope envelope = GeometryUtils
						.getEnvelope(traject.getGeom());
				GeometryUtils.expandEnvelope(envelope, 0.1,
						context.getMaxBoundingBox());
				context.setBoundingBox(envelope);
			}

			// BordLayer
			FeatureMapLayer bordLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(LabelUtils
							.lowerCamelCase(traject.getModelClass().getName()
									+ "Bord")));

			if (bordLayer != null) {
				bordLayer.setHidden(false);
				bordLayer
						.setFilter(FilterUtils.equal("naam", traject.getNaam()));
			}
			// Probleem
			if (object.getProbleem() instanceof BordProbleem) {
				// Bord Probleem
				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) object.getProbleem())
								.getBord());
				bordSelection.add(bord.getId().toString());

			} else if (object.getProbleem() instanceof AnderProbleem) {
				// Ander Probleem
				AnderProbleem anderProbleem = (AnderProbleem) object
						.getProbleem();
				anderProbleemGeoms.add(anderProbleem.getGeom());
			}

			bordLayer.setSelection(bordSelection);
			geomLayer.setGeometries(anderProbleemGeoms);

			if (!anderProbleemGeoms.isEmpty()) {
				geomLayer.setHidden(false);
			}
			return configuration;
		}
		return null;
	}

	/**
	 * Verzenden van een werkopdracht
	 * 
	 * @throws ValidationException
	 * 
	 */
	public void verzendenWerkOpdracht() {
		if (object != null) {
			if (object.getHandelingen().isEmpty()
					|| object.getHandelingen() == null) {
				messages.error("Gelieve minstens 1 handeling toe te voegen alvorens de werkopdracht te verzenden.");
			} else {
				object.setStatus(WerkopdrachtStatus.UIT_TE_VOEREN);
				object.setDatumUitTeVoeren(new Date());
				try {
					modelRepository.saveObject(object);
					clear();
					search();
					// send confirmatie mail naar Uitvoerder
					// sendConfirmationMail();
					messages.info("Werkopdracht succesvol verzonden.");
				} catch (IOException e) {
					messages.error("Fout bij het verzenden van werkopdracht: "
							+ e.getMessage());
					LOG.error("Can not save object.", e);
				} catch (Exception e) {
					messages.error("Fout bij het verzenden van werkopdracht: "
							+ e.getMessage());
					LOG.error("Can not send email", e);
				}
			}
		}
	}

	/**
	 * Annuleren van een werkopdracht
	 * 
	 */
	public void annuleerWerkOpdracht() {
		if (object != null) {
			object.setStatus(WerkopdrachtStatus.GEANNULEERD);
			object.setDatumGeannuleerd(new Date());
			try {
				modelRepository.saveObject(object);
				messages.info("Werkopdracht succesvol geannuleerd.");
				clear();
				search();
			} catch (IOException e) {
				messages.error("Fout bij het annuleren van werkopdracht: "
						+ e.getMessage());
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Heropenen geannuleerde werkOpdracht
	 * 
	 */
	public void reopenWerkOpdracht() {
		if (object != null) {
			object.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
			object.setDatumTeControleren(new Date());
			try {
				modelRepository.saveObject(object);
				clear();
				search();
				messages.info("Werkopdracht succesvol heropend: de werkopdracht heeft opnieuw status 'Te controleren'.");
			} catch (IOException e) {
				messages.error("Fout bij het heropenen van werkopdracht: "
						+ e.getMessage());
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Zoomen naar een probleem op de kaart
	 * 
	 * @param bordProbleem
	 */
	public void zoomToProbleem(Probleem probleem) {
		MapViewer viewer = getViewer();
		Envelope envelope;

		try {
			if (probleem instanceof BordProbleem) {
				BordProbleem bordProbleem = (BordProbleem) probleem;
				envelope = new Envelope(
						((Bord) modelRepository.loadObject(bordProbleem
								.getBord())).getGeom().getCoordinate());
				viewer.updateCurrentExtent(envelope);
			}

			else if (probleem instanceof AnderProbleem) {
				AnderProbleem anderProbleem = (AnderProbleem) probleem;
				GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) viewer
						.getConfiguration().getContext()
						.getLayer(GEOMETRY_LAYER_NAME);
				layer.setHidden(false);
				if (anderProbleem.getGeom() instanceof Point) {
					envelope = new Envelope(anderProbleem.getGeom()
							.getCoordinate());
					viewer.updateCurrentExtent(envelope);

				}
			}
		} catch (IOException e) {
			LOG.error("Can not load bord.", e);
		}
	}

	/**
	 * Aanmaken van een uitvoeringsronde met de geselecteerde werkopdrachten.
	 * 
	 */
	public void createUitvoeringsronde() {
		try {
			Uitvoeringsronde ronde = (Uitvoeringsronde) modelRepository
					.createObject("Uitvoeringsronde", null);
			ronde.setStatus(UitvoeringsrondeStatus.AANGEMAAKT);

			// Minstens 1 werkopdracht moet geselecteerd zijn
			if (Arrays.asList(selectedOpdrachten).size() < 1) {
				throw new IOException();
			}
			// Set opdrachten
			List<ResourceIdentifier> ids = new ArrayList<ResourceIdentifier>();
			for (WerkOpdracht werkOpdracht : Arrays.asList(selectedOpdrachten)) {
				ids.add(modelRepository.getResourceIdentifier(werkOpdracht));
			}

			ronde.setOpdrachten(ids);
			for (WerkOpdracht werkOpdracht : Arrays.asList(selectedOpdrachten)) {
				// Opdrachten mogen nog niet aan een ronde toegewezen zijn
				if (werkOpdracht.getInRonde().equals("1")) {
					throw new IOException();
				}
				// Set opdrachten flagged inRonde true
				werkOpdracht.setInRonde("1");
				ronde.setUitvoerder(werkOpdracht.getUitvoerder());
				modelRepository.saveObject(ronde);
				modelRepository.saveObject(werkOpdracht);
			}
			selectedOpdrachten = null;
			messages.info("Uitvoeringsronde succesvol aangemaakt.");
		} catch (InstantiationException e) {
			messages.error("Fout bij het aanmaken van uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			messages.error("Fout bij het aanmaken van uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Illegal access at creation model object.", e);
		} catch (IOException e) {
			messages.error("Gelieve minstens 1 werkopdracht te selecteren die nog niet aan een ronde is toegevoegd.");
			LOG.error("Can not save Uitvoeringsronde.", e);
		}
	}

	@Override
	public void save() {
		try {
			modelRepository.saveObject(object);
			messages.info("Werkopdracht succesvol bewaard.");
			clear();
			search();
		} catch (IOException e) {
			messages.error("Fout bij het bewaren van werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not save model object.", e);
		}
	}

	/**
	 * Bepaalt of een groep rijen in de dataTable kan selecteren.
	 * 
	 * @return
	 */
	public boolean canSelectRows() {
		if (identity.inGroup("Uitvoerder", "CUSTOM")
				|| identity.inGroup("admin", "CUSTOM")) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete() {
		try {
			// Check of probleem al bestaat in andere opdracht
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName(getModelClass().getName());
			query.setFilter(FilterUtils.equal("probleem", object.getProbleem()));
			List<WerkOpdracht> result = (List<WerkOpdracht>) modelRepository
					.searchObjects(query, true, true, true);

			// Indien nee delete probleem en WerkOpdracht
			if (result == null || result.isEmpty()) {
				modelRepository.deleteObject(object.getProbleem());
				modelRepository.deleteObject(object);
			} else {
				// Indien ja
				// Set probleem null en delete WerkOpdracht
				object.setProbleem(null);
				modelRepository.saveObject(object);
				modelRepository.deleteObject(object);
			}
			messages.info("Werkopdracht succesvol bewaard.");
			clear();
			search();
		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not delete model object.", e);
		}
	}

	/**
	 * Zoeken uitvoeringsronde.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Uitvoeringsronde getUitvoeringsronde() {
		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Uitvoeringsronde");
			query.setFilter(FilterUtils.equal("opdrachten",
					modelRepository.getResourceIdentifier(object)));
			List<Uitvoeringsronde> result = (List<Uitvoeringsronde>) modelRepository
					.searchObjects(query, true, true, true);
			return (Uitvoeringsronde) modelRepository.getUniqueResult(result);
		} catch (IOException e) {
			LOG.error("Can not search Uitvoeringsronde.", e);
		}
		return null;
	}

	/**
	 * Valideren van een WerkOpdracht. Bij het valideren van een WerkOpdracht
	 * wordt ook het StockMateriaal afgeboekt.
	 * 
	 */
	public void valideerWerkOpdracht() {
		try {
			// Set status en datum GEVALIDEERD
			object.setStatus(WerkopdrachtStatus.GEVALIDEERD);
			object.setDatumGevalideerd(new Date());

			// Afboeken stock voor elk gebruikt materiaal
			if (!object.getMaterialen().isEmpty()
					|| object.getMaterialen() != null) {

				for (GebruiktMateriaal materiaal : object.getMaterialen()) {
					int inStockUpdated = materiaal.getStockMateriaal()
							.getInStock() - materiaal.getAantal();
					materiaal.getStockMateriaal().setInStock(inStockUpdated);
					// Save
					modelRepository.saveObject(materiaal.getStockMateriaal());
				}
			}

			modelRepository.saveObject(object);
			messages.info("Werkopdracht succesvol gevalideerd.");

			// OPNIEUW UIT TE VOEREN = nieuwe WerkOpdracht
			if (validatieStatus.equals(ValidatieStatus.OPNIEUW_UITVOEREN)) {
				WerkOpdracht opdracht = (WerkOpdracht) modelRepository
						.createObject(getModelClass().getName(), null);
				opdracht.setMedewerker(object.getMedewerker());
				opdracht.setProbleem(object.getProbleem());
				opdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
				opdracht.setTraject(object.getTraject());
				opdracht.setUitvoerder(object.getUitvoerder());
				opdracht.setInRonde("0");
				opdracht.setDatumTeControleren(new Date());
				opdracht.setValidatie(validatieStatus);
				modelRepository.saveObject(opdracht);
				messages.info("Er is een nieuwe Werkopdracht aangemaakt met status 'Te controleren'");

			}
			// LATER OPNIEUW UITVOEREN VANAF = status UITGESTELD en nieuwe
			// werkOpdracht met status UITGESTELD en set datum laterUitTeVoeren
			else if (validatieStatus
					.equals(ValidatieStatus.LATER_OPNIEUW_UITVOEREN_VANAF)) {

				WerkOpdracht opdracht = (WerkOpdracht) modelRepository
						.createObject(getModelClass().getName(), null);
				opdracht.setMedewerker(object.getMedewerker());
				opdracht.setProbleem(object.getProbleem());
				opdracht.setStatus(WerkopdrachtStatus.UITGESTELD);
				opdracht.setTraject(object.getTraject());
				opdracht.setUitvoerder(object.getUitvoerder());
				opdracht.setInRonde("0");
				opdracht.setDatumLaterUitTeVoeren(new Date());
				opdracht.setValidatie(validatieStatus);
				modelRepository.saveObject(opdracht);
				messages.info("Er is een nieuwe Werkopdracht aangemaakt met status 'Uitgesteld.' De werkopdracht zal status 'Te controleren' verkrijgen op de gespecifieerde datum.");
			}
			// TODO: Uitgestelde WO automatisch op te controlen op
			// laterUitTeVoerenDatum
			search();
		} catch (IOException e) {
			messages.error("Fout bij het valideren van werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not save WerkOpdracht.", e);
		} catch (InstantiationException e) {
			messages.error("Fout bij het valideren van werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not instantiate WerkOpdracht.", e);
		} catch (IllegalAccessException e) {
			messages.error("Fout bij het valideren van werkopdracht: "
					+ e.getMessage());
			LOG.error("illegal access at WerkOpdracht.", e);
		}
	}
}
