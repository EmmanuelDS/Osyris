package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.jsf.component.ComponentUtils;
import org.conscientia.jsf.prime.PrimeUtils;

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
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.GebruiktMateriaal;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.UitvoeringsrondeStatus;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

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
		AbstractListForm<Uitvoeringsronde> {

	private static final long serialVersionUID = 3771393152252852618L;

	private static final Log LOG = LogFactory
			.getLog(UitvoeringsrondeOverzichtFormBase.class);

	private static final String GEOMETRY_LAYER_NAME = "geometry";
	private static final String GEOMETRY_LAYER_LINE_NAME = "geometryLine";

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
	public Query getQuery() {

		if (query == null) {
			query = getDefaultQuery();
		}
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
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void search() {
		try {

			dataModel = null;

			List<Uitvoeringsronde> list = (List<Uitvoeringsronde>) modelRepository
					.searchObjects(getQuery(), true, true, true);

			// Deze velden moeten gezocht worden in WerkOpdrachten, indien
			// allemaal leeg normale query uitvoeren
			if (uitvoerder == null && medewerker == null && trajectType == null
					&& regio == null && trajectNaam == null) {

				results = list;
			}

			else {
				// Filter Uitvoeringsronde ahv resultaten Werkopdracht query
				query.addFilter(FilterUtils.id(getSubQueryIds(list)));

				results = (List<Uitvoeringsronde>) modelRepository
						.searchObjects(getQuery(), true, true, true);
			}
			dataModel = PrimeUtils.dataModel(results);

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

		return (List<WerkOpdracht>) cacheProducer.getCache(
				"WerkOpdrachtInRondeCache", new Transformer() {

					@Override
					public Object transform(Object ronde) {

						try {
							if (ronde != null) {
								List<WerkOpdracht> opdrachten = new ArrayList<WerkOpdracht>();

								for (ResourceIdentifier id : ((Uitvoeringsronde) ronde)
										.getOpdrachten()) {
									WerkOpdracht opdracht = (WerkOpdracht) modelRepository
											.loadObject(id);
									opdrachten.add(opdracht);
								}
								return opdrachten;

							}
						} catch (IOException e) {
							LOG.error("Can not load WerkOpdracht", e);
						}
						return Collections.emptyList();
					}
				}).get(ronde);
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

		// Verwijder werkopdracht uit ronde
		object.getOpdrachten().remove(
				modelRepository.getResourceIdentifier(selectedWerkOpdracht));
		// Set flag inRonde false
		selectedWerkOpdracht.setInRonde("0");

		try {
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

		try {

			selectedWerkOpdracht.setStatus(WerkopdrachtStatus.GERAPPORTEERD);
			selectedWerkOpdracht.setDatumGerapporteerd(new Date());
			modelRepository.saveObject(selectedWerkOpdracht);
			messages.info("Werkopdracht in uitvoeringsronde succesvol gerapporteerd.");

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

	/**
	 * Rapporteren van een Uitvoeringsronde waarin alle WerkOpdrachten
	 * gerapporteerd zijn.
	 * 
	 */
	public void rapporteerUitvoeringsRonde() {

		setHasErrors(true);

		try {
			if (checkWerkOpdrachtenGerapporteerd()) {
				object.setStatus(UitvoeringsrondeStatus.UITGEVOERD);
				modelRepository.saveObject(object);
				messages.info("Uitvoeringsronde succesvol gerapporteerd.");
				setHasErrors(false);

			} else {
				messages.error("Uitvoeringsronde niet gerapporteerd: De uitvoeringsronde bevat nog niet-gerapporteerde werkopdrachten.");
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
						WerkopdrachtStatus.GERAPPORTEERD)) {
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

			Set<String> knooppuntFilterIds = new HashSet<String>();

			if (traject instanceof NetwerkLus) {

				NetwerkLus lus = ((NetwerkLus) traject);

				for (ResourceIdentifier segment : lus.getSegmenten()) {
					NetwerkSegment seg = (NetwerkSegment) modelRepository
							.loadObject(segment);
					knooppuntFilterIds.add(modelRepository
							.loadObject(seg.getVanKnooppunt()).getId()
							.toString());
					knooppuntFilterIds.add(modelRepository
							.loadObject(seg.getNaarKnooppunt()).getId()
							.toString());
				}
				layer.setFilter(FilterUtils.in("id", knooppuntFilterIds));
			}

			else if (traject instanceof NetwerkSegment) {

				NetwerkSegment seg = (NetwerkSegment) traject;

				knooppuntFilterIds.add(modelRepository
						.loadObject(seg.getVanKnooppunt()).getId().toString());
				knooppuntFilterIds.add(modelRepository
						.loadObject(seg.getNaarKnooppunt()).getId().toString());

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
			bordSelection.add(bord.getId().toString());
			bordLayer.setSelection(bordSelection);

			Envelope envelope = GeometryUtils.getEnvelope(bord.getGeom());
			GeometryUtils.expandEnvelope(envelope, 0.1,
					context.getMaxBoundingBox());
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
			GeometryUtils.expandEnvelope(envelope, 0.1,
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
	private void processNetwerkSegment(Traject traject,
			MapConfiguration configuration, MapContext context)
			throws InstantiationException, IllegalAccessException, IOException {

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
			bordLayer.setFilter(FilterUtils.equal("segmenten", traject));

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

		List<String> rondeIds = new ArrayList<String>();

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
}
