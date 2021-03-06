package be.gim.tov.osyris.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SortOrder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
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
import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.search.QueryOrderBy;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.resource.FileResource;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.jsf.component.ComponentUtils;
import org.primefaces.event.FileUploadEvent;
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
import be.gim.specto.api.context.RasterMapLayer;
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
import be.gim.tov.osyris.model.werk.status.ActieStockStatus;
import be.gim.tov.osyris.model.werk.status.UitvoeringsrondeStatus;
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
public class UitvoeringsrondeOverzichtFormBase extends
		AbstractListForm<Uitvoeringsronde> implements Serializable {
	private static final long serialVersionUID = 3771393152252852618L;

	private static final Log LOG = LogFactory
			.getLog(UitvoeringsrondeOverzichtFormBase.class);

	private static final String GEOMETRY_LAYER_NAME = "geometry";
	private static final String GEOMETRY_LAYER_LINE_NAME = "geometryLine";

	public static final String WO_PDF = "/META-INF/resources/osyris/xslts/werkOpdrachtPdf.xsl";

	public static final String WO_UVR_PDF = "/META-INF/resources/osyris/xslts/werkOpdrachtenPdf.xsl";

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected MapFactory mapFactory;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected CacheProducer cacheProducer;

	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected ResourceIdentifier werkOpdracht;
	protected ResourceIdentifier uitvoerder;
	protected ResourceIdentifier medewerker;
	protected WerkOpdracht selectedWerkOpdracht;
	protected GebruiktMateriaal selectedMateriaal;
	protected ResourceIdentifier trajectId;
	protected boolean hasErrors;
	protected String baseLayerName;
	protected List<WerkOpdracht> opdrachtenInRonde;
	protected WerkOpdracht opdrachtInRonde;
	protected List<String> bordSelection;
	protected List<Geometry> anderProbleemGeoms;
	protected List<Geometry> anderProbleemLineGeoms;

	protected List<String> rondeIds;
	protected List<Long> werkOpdrachtIds;
	protected List<ResourceIdentifier> filteredWerkOpdrachten;
	protected String hasMateriaal;

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

	public WerkOpdracht getSelectedWerkOpdracht() {
		return selectedWerkOpdracht;
	}

	public void setSelectedWerkOpdracht(WerkOpdracht selectedWerkOpdracht) {
		this.selectedWerkOpdracht = selectedWerkOpdracht;
	}

	public GebruiktMateriaal getSelectedMateriaal() {
		return selectedMateriaal;
	}

	public void setSelectedMateriaal(GebruiktMateriaal selectedMateriaal) {
		this.selectedMateriaal = selectedMateriaal;
	}

	public ResourceIdentifier getTrajectId() {
		return trajectId;
	}

	public void setTrajectId(ResourceIdentifier trajectId) {
		this.trajectId = trajectId;
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public String getBaseLayerName() {
		return baseLayerName;
	}

	public void setBaseLayerName(String baseLayerName) {
		this.baseLayerName = baseLayerName;
	}

	public List<WerkOpdracht> getOpdrachtenInRonde() {
		return opdrachtenInRonde;
	}

	public void setOpdrachtenInRonde(List<WerkOpdracht> opdrachtenInRonde) {
		this.opdrachtenInRonde = opdrachtenInRonde;
	}

	public WerkOpdracht getOpdrachtInRonde() {
		return opdrachtInRonde;
	}

	public void setOpdrachtInRonde(WerkOpdracht opdrachtInRonde) {
		this.opdrachtInRonde = opdrachtInRonde;
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

	public List<String> getRondeIds() {
		return rondeIds;
	}

	public void setRondeIds(List<String> rondeIds) {
		this.rondeIds = rondeIds;
	}

	public List<Long> getWerkOpdrachtIds() {
		return werkOpdrachtIds;
	}

	public void setWerkOpdrachtIds(List<Long> werkOpdrachtIds) {
		this.werkOpdrachtIds = werkOpdrachtIds;
	}

	public void setFilteredWerkOpdrachten(
			List<ResourceIdentifier> filteredWerkOpdrachten) {
		this.filteredWerkOpdrachten = filteredWerkOpdrachten;
	}

	public String getHasMateriaal() {

		if (selectedWerkOpdracht != null
				&& selectedWerkOpdracht.getMaterialen() != null
				&& selectedWerkOpdracht.getMaterialen().size() > 0) {
			return "true";
		} else {

			// Zet standaard hasMateriaal op true
			if (hasMateriaal == null) {
				if (selectedWerkOpdracht != null
						&& selectedWerkOpdracht.getMaterialen() != null) {
					return "true";
				} else {
					return "false";
				}
			}
			return hasMateriaal;
		}
	}

	public void setHasMateriaal(String hasMateriaal) {
		this.hasMateriaal = hasMateriaal;
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

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
	}

	@Override
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		try {
			if (identity.inGroup("Uitvoerder", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("uitvoerder", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
			}

			if (werkOpdracht != null) {
				query.addFilter(FilterUtils.equal("opdrachten", werkOpdracht));
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}

		DefaultQueryOrderBy orderBy = new DefaultQueryOrderBy(
				FilterUtils.property("id"));
		orderBy.setSortOrder(SortOrder.DESCENDING);

		query.setOrderBy(Collections.singletonList((QueryOrderBy) orderBy));

		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void search() {

		dataModel = null;

		try {
			Query query = transformQuery(getQuery());

			List<Uitvoeringsronde> list = (List<Uitvoeringsronde>) modelRepository
					.searchObjects(query, true, isIndex(), true);

			if (uitvoerder == null && medewerker == null && trajectType == null
					&& regio == null && trajectNaam == null) {

				// Deze velden moeten gezocht worden in WerkOpdrachten, indien
				// allemaal leeg normale query uitvoeren
				results = list;

			} else {
				// Filter Uitvoeringsronde ahv resultaten Werkopdracht query
				query.addFilter(FilterUtils.id(getSubQueryIds(list)));

				// if (werkOpdracht == null) {
				// query.addFilter(FilterUtils.in("opdrachten",
				// getFilteredWerkOpdrachten()));
				// }

				results = (List<Uitvoeringsronde>) modelRepository
						.searchObjects(query, true, isIndex(), true);
			}
		} catch (IOException e) {
			LOG.error("Can not search Uitvoeringsronde.", e);
			results = null;
		}
	}

	/**
	 * Zoeken van alle WerkOpdrachten in een UitvoeringsRonde.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<WerkOpdracht> getWerkOpdrachtenInUitvoeringsronde(
			Uitvoeringsronde ronde) {

		try {
			if (ronde != null) {
				opdrachtenInRonde = new ArrayList<WerkOpdracht>();

				for (ResourceIdentifier id : ronde.getOpdrachten()) {
					opdrachtInRonde = (WerkOpdracht) modelRepository
							.loadObject(id);

					if (opdrachtInRonde != null) {
						opdrachtenInRonde.add(opdrachtInRonde);
					}
				}
				return opdrachtenInRonde;

			}
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht", e);
		}
		return Collections.emptyList();

		// return (List<WerkOpdracht>) cacheProducer.getCache(
		// "WerkOpdrachtInRondeCache", new Transformer() {
		//
		// @Override
		// public Object transform(Object ronde) {
		//
		// try {
		// if (ronde != null) {
		// opdrachtenInRonde = new ArrayList<WerkOpdracht>();
		//
		// for (ResourceIdentifier id : ((Uitvoeringsronde) ronde)
		// .getOpdrachten()) {
		// opdrachtInRonde = (WerkOpdracht) modelRepository
		// .loadObject(id);
		//
		// if (opdrachtInRonde != null) {
		// opdrachtenInRonde.add(opdrachtInRonde);
		// }
		// }
		// return opdrachtenInRonde;
		//
		// }
		// } catch (IOException e) {
		// LOG.error("Can not load WerkOpdracht", e);
		// }
		// return Collections.emptyList();
		// }
		// }).get(ronde);
	}

	@Override
	public void delete() {
		try {
			// Set WerkOpdrachten inRonde flag to false
			for (WerkOpdracht opdracht : getWerkOpdrachtenInUitvoeringsronde(object)) {
				opdracht.setInRonde("0");
				// Save WerkOpdracht
				modelRepository.saveObject(opdracht);
			}

			// Links verwijderen met ronde
			object.setOpdrachten(null);
			modelRepository.saveObject(object);

			// Delete Uitvoeringsronde
			modelRepository.deleteObject(object);
			messages.info("Uitvoeringsronde succesvol verwijderd. De bijbehorende werkopdrachten behoren niet meer toe aan een uitvoeringsronde.");
			clear();
			search();

		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van de uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Can not delete model object.", e);
		}
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

			if (selectedWerkOpdracht != null) {

				// Ophalen traject
				Traject traject = (Traject) modelRepository
						.loadObject(selectedWerkOpdracht.getTraject());

				// ROUTES EN LUSSEN
				if (traject instanceof Route || traject instanceof NetwerkLus) {

					processRouteOrNetwerkLus(traject, configuration, context);
					return configuration;
				}

				// SEGMENTEN
				if (traject instanceof NetwerkSegment) {

					processNetwerkSegment(traject, configuration, context);
					return configuration;
				}
			}
		}
		return null;
	}

	/**
	 * Verwijderen van een werkopdracht uit een uitvoeringsronde.
	 * 
	 * @param ronde
	 * @param opdracht
	 */
	public void removeFromUitvoeringsronde() {

		try {
			// Verwijder WO uit ronde
			object.getOpdrachten()
					.remove(modelRepository
							.getResourceIdentifier(selectedWerkOpdracht));
			// Set flag inRonde false
			selectedWerkOpdracht.setInRonde("0");

			// Save opdracht en ronde
			modelRepository.saveObject(object);
			modelRepository.saveObject(selectedWerkOpdracht);
			messages.info("Werkopdracht succesvol verwijderd uit uitvoeringsronde.");

		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van de werkopdracht uit de uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Can not save object.", e);
		}
	}

	/**
	 * Rapporteren van een WerkOpdracht in een Uitvoeringsronde.
	 * 
	 */
	public void rapporteerWerkOpdrachtInRonde() {

		hasErrors = true;

		try {
			if (checkGerapporteerdeWerkOpdracht()) {

				hasErrors = false;

				// Expliciet leegmaken van de gebruikte materialen indien
				// aangevinkt
				if (hasMateriaal.equals("false")) {
					selectedWerkOpdracht.setMaterialen(null);
				}

				selectedWerkOpdracht
						.setStatus(WerkopdrachtStatus.IN_UITVOERING);
				selectedWerkOpdracht.setDatumLaatsteWijziging(new Date());
				// selectedWerkOpdracht.setDatumGerapporteerd(new Date());
				modelRepository.saveObject(selectedWerkOpdracht);
				messages.info("Werkopdracht succesvol in uitvoering geplaatst.");

				resetHasMateriaal();
			}

		} catch (IOException e) {
			messages.error("Fout bij het rapporteren van de werkopdracht in uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Can not save werkopdracht.", e);
		}
	}

	@Override
	public void save() {

		try {
			modelRepository.saveObject(object);
			messages.info("Uitvoeringsronde succesvol bewaard.");

		} catch (IOException e) {
			messages.error("fout bij het bewaren van uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Can not save Uitvoeringsronde.", e);
		}
	}

	public void saveWO(WerkOpdracht werkOpdracht) {

		try {
			modelRepository.saveObject(getSelectedWerkOpdracht());

		} catch (IOException e) {
			messages.error("fout bij het bewaren van Werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not save Uitvoeringsronde.", e);
		}
	}

	/**
	 * Rapporteren van een Uitvoeringsronde waarin alle WerkOpdrachten
	 * gerapporteerd zijn.
	 * 
	 */
	public void rapporteerUitvoeringsRonde() {

		setHasErrors(true);

		try {
			if (object.getAfstand() <= 0) {
				messages.error("Uitvoeringsronde niet gerapporteerd: De het totaal aantal km moet groter zijn dan 0.");
				return;
			}

			// Checken of alle WO in status IN_UITVOERING, GERAPPORTEERD of
			// GEVALIDEERD zijn
			if (checkWerkOpdrachtenGerapporteerd()) {

				// Afboeken stock voor elk gebruikt materiaal in elke WO
				for (ResourceIdentifier id : object.getOpdrachten()) {
					WerkOpdracht opdracht = (WerkOpdracht) modelRepository
							.loadObject(id);

					// Indien status IN_UITVOERING mag de werkopdracht in status
					// GERAPPORTEERD komen te staan.
					if (opdracht.getStatus().equals(
							WerkopdrachtStatus.IN_UITVOERING)) {
						opdracht.setStatus(WerkopdrachtStatus.GERAPPORTEERD);
						opdracht.setDatumLaatsteWijziging(new Date());
						opdracht.setDatumGerapporteerd(new Date());
					}
					// Enkel stock manipuleren
					// if(opdracht.getStatus().equals(WerkopdrachtStatus.GERAPPORTEERD))

					if (opdracht.getMaterialen() != null
							&& !opdracht.getMaterialen().isEmpty()) {

						for (GebruiktMateriaal materiaal : opdracht
								.getMaterialen()) {

							// Toevoegen aan stock als actie IN is
							if (materiaal.getActieStock().equals(
									ActieStockStatus.IN)) {

								int inStockUpdated = materiaal
										.getStockMateriaal().getInStock()
										+ materiaal.getAantal();
								materiaal.getStockMateriaal().setInStock(
										inStockUpdated);
								// Save
								modelRepository.saveObject(materiaal
										.getStockMateriaal());
							}

							else if (materiaal.getActieStock().equals(
									ActieStockStatus.UIT)) {
								// Verwijderen uit stock als actie UIT is
								int inStockUpdated = materiaal
										.getStockMateriaal().getInStock()
										- materiaal.getAantal();
								materiaal.getStockMateriaal().setInStock(
										inStockUpdated);
								// Save
								modelRepository.saveObject(materiaal
										.getStockMateriaal());

								// Checken of materiaal aan limiet zit en mail
								// sturen
								if (materiaal.getStockMateriaal().getInStock() <= materiaal
										.getStockMateriaal().getMin()) {

									// Stuur email naar Routedokters
									String mailServiceStatus = DefaultConfiguration
											.instance()
											.getString(
													"service.mail.stockMateriaal");

									if (mailServiceStatus
											.equalsIgnoreCase("on")) {
										Beans.getReference(
												OsyrisModelFunctions.class)
												.sendMailStockMateriaalLimiet(
														materiaal
																.getStockMateriaal());
									}
								}
							}
						}
					}
				}

				object.setStatus(UitvoeringsrondeStatus.UITGEVOERD);
				modelRepository.saveObject(object);
				messages.info("Uitvoeringsronde succesvol gerapporteerd.");
				setHasErrors(false);

			} else {
				messages.error("Uitvoeringsronde niet gerapporteerd: De uitvoeringsronde bevat nog werkopdrachten in status 'Uit te voeren'.");
			}

		} catch (IOException e) {
			messages.error("fout bij het rapporteren van uitvoeringsronde: "
					+ e.getMessage());
			LOG.error("Can not save Uitvoeringsronde.", e);
		}
	}

	/**
	 * Check of alle Werkopdrachten de status gerapporteerd hebben.
	 * 
	 * @return
	 */
	private boolean checkWerkOpdrachtenGerapporteerd() {

		boolean isGerapporteerd = true;

		try {
			for (ResourceIdentifier id : object.getOpdrachten()) {
				WerkOpdracht opdracht = (WerkOpdracht) modelRepository
						.loadObject(id);

				if (!opdracht.getStatus().equals(
						WerkopdrachtStatus.GERAPPORTEERD)
						&& !opdracht.getStatus().equals(
								WerkopdrachtStatus.GEVALIDEERD)
						&& !opdracht.getStatus().equals(
								WerkopdrachtStatus.IN_UITVOERING)) {
					isGerapporteerd = false;
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht.", e);
		}
		return isGerapporteerd;
	}

	/**
	 * Verwijderen van een GebruiktMateriaal uit een WerkOpdracht.
	 * 
	 * @param opdracht
	 * @param materiaal
	 */
	public void removeMateriaalFromWerkOpdracht() {

		try {
			selectedWerkOpdracht.getMaterialen().remove(selectedMateriaal);
			modelRepository.saveObject(selectedWerkOpdracht);
			messages.info("Materiaal succesvol verwijderd uit werkopdracht.");

		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van materiaal uit werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not ssave object.", e);
		}
	}

	/**
	 * Checken of Probleem een Bordprobleem is.
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
	 * Opzoeken van knooppunten voor lussen
	 * 
	 * @param layer
	 */
	private void searchKnooppuntLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.setFilter(null);
		layer.setSelection(Collections.EMPTY_LIST);

		try {

			Traject traject = (Traject) modelRepository
					.loadObject(selectedWerkOpdracht.getTraject());

			Set<Long> knooppuntFilterIds = new HashSet<Long>();

			if (traject instanceof NetwerkLus) {

				NetwerkLus lus = (NetwerkLus) traject;

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

			// Ortho TMS als default achtergrondlaag
			for (RasterMapLayer baseLayer : context.getBaseRasterLayers()) {

				baseLayer.setHidden(true);

				if (baseLayer.getLayerId().equalsIgnoreCase("tms")) {
					baseLayer.setHidden(false);
					baseLayerName = baseLayer.getLayerId();
				}
			}
		}
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
	public void processRouteOrNetwerkLus(Traject traject,
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

		// Geometry laag voor lijn probleem
		GeometryListFeatureMapLayer geomLineLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_LINE_NAME, null, LineString.class, null,
						true, "single", null, null);

		// Ophalen laag afhankelijk van trajectType
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
		if (selectedWerkOpdracht.getProbleem() instanceof BordProbleem) {

			// Bord Probleem
			Bord bord = (Bord) modelRepository
					.loadObject(((BordProbleem) selectedWerkOpdracht
							.getProbleem()).getBord());

			Envelope envelope = null;

			// Checken of Bord niet verwijderd is
			if (bord != null) {
				bordSelection.add(bord.getId().toString());
				bordLayer.setSelection(bordSelection);
				envelope = GeometryUtils.getEnvelope(bord.getGeom());
				GeometryUtils.expandEnvelope(envelope, 0.05,
						context.getMaxBoundingBox());
			} else {
				envelope = GeometryUtils.getEnvelope(traject.getGeom());
			}

			context.setBoundingBox(envelope);

		} else if (selectedWerkOpdracht.getProbleem() instanceof AnderProbleem) {

			// Ander Probleem
			AnderProbleem anderProbleem = (AnderProbleem) selectedWerkOpdracht
					.getProbleem();

			if (anderProbleem.getGeom() != null
					&& anderProbleem.getGeom() instanceof Point) {

				anderProbleemGeoms.add(anderProbleem.getGeom());
				geomLayer.setGeometries(anderProbleemGeoms);
				geomLayer.setHidden(false);
			}

			else if (anderProbleem.getGeomOmleiding() instanceof LineString) {

				anderProbleemLineGeoms.add(anderProbleem.getGeomOmleiding());
				geomLineLayer.setGeometries(anderProbleemLineGeoms);
				geomLineLayer.setHidden(false);
			}

			Envelope envelope = GeometryUtils.getEnvelope(anderProbleem
					.getGeom());
			GeometryUtils.expandEnvelope(envelope, 0.05,
					context.getMaxBoundingBox());
			context.setBoundingBox(envelope);
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
	public void processNetwerkSegment(Traject traject,
			MapConfiguration configuration, MapContext context)
			throws InstantiationException, IllegalAccessException, IOException {

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
			Envelope envelope = GeometryUtils.getEnvelope(traject.getGeom());
			GeometryUtils.expandEnvelope(envelope, 0.1,
					context.getMaxBoundingBox());
			context.setBoundingBox(envelope);

			// Tonen knooppunten bij segment
			FeatureMapLayer knooppuntLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(LabelUtils
							.lowerCamelCase(traject.getModelClass().getName()
									.replace("Segment", "Knooppunt"))));

			if (knooppuntLayer != null) {
				searchKnooppuntLayer(knooppuntLayer);
			}

			// BordLayer
			bordLayer = null;
			bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
					.lowerCamelCase(LabelUtils.lowerCamelCase(traject
							.getModelClass().getName().replace("Segment", "")
							+ "Bord")));

			bordLayer.setHidden(false);
			bordLayer.setFilter(FilterUtils.equal("segmenten",
					modelRepository.getResourceIdentifier(traject)));

			if (selectedWerkOpdracht.getProbleem() instanceof BordProbleem) {
				// Bord Probleem
				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) selectedWerkOpdracht
								.getProbleem()).getBord());
				bordSelection.add(bord.getId().toString());
				bordLayer.setSelection(bordSelection);

			} else if (selectedWerkOpdracht.getProbleem() instanceof AnderProbleem) {
				// Ander Probleem
				AnderProbleem anderProbleem = (AnderProbleem) selectedWerkOpdracht
						.getProbleem();

				if (anderProbleem.getGeom() != null
						&& anderProbleem.getGeom() instanceof Point) {

					anderProbleemGeoms.add(anderProbleem.getGeom());
					geomLayer.setGeometries(anderProbleemGeoms);
					geomLayer.setHidden(false);
				}

				else if (anderProbleem.getGeomOmleiding() != null
						&& anderProbleem.getGeom() instanceof LineString) {

					anderProbleemLineGeoms
							.add(anderProbleem.getGeomOmleiding());
					geomLineLayer.setGeometries(anderProbleemLineGeoms);
					geomLineLayer.setHidden(false);
				}

				Envelope e = GeometryUtils.getEnvelope(anderProbleem.getGeom());
				GeometryUtils.expandEnvelope(e, 0.1,
						context.getMaxBoundingBox());
				context.setBoundingBox(e);
			}
		}
	}

	/**
	 * Zoeken naar de rondeIds die de gevonden werkopdrachten bevatten aan de
	 * hand van de opgegeven zoekparameters.
	 * 
	 * @param list
	 * @return
	 * @throws IOException
	 */
	private List<String> getSubQueryIds(List<Uitvoeringsronde> list)
			throws IOException {

		rondeIds = new ArrayList<String>();

		// Indien minstens 1 van de velden ingevuld WerkOpdracht query
		// uitvoeren en resultaten filteren op de uitvoeringsronde query
		QueryBuilder builder = new QueryBuilder("WerkOpdracht");

		if (uitvoerder != null) {
			builder.addFilter(FilterUtils.equal("uitvoerder", uitvoerder));
		}
		if (medewerker != null) {
			builder.addFilter(FilterUtils.equal("medewerker", medewerker));
		}

		if (trajectType != null) {
			builder.addFilter(FilterUtils.equal("trajectType", trajectType));
		}

		if (regio != null) {
			builder.addFilter(FilterUtils.equal("regioId", regio));
		}

		if (trajectId != null) {
			builder.addFilter(FilterUtils.equal("traject", trajectId));
		}

		// Enkel WerkOpdracht ids nodig
		builder.results(FilterUtils.properties("id"));
		werkOpdrachtIds = (List<Long>) modelRepository.searchObjects(
				builder.build(), false, false, false);

		// Omzetten ids naar ResourceIdentifiers
		filteredWerkOpdrachten = new ArrayList<ResourceIdentifier>();

		for (Long id : werkOpdrachtIds) {
			filteredWerkOpdrachten.add(new ResourceKey("WerkOpdracht", id
					.toString()));
		}

		// Zoeken naar de rondeIds met de gevonden WerkOpdracht Ids
		for (Uitvoeringsronde ronde : list) {

			for (ResourceIdentifier id : filteredWerkOpdrachten) {
				if (ronde.getOpdrachten().contains(id)
						&& !rondeIds.contains(ronde.getId().toString())) {
					rondeIds.add(ronde.getId().toString());
				}
			}
		}

		return rondeIds;
	}

	/**
	 * Zoeken naar de rondeIds die de gevonden werkopdrachten bevatten aan de
	 * hand van de opgegeven zoekparameters.
	 * 
	 * @param list
	 * @return
	 * @throws IOException
	 */
	private List<ResourceIdentifier> getFilteredWerkOpdrachten()
			throws IOException {

		// Indien minstens 1 van de velden ingevuld WerkOpdracht query
		// uitvoeren en resultaten filteren op de uitvoeringsronde query
		QueryBuilder builder = new QueryBuilder("WerkOpdracht");

		if (uitvoerder != null) {
			builder.addFilter(FilterUtils.equal("uitvoerder", uitvoerder));
		}
		if (medewerker != null) {
			builder.addFilter(FilterUtils.equal("medewerker", medewerker));
		}

		if (trajectType != null) {
			builder.addFilter(FilterUtils.equal("trajectType", trajectType));
		}

		if (regio != null) {
			builder.addFilter(FilterUtils.equal("regioId", regio));
		}

		if (trajectId != null) {
			builder.addFilter(FilterUtils.equal("traject", trajectId));
		}

		// Enkel WerkOpdracht ids nodig
		builder.results(FilterUtils.properties("id"));
		List<Long> ids = (List<Long>) modelRepository.searchObjects(
				builder.build(), false, false, true);

		// Omzetten ids naar ResourceIdentifiers
		List<ResourceIdentifier> filteredWerkOpdrachten = new ArrayList<ResourceIdentifier>();

		for (Long id : ids) {
			filteredWerkOpdrachten.add(new ResourceKey("WerkOpdracht", id
					.toString()));
		}

		return filteredWerkOpdrachten;
	}

	/**
	 * Switchen tussen basislagen
	 * 
	 */
	public void switchBaseLayers() {
		getViewer().setBaseLayerId(baseLayerName);
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
			Traject traject = (Traject) modelRepository
					.loadObject(selectedWerkOpdracht.getTraject());

			Document doc = null;
			XmlBuilder xmlBuilder = new XmlBuilder();

			if (selectedWerkOpdracht.getProbleem() instanceof BordProbleem) {

				BordProbleem bordProbleem = (BordProbleem) selectedWerkOpdracht
						.getProbleem();
				Bord bord = (Bord) modelRepository.loadObject(bordProbleem
						.getBord());

				doc = xmlBuilder.buildWerkOpdrachtFicheBordProbleem(
						getViewer(), traject, selectedWerkOpdracht, bord);
			}

			else if (selectedWerkOpdracht.getProbleem() instanceof AnderProbleem) {

				doc = xmlBuilder.buildWerkOpdrachtFicheAnderProbleem(
						getViewer(), traject, selectedWerkOpdracht);
			}

			Random randomGenerator = new Random();
			Integer randomInt = randomGenerator.nextInt(1000000000);
			String fileName = "werkopdracht_"
					+ selectedWerkOpdracht.getId().toString() + "_"
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
			javax.xml.transform.Transformer transformer = TransformerFactory
					.newInstance().newTransformer(xslt);
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
			// in.close();
			// out.close();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Printen van alle werkopdrachten die tot de UVR van de geselecteerde
	 * werkOpdracht behoren. De knop printen is toegevoegd aan het
	 * uitvoeringsrondeWerkOpdrachtViewPanel omdat hier een kaartje beschikbaar
	 * is om te kunnen manipuleren. In het uitvoeringsrondeViewPanel is geen
	 * kaartje beschikbaar.
	 * 
	 */
	public Content printWerkOpdrachtenInRonde() throws Exception {
		FileOutputStream out = null;
		File file = null;
		InputStream in = null;

		try {

			// Opbouwen XML
			Document doc = null;
			XmlBuilder xmlBuilder = new XmlBuilder();

			doc = xmlBuilder.buildWerkOpdrachtFichesInRonde(getViewer(),
					selectedWerkOpdracht,
					getWerkOpdrachtenInUitvoeringsronde(object));

			Random randomGenerator = new Random();
			Integer randomInt = randomGenerator.nextInt(1000000000);
			String fileName = "werkopdrachten_"
					+ selectedWerkOpdracht.getId().toString() + "ronde_"
					+ object.getId().toString() + "_" + randomInt.toString()
					+ ".pdf";

			String location = DefaultConfiguration.instance().getString(
					"osyris.location.temp.pdf.wo.fiche");

			file = new File(location + fileName);
			out = new FileOutputStream(file);

			Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF,
					FopFactory.newInstance().newFOUserAgent(), out);

			// xslt
			Source xslt = new StreamSource(getClass().getResourceAsStream(
					WO_UVR_PDF));
			in = getClass().getResourceAsStream(WO_UVR_PDF);

			// Transform source
			javax.xml.transform.Transformer transformer = TransformerFactory
					.newInstance().newTransformer(xslt);
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

		} finally {
			// in.close();
			// out.close();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Checken of men specifiek heeft aangeduid of er materialen zijn gebruikt
	 * voor de Werkopdracht of niet.
	 * 
	 * @return
	 */
	public boolean checkGerapporteerdeWerkOpdracht() {
		if (hasMateriaal != null && !hasMateriaal.isEmpty()) {

			if (hasMateriaal.equals("true") && selectedWerkOpdracht != null
					&& selectedWerkOpdracht.getMaterialen() != null
					&& selectedWerkOpdracht.getMaterialen().size() == 0) {
				messages.warn("Er zijn geen gebruikte materialen toegevoegd aan deze werkopdracht. Voeg 1 of meerdere materialen toe of vink expliciet 'Geen materiaal gebruikt' aan.");
				return false;
			}

			return true;

		} else {
			messages.warn("Gelieve eerst expliciet te op te geven of er al dan niet materiaal gebruikt is bij deze werkopdracht.");
			return false;
		}
	}

	public void checkHasMateriaal() {
		if (hasMateriaal.equals("false")) {
			if (selectedWerkOpdracht.getMaterialen() != null
					&& selectedWerkOpdracht.getMaterialen().size() > 0) {

				selectedWerkOpdracht.setMaterialen(null);
			}
		}
	}

	public void resetHasMateriaal() {
		hasMateriaal = null;
	}

	public void fileUploadListener(FileUploadEvent event) throws IOException {

		// String path = FacesContext.getCurrentInstance().getExternalContext()
		// .getRealPath("/");
		//
		// String fileName = selectedWerkOpdracht.getId().toString() + "_foto4"
		// + ".jpg";
		//
		// File file = new File("c:/Temp/test/"
		// + selectedWerkOpdracht.getId().toString() + "/" + fileName);
		//
		// file.getParentFile().mkdirs();
		//
		// if (!file.exists()) {
		// file.createNewFile();
		// }
		//
		// InputStream in = event.getFile().getInputstream();
		// FileOutputStream out = new FileOutputStream(file.getPath());
		//
		// int read = 0;
		// byte[] bytes = new byte[1024];
		//
		// while ((read = in.read(bytes)) != -1) {
		// out.write(bytes, 0, read);
		// }
		//
		// in.close();
		// out.flush();
		// out.close();
		//
		// String appURL = getViewer().getConfiguration().getGeoServletURL()
		// .replace("geocms/http/ogc", "");
		//
		// String url = "http://localhost/werkopdrachten/"
		// + selectedWerkOpdracht.getId().toString() + "/" + fileName;
		//
		// selectedWerkOpdracht.setFoto4(ResourceIdentifier.fromString(url));
	}

	public void deleteFoto4() {

		// String fileName = selectedWerkOpdracht.getId().toString() + "_foto4"
		// + ".jpg";
		//
		// File file = new File("c:/Temp/test/"
		// + selectedWerkOpdracht.getId().toString() + "/" + fileName);
		// file.delete();
		//
		// selectedWerkOpdracht.setFoto4(null);
	}
}