package be.gim.tov.osyris.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SortOrder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.search.QueryOrderBy;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.resource.FileResource;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.jsf.component.ComponentUtils;
import org.quartz.xml.ValidationException;
import org.w3c.dom.Document;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.peritia.io.content.Content;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.RouteBordProbleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.GebruiktMateriaal;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.UitvoeringsrondeStatus;
import be.gim.tov.osyris.model.werk.status.ValidatieStatus;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;
import be.gim.tov.osyris.pdf.XmlBuilder;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class WerkOpdrachtOverzichtFormBase extends
		AbstractListForm<WerkOpdracht> implements Serializable {

	private static final long serialVersionUID = -7478667205313972513L;

	private static final Log LOG = LogFactory
			.getLog(WerkOpdrachtOverzichtFormBase.class);

	public static final String GEOMETRY_LAYER_NAME = "geometry";
	public static final String GEOMETRY_LAYER_LINE_NAME = "geometryLine";

	public static final String WO_PDF = "/META-INF/resources/osyris/xslts/werkOpdrachtPdf.xsl";

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
	protected ResourceIdentifier trajectId;

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

	public ResourceIdentifier getTrajectId() {
		return trajectId;
	}

	public void setTrajectId(ResourceIdentifier trajectId) {
		this.trajectId = trajectId;
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

	/**
	 * Bepalen of het probleem bij de WerkOpdracht een bordProbleem is.
	 * 
	 * @param probleem
	 * @return
	 */
	public boolean isBordProbleem(Probleem probleem) {
		if (probleem instanceof BordProbleem) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Bepalen of het probleem bij de WerkOpdracht een RouteBordProbleem is.
	 * 
	 * @param probleem
	 * @return
	 */
	public boolean isRouteBordProbleem(Probleem probleem) {
		if (probleem instanceof RouteBordProbleem) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		// Filteren meest recente datum
		if (vanDatum != null && totDatum != null) {
			query.addFilter(FilterUtils.and(FilterUtils.lessOrEqual(
					"datumLaatsteWijziging", totDatum), FilterUtils
					.greaterOrEqual("datumLaatsteWijziging", vanDatum)));
		}

		if (gemeente != null) {
			query.addFilter(FilterUtils.equal("gemeente", gemeente));
		}

		if (trajectType != null && trajectId != null) {
			query.addFilter(FilterUtils.equal("traject", trajectId));
		} else {

			if (trajectType != null) {
				query.addFilter(FilterUtils.equal("trajectType", trajectType));
			}

			if (regio != null) {
				query.addFilter(FilterUtils.equal("regioId", regio));
			}
		}

		try {
			if (identity.inGroup("Routedokter", "CUSTOM")) {
				return query;
			} else if (identity.inGroup("Uitvoerder", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("uitvoerder", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
				query.addFilter(FilterUtils.equal("status",
						WerkopdrachtStatus.UIT_TE_VOEREN));
			} else if (identity.inGroup("Medewerker", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}

		DefaultQueryOrderBy orderBy = new DefaultQueryOrderBy(
				FilterUtils.property("datumLaatsteWijziging"));
		orderBy.setSortOrder(SortOrder.DESCENDING);

		query.setOrderBy(Collections.singletonList((QueryOrderBy) orderBy));

		return query;
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

			// Reset context
			configuration.setContext(context);

			// Reset layers
			resetLayers(context);

			// Ophalen traject
			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			// ROUTES EN LUSSEN
			if (traject instanceof Route || traject instanceof NetwerkLus) {

				processRouteOrNetwerkLus(traject, configuration, context);
				return configuration;
			}

			// NETWERK SEGMENTEN
			else if (traject instanceof NetwerkSegment) {

				processNetwerkSegment(traject, configuration, context);
				return configuration;
			}
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
				object.setDatumLaatsteWijziging(new Date());

				try {

					modelRepository.saveObject(object);

					// Send confirmatie mail naar Uitvoerder
					String mailServiceStatus = DefaultConfiguration.instance()
							.getString("service.mail.werkOpdracht");

					if (mailServiceStatus.equalsIgnoreCase("on")) {
						sendConfirmationMail();
					}

					clear();
					search();
					messages.info("Werkopdracht succesvol verzonden.");

				} catch (IOException e) {
					messages.error("Fout bij het verzenden van werkopdracht: "
							+ e.getMessage());
					LOG.error("Can not save object.", e);

				} catch (Exception e) {
					messages.error("Fout bij het versturen van email: "
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
			object.setDatumLaatsteWijziging(new Date());

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
			object.setDatumLaatsteWijziging(new Date());

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
			messages.error("Gelieve minstens 1 werkopdracht aan te vinken die nog niet aan een ronde is toegevoegd.");
			LOG.error("Can not save Uitvoeringsronde.", e);

		}
	}

	@Override
	public void save() {
		try {
			modelRepository.saveObject(object);
			messages.info("Werkopdracht succesvol bewaard.");

			// Stuur mail naar uitvoerder indien werkopdracht gewijzigd door TOV
			// en in status uit te voeren
			if (object.getStatus().equals(WerkopdrachtStatus.UIT_TE_VOEREN)
					&& (identity.inGroup("admin", "CUSTOM")
							|| identity.inGroup("Medewerker", "CUSTOM") || identity
								.inGroup("Routedokter", "CUSTOM"))) {

				String mailServiceStatus = DefaultConfiguration.instance()
						.getString("service.mail.werkOpdracht");

				if (mailServiceStatus.equalsIgnoreCase("on")) {
					sendWerkOpdrachtEditedMail();
				}
			}
			// clear();
			search();

		} catch (IOException e) {
			messages.error("Fout bij het bewaren van werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not save model object.", e);

		} catch (Exception e) {
			messages.error("Fout bij het versturen van email: "
					+ e.getMessage());
			LOG.error("Can not send email", e);
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
				object.setHandelingen(null);
				object.setMaterialen(null);
				modelRepository.saveObject(object);
				modelRepository.deleteObject(object);
			}
			messages.info("Werkopdracht succesvol verwijderd.");
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
			object.setValidatie(validatieStatus);
			object.setDatumLaatsteWijziging(new Date());

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
				opdracht.setTrajectType(object.getTrajectType());
				opdracht.setRegioId(object.getRegioId());
				opdracht.setDatumLaatsteWijziging(new Date());

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
				opdracht.setTrajectType(object.getTrajectType());
				opdracht.setRegioId(object.getRegioId());
				opdracht.setDatumLaatsteWijziging(new Date());

				modelRepository.saveObject(opdracht);
				messages.info("Er is een nieuwe Werkopdracht aangemaakt met status 'Uitgesteld.' De werkopdracht zal status 'Te controleren' verkrijgen op de gespecifieerde datum.");
			}
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

	/**
	 * Opzoeken van knooppunten voor lussen
	 * 
	 * @param layer
	 */
	private void searchKnooppuntLayer(FeatureMapLayer layer) {
		layer.setHidden(false);
		layer.setFilter(null);
		layer.setSelection(Collections.EMPTY_LIST);

		try {

			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			Set<Long> knooppuntFilterIds = new HashSet<Long>();

			// KNOOPPUNTEN VOOR LUSSEN
			if (traject instanceof NetwerkLus) {

				NetwerkLus lus = (NetwerkLus) modelRepository.loadObject(object
						.getTraject());

				for (ResourceIdentifier segment : lus.getSegmenten()) {
					NetwerkSegment seg = (NetwerkSegment) modelRepository
							.loadObject(segment);

					NetwerkKnooppunt vanKp = (NetwerkKnooppunt) modelRepository
							.loadObject(seg.getVanKnooppunt());

					NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
							.loadObject(seg.getNaarKnooppunt());

					knooppuntFilterIds.add(vanKp.getId());
					knooppuntFilterIds.add(naarKp.getId());
				}
				layer.setFilter(FilterUtils.in("id", knooppuntFilterIds));
			}

			// KNOOPPUNTEN VOOR SEGMENTEN
			else if (traject instanceof NetwerkSegment) {

				NetwerkSegment seg = (NetwerkSegment) traject;

				NetwerkKnooppunt vanKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getVanKnooppunt());

				NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getNaarKnooppunt());

				knooppuntFilterIds.add(vanKp.getId());
				knooppuntFilterIds.add(naarKp.getId());

				layer.setFilter(FilterUtils.in("id", knooppuntFilterIds));
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * Reset alle lagen in de mapConfiguratie en toon enkel de provinciegrens.
	 * 
	 * @param context
	 */
	private void resetLayers(MapContext context) {

		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			layer.setFilter(null);
			layer.setHidden(true);
			layer.setSelection(Collections.EMPTY_LIST);

			// Provincie altijd zichtbaar
			if (layer.getLayerId().equalsIgnoreCase("provincie")) {
				layer.setHidden(false);
			}
		}
	}

	/**
	 * Ophalen Envelope van een specififiek Bord.
	 * 
	 * @param bord
	 * @param context
	 * @return
	 */
	private Envelope getEnvelopeProbleem(Probleem probleem, MapContext context) {

		try {
			// BordProbleem
			if (probleem instanceof BordProbleem) {

				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) probleem).getBord());

				if (bord != null) {
					Envelope envelope = GeometryUtils.getEnvelope(bord
							.getGeom());
					GeometryUtils.expandEnvelope(envelope, 0.1,
							context.getMaxBoundingBox());
					return envelope;
				} else {
					// return envelope of segment of route
					Traject traject = (Traject) modelRepository
							.loadObject(object.getTraject());
					return GeometryUtils.getEnvelope(traject.getGeom());
				}

			}

			// AnderProbleem
			else if (probleem instanceof AnderProbleem) {
				Envelope envelope = GeometryUtils
						.getEnvelope(((AnderProbleem) probleem).getGeom());
				GeometryUtils.expandEnvelope(envelope, 0.1,
						context.getMaxBoundingBox());

				return envelope;
			}
		} catch (IOException e) {
			LOG.error("Can not load Bord.", e);
		}
		return null;
	}

	/**
	 * Verwerken lagen voor wat betreft WerkOpdrachten die behoren tot een Route
	 * of NetwerkLus.
	 * 
	 * @param traject
	 * @param configuration
	 * @param context
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void processRouteOrNetwerkLus(Traject traject,
			MapConfiguration configuration, MapContext context)
			throws IOException, InstantiationException, IllegalAccessException {

		List<String> bordSelection = new ArrayList<String>();
		List<Geometry> anderProbleemGeoms = new ArrayList<Geometry>();
		List<Geometry> anderProbleemLineGeoms = new ArrayList<Geometry>();
		FeatureMapLayer bordLayer = null;

		// Geometry laag voor punt probleem
		GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_NAME, null, Point.class, null, true,
						"single", null, null);

		// Geometrey laag voor probleem lijn
		GeometryListFeatureMapLayer geomLineLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_LINE_NAME, null, LineString.class, null,
						true, "single", null, null);

		// Ophalen laag afhankelijk van het trajectType
		FeatureMapLayer trajectLayer = (FeatureMapLayer) context
				.getLayer(LabelUtils.lowerCamelCase(traject.getModelClass()
						.getName()));

		if (trajectLayer != null) {

			trajectLayer.setHidden(false);
			trajectLayer
					.setFilter(FilterUtils.equal("naam", traject.getNaam()));
		}

		// BordLayer
		if (traject instanceof Route) {
			bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
					.lowerCamelCase(LabelUtils.lowerCamelCase(traject
							.getModelClass().getName() + "Bord")));

			bordLayer.setHidden(false);
			bordLayer.setFilter(FilterUtils.equal("naam", traject.getNaam()));

		} else if (traject instanceof NetwerkLus) {
			bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
					.lowerCamelCase(LabelUtils.lowerCamelCase(traject
							.getModelClass().getName().replace("Lus", "")
							+ "Bord")));

			bordLayer.setHidden(false);

			// Toon borden bij segmenten lus
			bordLayer.setFilter(FilterUtils.in("segmenten",
					((NetwerkLus) traject).getSegmenten()));

			// Toon eventueel knooppunten indien nodig
			FeatureMapLayer knooppuntLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(LabelUtils
							.lowerCamelCase(traject.getModelClass().getName()
									.replace("Lus", "Knooppunt"))));

			if (knooppuntLayer != null) {
				searchKnooppuntLayer(knooppuntLayer);
			}
		}

		// Probleem
		if (object.getProbleem() instanceof BordProbleem) {

			// Bord Probleem
			Bord bord = (Bord) modelRepository
					.loadObject(((BordProbleem) object.getProbleem()).getBord());

			bordSelection.add(bord.getId().toString());
			bordLayer.setSelection(bordSelection);
			context.setBoundingBox(getEnvelopeProbleem(object.getProbleem(),
					context));

		} else if (object.getProbleem() instanceof AnderProbleem) {

			// Ander Probleem
			AnderProbleem anderProbleem = (AnderProbleem) object.getProbleem();

			if (anderProbleem.getGeom() != null) {
				anderProbleemGeoms.add(anderProbleem.getGeom());
				geomLayer.setGeometries(anderProbleemGeoms);
				geomLayer.setHidden(false);
			}

			if (anderProbleem.getGeomOmleiding() != null) {
				anderProbleemLineGeoms.add(anderProbleem.getGeomOmleiding());
				geomLineLayer.setGeometries(anderProbleemLineGeoms);
				geomLineLayer.setHidden(false);
			}

			context.setBoundingBox(getEnvelopeProbleem(object.getProbleem(),
					context));

		}

		if (!anderProbleemGeoms.isEmpty()) {
			geomLayer.setHidden(false);
		}
	}

	/**
	 * Verwerken van lagen voor wat betreft WerkOpdrachten die behoren tot een
	 * NetwerkSegment.
	 * 
	 * @param traject
	 * @param configuration
	 * @param context
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	private void processNetwerkSegment(Traject traject,
			MapConfiguration configuration, MapContext context)
			throws InstantiationException, IllegalAccessException, IOException {

		List<String> bordSelection = new ArrayList<String>();
		List<Geometry> anderProbleemGeoms = new ArrayList<Geometry>();
		FeatureMapLayer bordLayer = null;

		// Geometry laag voor punt probleem
		GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_NAME, null, Point.class, null, true,
						"single", null, null);

		// Geometrey laag voor probleem lijn
		GeometryListFeatureMapLayer geomLineLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_LINE_NAME, null, LineString.class, null,
						true, "single", null, null);

		// Ophalen laag afhankelijk van het trajectType
		FeatureMapLayer trajectLayer = (FeatureMapLayer) context
				.getLayer(LabelUtils.lowerCamelCase(traject.getModelClass()
						.getName()));

		if (trajectLayer != null) {

			trajectLayer.setHidden(false);

			// Tonen knooppunten bij segment
			FeatureMapLayer knooppuntLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(LabelUtils
							.lowerCamelCase(traject.getModelClass().getName()
									.replace("Segment", "Knooppunt"))));

			if (knooppuntLayer != null) {
				searchKnooppuntLayer(knooppuntLayer);
			}

			// BordProbleem
			if (object.getProbleem() instanceof BordProbleem) {

				bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
						.lowerCamelCase(LabelUtils.lowerCamelCase(traject
								.getModelClass().getName()
								.replace("Segment", "")
								+ "Bord")));

				bordLayer.setHidden(false);
				bordLayer.setFilter(FilterUtils.equal("segmenten",
						modelRepository.getResourceIdentifier(traject)));

				// Bord Probleem
				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) object.getProbleem())
								.getBord());

				if (bord != null) {
					bordSelection.add(bord.getId().toString());
					bordLayer.setSelection(bordSelection);
				}
				context.setBoundingBox(getEnvelopeProbleem(
						object.getProbleem(), context));

			} else if (object.getProbleem() instanceof AnderProbleem) {

				// Ander Probleem
				AnderProbleem anderProbleem = (AnderProbleem) object
						.getProbleem();
				anderProbleemGeoms.add(anderProbleem.getGeom());

				if (anderProbleem.getGeom() instanceof Point) {
					geomLayer.setGeometries(anderProbleemGeoms);
					geomLayer.setHidden(false);
				}

				else if (anderProbleem.getGeom() instanceof LineString) {
					geomLineLayer.setGeometries(anderProbleemGeoms);
					geomLineLayer.setHidden(false);
				}
				context.setBoundingBox(getEnvelopeProbleem(
						object.getProbleem(), context));
			}

			if (!anderProbleemGeoms.isEmpty()) {
				geomLayer.setHidden(false);
			}
		}
	}

	/**
	 * Stuurt een mail naar de betrokken Uitvoerder dat er een nieuwe
	 * WerkOpdracht op status uit te voeren beschikbaar is
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	private void sendConfirmationMail() throws IOException, Exception {

		User medewerker = (User) modelRepository.loadObject(object
				.getMedewerker());

		UserProfile profiel = (UserProfile) medewerker.getAspect("UserProfile",
				modelRepository, true);

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);
		variables.put("id", object.get("id"));
		variables.put("typeTraject", object.getTrajectType());
		variables.put("gemeente", object.getGemeente());
		variables.put("regio", Beans.getReference(OsyrisModelFunctions.class)
				.getTrajectRegio(object.getTraject()));
		variables.put("medewerker",
				profiel.getLastName() + " " + profiel.getFirstName());

		User uitvoerder = (User) modelRepository.loadObject(object
				.getMedewerker());

		UserProfile uitvoerderProfiel = (UserProfile) uitvoerder.getAspect(
				"UserProfile", modelRepository, true);

		String uitvoerderEmail = uitvoerderProfiel.getEmail();

		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(uitvoerderEmail),
				"/META-INF/resources/core/mails/confirmWerkOpdracht.fmt",
				variables);

		messages.info("Er is een email verzonden naar de betrokken uitvoerder.");
	}

	/**
	 * Stuurt een mail naar de betrokken Uitvoerder dat er een nieuwe
	 * WerkOpdracht op status uit te voeren beschikbaar is
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	private void sendWerkOpdrachtEditedMail() throws IOException, Exception {

		User editor = (User) modelRepository.loadObject(modelRepository
				.getResourceName(userRepository.loadUser(identity.getUser()
						.getId())));

		UserProfile profiel = (UserProfile) editor.getAspect("UserProfile",
				modelRepository, true);

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);
		variables.put("id", object.get("id"));
		variables.put("editor",
				profiel.getLastName() + " " + profiel.getFirstName());

		User uitvoerder = (User) modelRepository.loadObject(object
				.getMedewerker());

		UserProfile uitvoerderProfiel = (UserProfile) uitvoerder.getAspect(
				"UserProfile", modelRepository, true);

		String uitvoerderEmail = uitvoerderProfiel.getEmail();

		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(uitvoerderEmail),
				"/META-INF/resources/core/mails/editedWerkOpdracht.fmt",
				variables);

		messages.info("Er is een email verzonden naar de betrokken uitvoerder.");
	}

	/**
	 * Printen van een PDF bestand met overzicht van de WerkOpdracht.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Content printWerkOpdracht() throws Exception {

		FileOutputStream out = null;
		File file = null;
		InputStream in = null;

		try {

			// Opbouwen XML
			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			Document doc = null;
			XmlBuilder xmlBuilder = new XmlBuilder();

			if (object.getProbleem() instanceof BordProbleem) {

				BordProbleem bordProbleem = (BordProbleem) object.getProbleem();
				Bord bord = (Bord) modelRepository.loadObject(bordProbleem
						.getBord());

				doc = xmlBuilder.buildWerkOpdrachtFicheBordProbleem(
						getViewer(), traject, object, bord);
			}

			else if (object.getProbleem() instanceof AnderProbleem) {

				doc = xmlBuilder.buildWerkOpdrachtFicheAnderProbleem(
						getViewer(), traject, object);
			}

			Random randomGenerator = new Random();
			Integer randomInt = randomGenerator.nextInt(1000000000);
			String fileName = "traject" + traject.getId().toString() + "_"
					+ randomInt.toString() + ".pdf";

			String location = DefaultConfiguration.instance().getString(
					"osyris.location.temp.pdf.wo.fiche");

			file = new File(location + fileName);
			out = new FileOutputStream(file);

			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF,
					FopFactory.newInstance().newFOUserAgent(), out);

			// xslt
			Source xslt = new StreamSource(getClass().getResourceAsStream(
					WO_PDF));
			in = getClass().getResourceAsStream(WO_PDF);

			// Transform source
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(xslt);
			Result res = new SAXResult(fop.getDefaultHandler());

			transformer.transform(new DOMSource(doc), res);

			String contentStr = IOUtils.toString(in, "UTF-8");

			// File aanmaken indien niet bestaand
			if (!file.exists()) {
				file.createNewFile();
			}
			out.write(contentStr.getBytes());
			contentStr = null;

			return new FileResource(file);
			// return new ByteArrayContent("application/pdf",
			// out.toByteArray());

		} finally {
			in.close();
			out.close();
		}
	}
}
