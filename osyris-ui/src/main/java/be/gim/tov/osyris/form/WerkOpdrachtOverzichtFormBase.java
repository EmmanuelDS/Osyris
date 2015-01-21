package be.gim.tov.osyris.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.search.QueryOrderBy;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.resource.FileResource;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.jsf.component.ComponentUtils;
import org.conscientia.jsf.event.ControllerEvent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.quartz.xml.ValidationException;
import org.w3c.dom.Document;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.api.Encoder;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.peritia.codec.EncodableContent;
import be.gim.peritia.io.content.Content;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.FeatureSelectionMode;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.api.context.RasterMapLayer;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.NetwerkAnderProbleem;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.RouteAnderProbleem;
import be.gim.tov.osyris.model.controle.RouteBordProbleem;
import be.gim.tov.osyris.model.controle.status.ProbleemStatus;
import be.gim.tov.osyris.model.encoder.WerkOpdrachtCSVModelEncoder;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Provincie;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.exceptions.AlreadyInRondeException;
import be.gim.tov.osyris.model.werk.exceptions.InStatusTeControlerenException;
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

	protected String probleemType;
	protected Integer knooppuntNummer;
	protected Envelope envelope = null;
	protected boolean hasErrors;
	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected ResourceIdentifier regioCreate;
	protected String trajectTypeCreate;
	protected String trajectNaamCreate;
	protected Date vanDatum;
	protected Date totDatum;
	protected String gemeente;
	protected WerkOpdracht[] selectedOpdrachten;
	protected ValidatieStatus validatieStatus;
	protected ResourceIdentifier trajectId;
	protected String baseLayerName;
	protected List<Bord> selectableBorden;
	protected List<String> bordSelection;
	protected List<Geometry> anderProbleemGeoms;
	protected List<Geometry> anderProbleemLineGeoms;

	// GETTERS AND SETTERS
	public WerkOpdracht getWerkOpdracht() {

		if (object == null) {
			object = createWerkOpdracht();
		}
		return object;
	}

	public String getProbleemType() {
		return probleemType;
	}

	public void setProbleemType(String probleemType) {
		this.probleemType = probleemType;
	}

	public Integer getKnooppuntNummer() {
		return knooppuntNummer;
	}

	public void setKnooppuntNummer(Integer knooppuntNummer) {
		this.knooppuntNummer = knooppuntNummer;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

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

	public ResourceIdentifier getRegioCreate() {
		return regioCreate;
	}

	public void setRegioCreate(ResourceIdentifier regioCreate) {
		this.regioCreate = regioCreate;
	}

	public String getTrajectTypeCreate() {
		return trajectTypeCreate;
	}

	public void setTrajectTypeCreate(String trajectTypeCreate) {
		this.trajectTypeCreate = trajectTypeCreate;
	}

	public String getTrajectNaamCreate() {
		return trajectNaamCreate;
	}

	public void setTrajectNaamCreate(String trajectNaamCreate) {
		this.trajectNaamCreate = trajectNaamCreate;
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

	public String getBaseLayerName() {
		return baseLayerName;
	}

	public void setBaseLayerName(String baseLayerName) {
		this.baseLayerName = baseLayerName;
	}

	public List<Bord> getSelectableBorden() {
		return selectableBorden;
	}

	public void setSelectableBorden(List<Bord> selectableBorden) {
		this.selectableBorden = selectableBorden;
	}

	public List<String> getBordSelection() {
		return bordSelection;
	}

	public void setBordSelection(List<String> bordSelection) {
		this.bordSelection = bordSelection;
	}

	public List<Geometry> getAnderProbleemGeoms() {
		return anderProbleemGeoms;
	}

	public void setAnderProbleemGeoms(List<Geometry> anderProbleemGeoms) {
		this.anderProbleemGeoms = anderProbleemGeoms;
	}

	public List<Geometry> getAnderProbleemLineGeoms() {
		return anderProbleemLineGeoms;
	}

	public void setAnderProbleemLineGeoms(List<Geometry> anderProbleemLineGeoms) {
		this.anderProbleemLineGeoms = anderProbleemLineGeoms;
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

				messages.warn("Gelieve minstens 1 werkhandeling toe te voegen alvorens deze werkopdracht naar de uitvoerder te verzenden.");
				setHasErrors(true);

			} else {

				setHasErrors(false);
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
					throw new AlreadyInRondeException();
				}

				// Opdracht mag niet in status Te Controleren staan
				if (werkOpdracht.getStatus().equals(
						WerkopdrachtStatus.TE_CONTROLEREN)) {
					throw new InStatusTeControlerenException();
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

		} catch (AlreadyInRondeException e) {
			messages.error("Gelieve minstens 1 werkopdracht aan te vinken die nog niet aan een ronde is toegevoegd.");
			LOG.error("Can not save Uitvoeringsronde.", e);

		} catch (InStatusTeControlerenException e) {
			messages.error("Er zijn geselecteerde opdrachten die zich in status 'Te Controleren' bevinden. Gelieve deze opdracht(en) eerst te controleren.");
			LOG.error("Can not save Uitvoeringsronde.", e);

		} catch (IOException e) {
			messages.error("Fout bij het aanmaken van uitvoeringsronde: "
					+ e.getMessage());
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

		setHasErrors(true);
		try {
			if (checkWerkOpdrachtValidatie()) {
				setHasErrors(false);

				// Set status en datum GEVALIDEERD
				object.setStatus(WerkopdrachtStatus.GEVALIDEERD);
				object.setDatumGevalideerd(new Date());
				object.setValidatie(validatieStatus);
				object.setDatumLaatsteWijziging(new Date());

				// Afboeken stock voor elk gebruikt materiaal
				// if (!object.getMaterialen().isEmpty()
				// || object.getMaterialen() != null) {

				// for (GebruiktMateriaal materiaal : object.getMaterialen()) {
				// int inStockUpdated = materiaal.getStockMateriaal()
				// .getInStock() - materiaal.getAantal();
				// materiaal.getStockMateriaal().setInStock(inStockUpdated);
				// // Save
				// modelRepository.saveObject(materiaal.getStockMateriaal());
				// }
				// }

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
					opdracht.setGemeente(object.getGemeente());

					modelRepository.saveObject(opdracht);
					messages.info("Er is een nieuwe Werkopdracht aangemaakt met status 'Te controleren'");

				}
				// LATER OPNIEUW UITVOEREN VANAF = status UITGESTELD en nieuwe
				// werkOpdracht met status UITGESTELD en set datum
				// laterUitTeVoeren
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
					opdracht.setDatumLaterUitTeVoeren(object
							.getDatumLaterUitTeVoeren());
					opdracht.setValidatie(validatieStatus);
					opdracht.setTrajectType(object.getTrajectType());
					opdracht.setRegioId(object.getRegioId());
					opdracht.setDatumLaatsteWijziging(new Date());
					opdracht.setGemeente(object.getGemeente());

					modelRepository.saveObject(opdracht);
					messages.info("Er is een nieuwe Werkopdracht aangemaakt met status 'Uitgesteld.' De werkopdracht zal status 'Te controleren' verkrijgen op de gespecifieerde datum.");
				}
				search();
			}

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

		// Ortho TMS als default achtergrondlaag
		for (RasterMapLayer baseLayer : context.getBaseRasterLayers()) {

			baseLayer.setHidden(true);

			if (baseLayer.getLayerId().equalsIgnoreCase("tms")) {
				baseLayer.setHidden(false);
				baseLayerName = baseLayer.getLayerId();
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
					GeometryUtils.expandEnvelope(envelope, 0.04,
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
				GeometryUtils.expandEnvelope(envelope, 0.04,
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

		// List<String> bordSelection = new ArrayList<String>();
		// List<Geometry> anderProbleemGeoms = new ArrayList<Geometry>();
		// List<Geometry> anderProbleemLineGeoms = new ArrayList<Geometry>();

		bordSelection = new ArrayList<String>();
		anderProbleemGeoms = new ArrayList<Geometry>();
		anderProbleemLineGeoms = new ArrayList<Geometry>();

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

			// In geval dat bord verwijderd is
			if (bord != null) {
				bordSelection.add(bord.getId().toString());
				bordLayer.setSelection(bordSelection);
			}
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

		// List<String> bordSelection = new ArrayList<String>();
		// List<Geometry> anderProbleemGeoms = new ArrayList<Geometry>();

		bordSelection = new ArrayList<String>();
		anderProbleemGeoms = new ArrayList<Geometry>();

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

			// Init BordLayer
			bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
					.lowerCamelCase(LabelUtils.lowerCamelCase(traject
							.getModelClass().getName().replace("Segment", "")
							+ "Bord")));

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

				// Toon borden bij segment
				bordLayer.setFilter(FilterUtils.equal("segmenten",
						modelRepository.getResourceIdentifier(traject)));
				bordLayer.setHidden(false);

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
		variables.put("commentaar", object.getCommentaarMedewerker());

		User uitvoerder = (User) modelRepository.loadObject(object
				.getUitvoerder());

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

		variables.put("commentaar", object.getCommentaarMedewerker());

		User uitvoerder = (User) modelRepository.loadObject(object
				.getUitvoerder());

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

	/**
	 * Switchen tussen basislagen
	 */
	public void switchBaseLayers() {
		getViewer().setBaseLayerId(baseLayerName);
	}

	/**
	 * Manueel aanmaken WerkOpdracht.
	 */
	public WerkOpdracht createWerkOpdracht() {

		try {
			object = (WerkOpdracht) modelRepository.createObject(
					modelRepository.getModelClass("WerkOpdracht"), null);

		} catch (InstantiationException e) {
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at creation model object.", e);
		}

		return object;
	}

	/**
	 * Doorzoekt de lagen van de kaartconfiguratie en voert de correcte
	 * kaartoperaties uit.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void searchTraject() throws IOException {

		if (trajectTypeCreate == null) {
			messages.warn("Gelieve eerst een route- of netwerktype te selecteren alvorens te zoeken.");
		}

		else if (trajectTypeCreate.contains("Route")
				&& trajectNaamCreate == null) {
			messages.warn("Gelieve bij het zoeken naar routes een trajectnaam te selecteren.");
		}

		else {
			MapViewer viewer = getViewer();
			MapContext context = viewer.getConfiguration().getContext();
			envelope = getEnvelopeProvincie();

			getWerkOpdracht().setProbleem(null);
			probleemType = "";

			// Itereren over de lagen en de correcte operaties uitvoeren
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.setFilter(null);
				layer.setHidden(true);
				layer.set("selectable", false);
				// Provincie altijd zichtbaar
				if (layer.getLayerId().equalsIgnoreCase("provincie")) {
					layer.setHidden(false);
				}

				else if (layer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
					// Netwerk
					if (trajectTypeCreate.contains("Segment")) {
						searchNetwerkLayer(layer);
					}
					// Route
					else {
						searchRouteLayer(layer);
						envelope = viewer.getContentExtent(layer);
					}
				}
				// RouteBord
				else if (layer.getLayerId().equalsIgnoreCase(
						trajectTypeCreate + "Bord")) {
					searchRouteBordLayer(layer);
				}
				// NetwerkBord
				// Filtering op de borden
				else if (layer.getLayerId().equalsIgnoreCase(
						trajectTypeCreate.replace("Segment", "") + "Bord")) {
					layer.setHidden(false);
					searchNetwerkBordLayer(layer);
				}
				// WandelKnooppunt
				else if (layer.getLayerId().contains("Knooppunt")
						&& trajectTypeCreate.contains("WandelNetwerk")) {
					FeatureMapLayer mapLayer = (FeatureMapLayer) context
							.getLayer("wandelNetwerkKnooppunt");
					searchKnooppuntLayer2(mapLayer);
				}

				// FietsKnooppunt
				else if (layer.getLayerId().contains("Knooppunt")
						&& trajectTypeCreate.contains("FietsNetwerk")) {
					FeatureMapLayer mapLayer = (FeatureMapLayer) context
							.getLayer("fietsNetwerkKnooppunt");
					searchKnooppuntLayer2(mapLayer);
				}

				// Intekenen Punt
				else if (layer.getLayerId().equalsIgnoreCase(
						GEOMETRY_LAYER_NAME)) {
					layer.setHidden(true);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(Collections.EMPTY_LIST);
				} else {
					layer.setHidden(true);
					layer.setFilter(null);
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}

			viewer.updateCurrentExtent(envelope);
			viewer.updateContext(null);
		}
	}

	/**
	 * Haalt de kaartconfiguratie van de kleine kaart op bij het manueel
	 * aanmaken van een WerkOpdracht.
	 * 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public MapConfiguration getCreateWerkOpdrachtConfiguration()
			throws IOException, InstantiationException, IllegalAccessException {

		MapContext context = (MapContext) modelRepository
				.loadObject(new ResourceKey("Form@12"));

		if (context != null) {
			MapConfiguration configuration = mapFactory
					.getConfiguration(context);

			// Retrieve context instance from configuration.
			context = configuration.getContext();

			// Reset context
			configuration.setContext(context);

			// Reset layers
			resetLayers(context);

			// Add edit layer to context
			mapFactory.createGeometryLayer(context, GEOMETRY_LAYER_NAME, null,
					Point.class, null, true, "single", null, null);

			// Start configuratie zoomt naar Provincie OVL
			FeatureMapLayer provincieLayer = (FeatureMapLayer) context
					.getLayer("provincie");
			provincieLayer.setHidden(false);
			Provincie provincie = (Provincie) modelRepository
					.getUniqueResult(modelRepository.searchObjects(
							new DefaultQuery("Provincie"), true, true));
			Envelope envelope = GeometryUtils.getEnvelope(provincie.getGeom());
			context.setBoundingBox(envelope);

			setBaseLayerName("tms");
			return configuration;
		}

		return null;
	}

	/**
	 * Reset zoekvelden trajectNaam en knooppuntNummer en koppel de medewerker
	 * aan de WerkOpdracht bij het wijzigen van het trajectType zoekveld.
	 */
	public void resetChildSearchParameters() {

		setKnooppuntNummer(null);
		trajectNaamCreate = null;

		object.setMedewerker(Beans.getReference(OsyrisModelFunctions.class)
				.zoekVerantwoordelijke(trajectTypeCreate));
	}

	/**
	 * Ophalen envelope voor provincie OVL.
	 * 
	 * @param viewer
	 * @return
	 */
	@SuppressWarnings("static-access")
	public Envelope getEnvelopeProvincie() {

		try {
			FeatureMapLayer provincieLayer = (FeatureMapLayer) getViewer()
					.getConfiguration().getContext().getLayer("provincie");
			provincieLayer.setHidden(false);
			Provincie provincie = (Provincie) modelRepository
					.getUniqueResult(modelRepository.searchObjects(
							new DefaultQuery("Provincie"), true, true));

			return GeometryUtils.getEnvelope(provincie.getGeom());

		} catch (IOException e) {
			LOG.error("Can not search Provincie.", e);
		}
		return null;
	}

	/**
	 * Operaties op de Routelagen.
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchRouteLayer(FeatureMapLayer layer) {

		layer.setHidden(false);

		if (regioCreate != null && trajectNaamCreate == null) {
			layer.setFilter(FilterUtils.equal("regio", regioCreate));
		} else if (trajectNaamCreate != null) {
			layer.setFilter(FilterUtils.like("naam", trajectNaamCreate));
		}
	}

	/**
	 * Operaties op de RouteBordlagen.
	 * 
	 * @param layer
	 * @param viewer
	 * @throws IOException
	 */
	private void searchRouteBordLayer(FeatureMapLayer layer) throws IOException {

		layer.setHidden(false);
		layer.setSelection(Collections.EMPTY_LIST);
		Filter filter = null;

		if (regioCreate != null && trajectNaamCreate != null) {
			filter = FilterUtils.and(FilterUtils.equal("regio", regioCreate),
					FilterUtils.like("naam", trajectNaamCreate));
		}

		else if (trajectNaamCreate != null) {
			filter = FilterUtils.like("naam", trajectNaamCreate);
		}

		else if (regioCreate != null) {
			filter = FilterUtils.equal("regio", regioCreate);

		}

		DefaultQuery query = new DefaultQuery("RouteBord");
		query.addFilter(filter);
		selectableBorden = (List<Bord>) modelRepository.searchObjects(query,
				false, false);

		layer.setFilter(filter);
		layer.set("selectionMode", FeatureSelectionMode.SINGLE);
	}

	/**
	 * Operaties op de NetwerkBord lagen.
	 * 
	 * @param layer
	 * @throws IOException
	 */
	private void searchNetwerkBordLayer(FeatureMapLayer layer)
			throws IOException {

		layer.setFilter(null);
		layer.setHidden(false);

		// Filter met de borden die verbonden zijn met het opgegeven
		// knooppuntNr
		Filter knooppuntFilter = FilterUtils.or(
				FilterUtils.equal("kpnr0", knooppuntNummer),
				FilterUtils.equal("kpnr1", knooppuntNummer),
				FilterUtils.equal("kpnr2", knooppuntNummer),
				FilterUtils.equal("kpnr3", knooppuntNummer));

		Filter filter = null;
		if (regioCreate != null) {
			if (knooppuntNummer == null) {
				filter = FilterUtils.and(FilterUtils
						.equal("regio", regioCreate));
			} else {
				filter = FilterUtils.and(
						FilterUtils.equal("regio", regioCreate),
						knooppuntFilter);
			}
		}
		if (trajectNaamCreate != null) {
			if (knooppuntNummer == null) {
				filter = FilterUtils.and(FilterUtils.equal("naam",
						trajectNaamCreate));
			} else {
				filter = FilterUtils.and(
						FilterUtils.equal("naam", trajectNaamCreate),
						knooppuntFilter);
			}
		}

		DefaultQuery query = new DefaultQuery("NetwerkBord");
		if (filter != null) {
			query.addFilter(filter);
		} else {
			query.addFilter(knooppuntFilter);
		}
		selectableBorden = (List<Bord>) modelRepository.searchObjects(query,
				false, false);

		layer.setFilter(filter);
	}

	/**
	 * Operaties op de knooppuntlagen.
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchKnooppuntLayer2(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.setFilter(null);
		layer.setSelection(Collections.EMPTY_LIST);
		Filter filter = null;

		if (knooppuntNummer == null) {
			if (regioCreate != null) {
				filter = FilterUtils.equal("regio", regioCreate);
				layer.setFilter(filter);
			}

			if (trajectNaamCreate != null) {
				filter = FilterUtils.equal("naam", trajectNaamCreate);
				layer.setFilter(filter);
			}
		}

		else if (knooppuntNummer != null) {
			if (regioCreate != null) {
				filter = FilterUtils.and(
						FilterUtils.equal("regio", regioCreate),
						(FilterUtils.equal("nummer", knooppuntNummer)));
				layer.setFilter(filter);
			}

			if (trajectNaamCreate != null) {
				filter = FilterUtils.and(
						FilterUtils.equal("naam", trajectNaamCreate),
						(FilterUtils.equal("nummer", knooppuntNummer)));
				layer.setFilter(filter);
			}

			if (trajectNaamCreate == null && regioCreate == null) {
				filter = FilterUtils.equal("nummer", knooppuntNummer);
				layer.setFilter(filter);
			}
			// Set Selectie enkel als specifiek knooppuntNr is opgegeven
			layer.set("selectable", true);
			List<String> ids = new ArrayList<String>();
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getViewer()
					.getFeature(layer, getViewer().getContext().getSrsName(),
							getViewer().getContext().getBoundingBox(), null,
							filter, null, null);
			FeatureIterator<SimpleFeature> iterator = features.features();

			envelope = getViewer().getFeatureExtent(layer, filter);

			while (iterator.hasNext()) {
				ids.add(iterator.next().getID());
			}
			layer.setSelection(ids);

			// Indien geen knooppunten gevonden toon provincie
			if (layer.getSelection().isEmpty()) {
				envelope = getEnvelopeProvincie();
			}
		}
	}

	/**
	 * Operaties op de Netwerklagen.
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchNetwerkLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.set("selectable", false);
		layer.setSelection(Collections.EMPTY_LIST);
		Filter filter = null;

		if (regioCreate != null && trajectNaamCreate == null
				&& knooppuntNummer == null) {

			filter = FilterUtils.equal("regio", regioCreate);
			envelope = getViewer().getFeatureExtent(layer, filter);

			List<String> ids = new ArrayList<String>();
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getViewer()
					.getFeature(layer, getViewer().getContext().getSrsName(),
							getViewer().getContext().getBoundingBox(), null,
							filter, null, null);
			FeatureIterator<SimpleFeature> iterator = features.features();

			while (iterator.hasNext()) {
				ids.add(iterator.next().getID());
			}

			layer.setSelection(ids);

			// Indien geen selectie gevonden toon provincie
			if (layer.getSelection().isEmpty()) {
				envelope = getEnvelopeProvincie();
			}
		}

		else if (trajectNaamCreate != null) {

			filter = FilterUtils.equal("naam", trajectNaamCreate);
			layer.set("selectable", true);

			if (knooppuntNummer == null) {
				List<String> ids = new ArrayList<String>();
				FeatureCollection<SimpleFeatureType, SimpleFeature> features = getViewer()
						.getFeature(layer,
								getViewer().getContext().getSrsName(),
								getViewer().getContext().getBoundingBox(),
								null, filter, null, null);
				FeatureIterator<SimpleFeature> iterator = features.features();
				while (iterator.hasNext()) {
					ids.add(iterator.next().getID());
				}
				layer.setSelection(ids);
				envelope = getViewer().getFeatureExtent(layer, filter);
			}
		}
		// Heel de provincie tonen indien geen filters ingesteld
		if (filter == null) {
			envelope = getEnvelopeProvincie();
		}
	}

	/**
	 * Bewaart een manueel aangemaakte WerkOpdracht in de databank.
	 * 
	 */
	public void saveWerkOpdracht() {

		try {
			setHasErrors(true);

			// Save en check WerkOpdracht
			if (checkWerkOpdracht(object)) {
				setHasErrors(false);
				modelRepository.saveObject(object);
				messages.info("Werkopdracht succesvol aangemaakt.");
				regioCreate = null;
				trajectTypeCreate = null;
				trajectNaamCreate = null;
				probleemType = null;
				object = null;
				search();
			}

		} catch (IOException e) {
			messages.error("Fout bij het aanmaken van werkopdracht.");
			LOG.error("Can not save model object.", e);
		}
	}

	/**
	 * Checkt of een manueel aangemaakte WerkOpdracht mag bewaard worden in de
	 * databank.
	 * 
	 * @param werkOpdracht
	 * @return
	 */
	public boolean checkWerkOpdracht(WerkOpdracht werkOpdracht) {

		if (probleemType == null || probleemType.isEmpty()) {
			messages.warn("Gelieve eerst een probleem aan de werkopdracht te koppelen.");
			return false;
		}
		if (object.getProbleem() instanceof AnderProbleem) {
			if (((AnderProbleem) object.getProbleem()).getGeom() == null) {
				messages.warn("Werkopdracht niet aangemaakt: gelieve eerst een punt aan te duiden op de kaart.");
				return false;
			}
			// Indien RouteAnderProbleem koppel trajectId aan de melding via de
			// routeNaam
			if (object.getProbleem() instanceof RouteAnderProbleem) {
				MapViewer viewer = getViewer();
				MapContext context = viewer.getConfiguration().getContext();

				for (FeatureMapLayer layer : context.getFeatureLayers()) {
					if (layer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
						layer.setFilter(FilterUtils.equal("naam",
								trajectNaamCreate));
						searchTrajectId(layer);
					}
				}
			}
			// Indien NetwerkAnderProbleem check of segment geselecteerd is
			if (object.getProbleem() instanceof NetwerkAnderProbleem) {
				if (object.getTraject() == null) {
					messages.warn("Werkopdracht niet aangemaakt: gelieve eerst een segment te selecteren.");
					return false;
				}
			}

		}
		// Indien BordProbleem check of bord geselecteerd is
		if (object.getProbleem() instanceof BordProbleem) {
			BordProbleem b = (BordProbleem) object.getProbleem();
			if (b.getBord() == null) {
				messages.warn("Werkopdracht niet aangemaakt: gelieve eerst een bord op de kaart te selecteren.");
				return false;
			}
		}
		return true;
	}

	/**
	 * Checkt of een manueel aangemaakte WerkOpdracht mag bewaard worden in de
	 * databank.
	 * 
	 * @param werkOpdracht
	 * @return
	 */
	public boolean checkWerkOpdrachtValidatie() {

		if (validatieStatus != null) {

			// Checken of datum LATER OPNIEUW UIT TE VOEREN is opgegeven en de
			// datum
			// in de toekomst ligt
			if (validatieStatus
					.equals(ValidatieStatus.LATER_OPNIEUW_UITVOEREN_VANAF)) {

				if (object.getDatumLaterUitTeVoeren() == null) {
					messages.warn("Gelieve een datum op te geven om de werkopdracht later opnieuw te kunnen uitvoeren. Deze datum moet in de toekomst liggen.");
					return false;
				}

				Calendar datumLaterUitTeVoeren = Calendar.getInstance();
				datumLaterUitTeVoeren
						.setTime(object.getDatumLaterUitTeVoeren());
				Calendar datumVandaag = Calendar.getInstance();
				datumVandaag.setTime(new Date());

				if (datumLaterUitTeVoeren.getTimeInMillis() < datumVandaag
						.getTimeInMillis()
						|| DateUtils.isSameDay(datumLaterUitTeVoeren,
								datumVandaag)) {
					messages.warn("De datum om de werkopdracht later opnieuw te kunnen uitvoeren moet in de toekomst liggen.");
					return false;
				}

			}
		} else {
			messages.warn("Gelieve eerst een validatiestatus te selecteren voor deze werkopdracht.");
			return false;
		}
		return true;
	}

	/**
	 * Event bij het selecteren van features op de kaart
	 * 
	 * @param event
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void onSelectFeatures(ControllerEvent event) throws IOException {

		// Selecteren bord
		if (object.getProbleem() != null
				&& object.getProbleem() instanceof BordProbleem) {

			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");

			String layerId = (String) event.getParams().get("layerId");
			FeatureMapLayer layer = (FeatureMapLayer) getViewer().getContext()
					.getLayer(layerId);

			// Workaround om te vermijden dat niet zichtbare borden geselecteerd
			// worden
			// Enkel de Borden behorende tot de bewegwijzering van het Traject
			// mogen geselecteerd
			// worden.
			List<String> idsFiltered = new ArrayList<String>();
			for (Bord b : selectableBorden) {
				if (ids.contains(b.getId().toString())) {
					idsFiltered.add(b.getId().toString());
				}
			}

			if (idsFiltered.isEmpty()) {
				layer.setSelection(new ArrayList<String>(1));
			}

			else if (idsFiltered.size() == 1) {
				layer.setSelection(idsFiltered);

				((BordProbleem) object.getProbleem()).setBord(new ResourceKey(
						"Bord", layer.getSelection().get(0)));

				Bord selectedBord = (Bord) modelRepository
						.loadObject(new ResourceKey("Bord", layer
								.getSelection().get(0)));

				// Zoek traject dat bij routebord hoort
				if (selectedBord instanceof RouteBord) {
					object.setTraject(((RouteBord) selectedBord).getRoute());
				}

				// Zoek segment dat bij NetwerkBord hoort
				else if (selectedBord instanceof NetwerkBord) {
					object.setTraject(((NetwerkBord) selectedBord)
							.getSegmenten().get(0));
				}
			}

			else {
				messages.error("Er zijn meerdere borden geselecteerd. Mogelijk bevinden deze borden zich op dezelfde locatie. Gelieve in dit geval de 'Tonen informatie' knop te gebruiken om uw selectie te verfijnen.");
				layer.setSelection(new ArrayList<String>(1));
			}
		}
	}

	/**
	 * Event bij het deselecteren van features op de kaart
	 * 
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void onUnselectFeatures(ControllerEvent event) {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			layer.setSelection(new ArrayList<String>(1));
		}

		List<String> ids = (List<String>) event.getParams().get("featureIds");
		if (ids.size() > 0) {
			if (object.getProbleem() instanceof BordProbleem) {
				((BordProbleem) object.getProbleem()).setBord(null);
			}

			if (object.getProbleem() instanceof NetwerkAnderProbleem) {
				object.setTraject(null);
			}
		}
	}

	/**
	 * Event bij het updaten van features op de kaart
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void onUpdateFeatures(ControllerEvent event) throws IOException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_NAME);

		// Slechts 1 punt mag ingegeven worden
		if (layer.getGeometries().size() > 1) {
			layer.getGeometries().remove(0);
		}

		// Precies 1 punt koppelen aan een Anderprobleem
		if (layer.getGeometries().size() == 1) {

			if (object.getProbleem() instanceof NetwerkAnderProbleem) {

				// Zoek het dichtstbijzijnde NetwerkSegment
				ResourceIdentifier segmentId = Beans.getReference(
						OsyrisModelFunctions.class).getNearestSegment(
						layer.getGeometries().iterator().next(),
						trajectTypeCreate.replace("netwerk", "Netwerk"));

				// Koppel het dichtstbijzijnde NetwerkSegment bij het
				// Probleempunt
				if (segmentId != null) {
					object.setTraject(segmentId);
					((AnderProbleem) object.getProbleem()).setGeom(layer
							.getGeometries().iterator().next());
				} else {
					layer.getGeometries().clear();
				}

			} else if (object.getProbleem() instanceof AnderProbleem) {
				((AnderProbleem) object.getProbleem()).setGeom(layer
						.getGeometries().iterator().next());
			}
		}
	}

	/**
	 * Event bij het deleten van features op de kaart
	 * 
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void onDeleteFeatures(ControllerEvent event) {

		List<String> ids = (List<String>) event.getParams().get("featureIds");
		if (ids.size() > 0) {
			((AnderProbleem) object.getProbleem()).setGeom(null);
		}
	}

	/**
	 * Filteren van de laag op basis van de trajectNaam en setten van TrajectID
	 * via trajectNaam. Dit is enkel toepasbaar op routes.
	 * 
	 * @param layer
	 */
	public void searchTrajectId(FeatureMapLayer layer) {

		if (layer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getViewer()
					.getFeature(layer, getViewer().getContext().getSrsName(),
							getViewer().getContext().getBoundingBox(), null,
							FilterUtils.equal("naam", trajectNaamCreate), null,
							1);
			FeatureIterator<SimpleFeature> iterator = features.features();

			try {
				if (features.size() == 1) {
					while (iterator.hasNext()) {
						SimpleFeature feature = iterator.next();
						getWerkOpdracht().setTraject(
								new ResourceKey("Traject", feature
										.getAttribute("id").toString()));
					}
				}
			} finally {
				iterator.close();
			}
		}
	}

	/**
	 * Maakt een door de gebruiker gekozen type probleem aan en configureert de
	 * kaart om problemen te kunnen aanduiden. De status van het probleem
	 * gekoppeld aan een nieuwe manueel aangemaakte WerkOpdracht heeft steeds de
	 * waarde WERKOPDRACHT.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void createProbleem() throws InstantiationException,
			IllegalAccessException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		if (object.getProbleem() != null) {
			modelRepository.evictObject(object.getProbleem());
			object.setProbleem(null);
		}

		Probleem probleem = null;

		if ("bord".equals(probleemType)) {

			// context.setShowFeatureInfoControl(true);
			context.setShowSelectControl(true);
			context.setShowDrawPointControl(false);

			if (trajectTypeCreate.endsWith("Route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteBordProbleem", null);

			} else if (trajectTypeCreate.contains("Netwerk")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkBordProbleem", null);
			}

			// Itereren over de lagen en de correcte lagen selecteerbaar zetten
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.set("selectable", false);

				if (layer.getLayerId().equalsIgnoreCase(
						trajectTypeCreate + "Bord")) {
					layer.set("selectable", true);
					layer.set("selectionMode", FeatureSelectionMode.SINGLE);
					layer.setSelection(new ArrayList<String>(1));

				} else if (layer.getLayerId().equalsIgnoreCase(
						trajectTypeCreate.replace("Segment", "") + "Bord")) {
					layer.set("selectable", true);
					layer.set("selectionMode", FeatureSelectionMode.SINGLE);
					layer.setSelection(new ArrayList<String>(1));

				} else if (layer.getLayerId().contains("Knooppunt")) {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}

				else if (layer.getLayerId().equalsIgnoreCase(
						GEOMETRY_LAYER_NAME)) {
					layer.setHidden(true);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(Collections.EMPTY_LIST);
				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		} else if ("ander".equals(probleemType)) {

			// context.setShowFeatureInfoControl(false);
			context.setShowSelectControl(false);
			context.setShowDrawPointControl(true);

			if (trajectTypeCreate.endsWith("Route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteAnderProbleem", null);

			} else if (trajectTypeCreate.contains("Netwerk")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkAnderProbleem", null);
			}

			for (FeatureMapLayer layer : context.getFeatureLayers()) {

				// Geometrie laag
				if (layer.getLayerId().equalsIgnoreCase(GEOMETRY_LAYER_NAME)) {
					layer.setHidden(false);
					layer.set("selectable", true);
					layer.set("editable", true);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(new ArrayList<Geometry>(1));
				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		}

		probleem.setStatus(ProbleemStatus.WERKOPDRACHT);
		object.setProbleem(probleem);
		viewer.updateContext(null);
	}

	/**
	 * Annuleren van een manueel aangemaakte WerkOpdracht
	 * 
	 */
	public void cancelCreateWO() {
		if (object != null) {
			modelRepository.evictObject(object);
		}
		trajectTypeCreate = null;
		regioCreate = null;
		trajectNaamCreate = null;
		probleemType = null;
		object = null;
		search();
	}

	/**
	 * Custom WerkOpdracht CSV export
	 * 
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> report() {

		File file = null;

		DefaultModelObjectList objectList = new DefaultModelObjectList<WerkOpdracht>(
				getModelClass(), getResults());

		// Write CSV to disk
		Content content = new EncodableContent<ModelObjectList>(
				(Encoder) new WerkOpdrachtCSVModelEncoder(), objectList);

		Random randomGenerator = new Random();
		Integer randomInt = randomGenerator.nextInt(1000000000);
		String fileName = "WerkOpdrachtOverzicht_" + randomInt.toString()
				+ ".csv";

		String location = DefaultConfiguration.instance().getString(
				"osyris.location.temp.csv");

		file = new File(location + fileName);

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			content.encode(file);
		} catch (IOException e) {
			LOG.error("Can not encode CSV file.");
		} catch (Exception e) {
			LOG.error("Can not create new CSV file.");
		}
		return new FileResource(file);
	}

	/*
	 * public void saveImagesToDisk() {
	 * 
	 * File file = null;
	 * 
	 * try { for (WerkOpdracht wo : getResults()) {
	 * 
	 * if (wo.getFoto() != null) {
	 * 
	 * // WerkOpdracht wo = (WerkOpdracht) modelRepository // .loadObject(new
	 * ResourceKey("WerkOpdracht", "4"));
	 * 
	 * String fileName = wo.getId().toString() + "_foto1" + ".jpg";
	 * 
	 * String location = DefaultConfiguration.instance()
	 * .getString("osyris.location.temp.csv");
	 * 
	 * file = new File(location + wo.getId().toString() + "/" + fileName);
	 * 
	 * file.getParentFile().mkdirs();
	 * 
	 * if (!file.exists()) { file.createNewFile(); } FileOutputStream fos = new
	 * FileOutputStream(file.getPath());
	 * 
	 * fos.write(wo.getFoto()); fos.close(); } } } catch (Exception e) {
	 * 
	 * } }
	 */
}
