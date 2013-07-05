package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.jsf.component.ComponentUtils;
import org.conscientia.jsf.event.ControllerEvent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.jboss.seam.international.status.Messages;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import be.gim.commons.filter.FilterUtils;
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
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.Traject;

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
public class ControleOpdrachtOverzichtFormBase extends
		AbstractListForm<ControleOpdracht> {
	private static final String GEOMETRY_LAYER_NAME = "geometry";

	private static final long serialVersionUID = -86881009141250710L;

	private static final Log LOG = LogFactory
			.getLog(ControleOpdrachtOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected Messages messages;
	@Inject
	protected MapFactory mapFactory;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;

	protected String controleOpdrachtType;
	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected Date vanDatum;
	protected Date totDatum;
	protected List<Bord> bewegwijzering;
	protected String probleemType;
	protected Probleem probleem;
	protected Probleem selectedProbleem;

	public String getControleOpdrachtType() {
		return controleOpdrachtType;
	}

	public void setControleOpdrachtType(String controleOpdrachtType) {
		this.controleOpdrachtType = controleOpdrachtType;
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

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
	}

	public List<Bord> getBewegwijzering() {
		return bewegwijzering;
	}

	public void setBewegwijzering(List<Bord> bewegwijzering) {
		this.bewegwijzering = bewegwijzering;
	}

	public String getProbleemType() {
		return probleemType;
	}

	public void setProbleemType(String probleemType) {
		this.probleemType = probleemType;
	}

	public Probleem getProbleem() {
		return probleem;
	}

	public void setProbleem(Probleem probleem) {
		this.probleem = probleem;
	}

	public Probleem getSelectedProbleem() {
		return selectedProbleem;
	}

	public void setSelectedProbleem(Probleem selectedProbleem) {
		this.selectedProbleem = selectedProbleem;
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

	@SuppressWarnings("unchecked")
	@Override
	public Query getQuery() {

		if (query == null) {
			query = getDefaultQuery();
		}
		try {
			// Medewerker moet gevalideerd en geannuleerd status niet kunnen
			// zien
			if (identity.inGroup("Medewerker", "CUSTOM")) {

				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
				query.addFilter(FilterUtils.and(FilterUtils.notEqual("status",
						ControleOpdrachtStatus.GEVALIDEERD), FilterUtils
						.notEqual("status", ControleOpdrachtStatus.GEANNULEERD)));
				return query;
			}
			// PeterMeter kan enkel status uit te voeren zien
			if (identity.inGroup("PeterMeter", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("peterMeter", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
				query.addFilter(FilterUtils.equal("status",
						ControleOpdrachtStatus.UIT_TE_VOEREN));
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

	@SuppressWarnings("unchecked")
	@Override
	public void search() {
		try {
			List<ControleOpdracht> list = (List<ControleOpdracht>) modelRepository
					.searchObjects(getQuery(), true, true, true);
			List<ControleOpdracht> filteredList = new ArrayList<ControleOpdracht>();

			// Kan dit op een betere manier gebeuren?
			if (vanDatum != null && totDatum != null) {
				for (ControleOpdracht c : list) {
					if (c.getDatumLaatsteWijziging().before(totDatum)
							&& c.getDatumLaatsteWijziging().after(vanDatum)) {
						filteredList.add(c);
					}
				}
				results = filteredList;
			} else {
				results = (List<ControleOpdracht>) modelRepository
						.searchObjects(getQuery(), true, true);
			}
		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			results = null;
		}
	}

	@Override
	public void save() {

		try {
			// Indien route achterhalen van de TrajectId via de routeNaam
			if (object.getTraject() == null) {
				if (trajectType.contains("Route")) {
					MapViewer viewer = getViewer();
					MapContext context = viewer.getConfiguration().getContext();

					for (FeatureMapLayer layer : context.getFeatureLayers()) {
						if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
							layer.setFilter(FilterUtils.equal("naam",
									trajectNaam));
							searchTrajectId(layer);
						}
					}
				}
			}
			if (object.getTraject() != null) {
				modelRepository.saveObject(object);
				clear();
				search();
			}
		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
		}
	}

	@Override
	public void clear() {
		object = null;
		controleOpdrachtType = null;
	}

	/**
	 * Basis Map Configuratie
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

			return configuration;
		}
		return null;
	}

	/**
	 * Maakt het bewegwijzeringsverslag voor het gekozen traject.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void createBewegwijzeringVerslag() {

		try {
			// Routes
			QueryBuilder builder = new QueryBuilder("Bord");
			builder.addFilter(FilterUtils.equal("naam", trajectNaam));
			// TODO: volgnummers voor netwerkborden moeten nog toegevoegd worden
			// in DB
			// TODO: volgnummers zijn geen numerieke velden
			// builder.orderBy(new
			// DefaultQueryOrderBy(FilterUtils.property("volg")));
			bewegwijzering = (List<Bord>) modelRepository.searchObjects(
					builder.build(), true, true);
		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
			bewegwijzering = null;
		}
	}

	/**
	 * Zoekt het bewegwijzeringsverslag voor een opgegeven traject
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Bord> createBewegwijzering(ResourceIdentifier trajectId) {

		try {
			// Routes
			Traject t = (Traject) modelRepository.loadObject(trajectId);
			if (t == null) {
				return null;
			}
			QueryBuilder builder = new QueryBuilder("Bord");
			builder.addFilter(FilterUtils.equal("naam", t.getNaam()));
			// TODO: volgnummers voor netwerkborden moeten nog toegevoegd worden
			// in DB
			// TODO: volgnummers zijn geen numerieke velden
			return (List<Bord>) modelRepository.searchObjects(builder.build(),
					true, true);
		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
			return null;
		}
	}

	/**
	 * Zoekt het traject aan de hand van de ingegeven zoekparameters
	 * 
	 * @throws IOException
	 */
	public void searchTraject() throws IOException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			layer.setFilter(null);
			// Traject
			if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
				// Netwerk
				if (trajectType.contains("Segment")) {
					searchNetwerkLayer(layer, viewer);
				}
				// Route
				else {
					searchRouteLayer(layer, viewer);
				}
			}
			// RouteBord
			else if (layer.getLayerId().equalsIgnoreCase(trajectType + "Bord")) {
				searchRouteLayer(layer, viewer);
			}
		}

		context.setBoundingBox(viewer.getContentExtent());
		viewer.updateContext(null);
	}

	/**
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchNetwerkLayer(FeatureMapLayer layer, MapViewer viewer) {
		layer.setHidden(false);
		if (trajectNaam != null) {
			layer.setFilter(FilterUtils.equal("naam", trajectNaam));
			viewer.updateCurrentExtent(viewer.getFeatureExtent(layer,
					FilterUtils.equal("naam", trajectNaam)));
		}
	}

	/**
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchRouteLayer(FeatureMapLayer layer, MapViewer viewer) {
		layer.setHidden(false);
		if (trajectNaam != null) {
			layer.setFilter(FilterUtils.like("naam", trajectNaam));
		}
	}

	/**
	 * Filters the layer based on trajectNaam and sets the TrajectID via
	 * trajectNaam. Dit is enkel toepasbaar op routes.
	 * 
	 * @param layer
	 */
	public void searchTrajectId(FeatureMapLayer layer) {
		if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getViewer()
					.getFeature(layer, getViewer().getContext().getSrsName(),
							getViewer().getContext().getBoundingBox(), null,
							FilterUtils.equal("naam", trajectNaam), null, 1);
			FeatureIterator<SimpleFeature> iterator = features.features();

			try {
				if (features.size() == 1) {
					while (iterator.hasNext()) {
						SimpleFeature feature = iterator.next();
						object.setTraject(new ResourceKey("Traject", feature
								.getAttribute("id").toString()));
					}
				}
			} finally {
				iterator.close();
			}
		}
	}

	/**
	 * Toont het traject met bijbehorende borden voor controleopdrachten in
	 * raadpleeg modus.
	 * 
	 * @param controleOpdracht
	 */
	public void showOpdrachtMapData(ResourceIdentifier trajectId) {
		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		FeatureMapLayer layer = null;
		String layerId = null;
		try {
			// Get traject
			Traject t = (Traject) modelRepository.loadObject(trajectId);

			// Routes
			layerId = Character.toLowerCase(t.getModelClass().getName()
					.charAt(0))
					+ t.getModelClass().getName().substring(1);
			layer = (FeatureMapLayer) context.getLayer(layerId);
			layer.setHidden(false);
			layer.setFilter(FilterUtils.equal("naam", t.getNaam()));
			context.setBoundingBox(viewer.getContentExtent(layer));

			// Routeborden
			layerId = layerId + "Bord";
			layer = (FeatureMapLayer) context.getLayer(layerId);
			layer.setHidden(false);
			layer.setFilter(FilterUtils.equal("naam", t.getNaam()));

			viewer.updateContext(null);

		} catch (IOException e) {
			LOG.error("Can load object", e);
		}
	}

	/**
	 * Annuleren van een controleopdracht
	 * 
	 */
	public void annuleerControleOpdracht() {
		if (object != null) {
			object.setStatus(ControleOpdrachtStatus.GEANNULEERD);
			try {
				modelRepository.saveObject(object);
				clear();
				search();
			} catch (IOException e) {
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Heropenen geannuleerde controleOpdracht
	 * 
	 */
	public void reopenControleOpdracht() {
		if (object != null) {
			object.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
			try {
				modelRepository.saveObject(object);
				clear();
				search();
			} catch (IOException e) {
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Verzenden van controleOpdracht naar de betrokken peterMeter
	 * 
	 */
	public void verzendenControleOpdracht() {
		if (object != null) {
			object.setStatus(ControleOpdrachtStatus.UIT_TE_VOEREN);
			object.setDatumUitTeVoeren(new Date());
			try {
				modelRepository.saveObject(object);
				clear();
				search();
				// send confirmatie mail naar peterMeter
				// sendConfirmationMail();
				messages.info("Controleopdracht verzonden.");
			} catch (IOException e) {
				LOG.error("Can not save object.", e);
			} catch (Exception e) {
				LOG.error("Can not send email", e);
			}
		}
	}

	/**
	 * Rapporteren van controleOpdracht naar TOV
	 * 
	 */
	public void rapporterenControleOpdracht() {
		if (object != null) {
			object.setStatus(ControleOpdrachtStatus.GERAPPORTEERD);
			object.setDatumGerapporteerd(new Date());
			try {
				modelRepository.saveObject(object);
				clear();
				search();
				messages.info("Controleopdracht gerapporteerd.");
			} catch (IOException e) {
				LOG.error("Can not save object.", e);
			} catch (Exception e) {
				LOG.error("Can not send email", e);
			}
		}
	}

	/**
	 * Stuurt een mail naar de betrokken peterMeter dat er een nieuwe
	 * controleOpdracht op status uit te voeren beschikbaar is
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	private void sendConfirmationMail() throws IOException, Exception {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);
		// TODO: hardcoded link vervangen
		variables
				.put("link",
						"http://www.tov-osyris.gim.be/geocms/web/view/form:ControleOpdrachtOverzichtForm");

		mailSender.sendMail(
				preferences.getNoreplyEmail(),
				Collections.singleton(modelRepository
						.loadObject(object.getPeterMeter())
						.getAspect("UserProfile").get("email").toString()),
				"/META-INF/resources/core/mails/confirmControleOpdracht.fmt",
				variables);
	}

	/**
	 * Zoomt naar een bepaald bord op de kaart
	 * 
	 * @param bord
	 */
	public void zoomToBord(Bord bord) {
		MapViewer viewer = getViewer();
		Envelope envelope = new Envelope(bord.getGeom().getCoordinate());
		viewer.updateCurrentExtent(envelope);
	}

	/**
	 * Oplijsten van problemen van het type BordProbleem
	 * 
	 * @return
	 */
	public List<BordProbleem> getBordProblemen() {
		List<BordProbleem> bordProblemen = new ArrayList<BordProbleem>();

		if (object != null) {
			for (Probleem probleem : object.getProblemen()) {
				if (probleem instanceof BordProbleem) {
					bordProblemen.add((BordProbleem) probleem);
				}
			}
		}
		return bordProblemen;
	}

	/**
	 * Oplijsten van problemen van het type AnderProbleem
	 * 
	 * @return
	 */
	public List<AnderProbleem> getAndereProblemen() {
		List<AnderProbleem> andereProblemen = new ArrayList<AnderProbleem>();
		if (object != null) {
			for (Probleem probleem : object.getProblemen()) {
				if (probleem instanceof AnderProbleem) {
					andereProblemen.add((AnderProbleem) probleem);
				}
			}
		}
		return andereProblemen;
	}

	/**
	 * Aanmaken van een probleem bij een controleopdracht
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void createProbleem() throws InstantiationException,
			IllegalAccessException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		probleem = null;

		// Bordprobleem
		if ("bord".equals(probleemType)) {
			if (object.getTrajectType().contains("route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteBordProbleem", null);
			} else if (object.getTrajectType().contains("netwerk")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkBordProbleem", null);
			}
			// Itereren over de lagen en de correctie lagen selecteerbaar zetten
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				if (layer.getLayerId().equalsIgnoreCase(
						object.getTrajectType() + "Bord")) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
				} else if (layer.getLayerId().equalsIgnoreCase(
						GEOMETRY_LAYER_NAME)) {
					layer.setHidden(true);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(Collections.EMPTY_LIST);
				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		}
		// Ander probleem
		else if ("ander".equals(probleemType)) {
			if (object.getTrajectType().contains("route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteAnderProbleem", null);
			} else if (trajectType.contains("netwerk")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkAnderProbleem", null);
			}

			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
				} else if (layer.getLayerId().equalsIgnoreCase(
						GEOMETRY_LAYER_NAME)) {
					layer.setHidden(false);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(new ArrayList<Geometry>(1));
				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		}
		viewer.updateContext(null);
	}

	/**
	 * Toevoegen van een probleem aan de controleOpdracht
	 * 
	 */
	public void addProbleem() {
		// Toevoegen probleem aan controleOpdracht
		Probleem p = getProbleem();
		object.getProblemen().add(p);

		// Reset probleem
		probleemType = null;
		setProbleem(null);

		// Deselect all features
		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			layer.setSelection(null);
		}
		viewer.updateContext(null);
	}

	/**
	 * Event bij het selecteren van features op de kaart
	 * 
	 * @param event
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void onSelectFeatures(ControllerEvent event) throws IOException {
		// Routes
		if (object.getTrajectType().contains("route")) {
			if (getProbleem() != null) {
				if (getProbleem() instanceof BordProbleem) {
					List<String> ids = (List<String>) event.getParams().get(
							"featureIds");
					if (ids.size() > 0) {
						String id = ids.iterator().next();
						((BordProbleem) getProbleem()).setBord(new ResourceKey(
								"Bord", id));
					}
				}
			}
		}
	}

	/**
	 * Event bij het updaten van features op de kaart
	 * 
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	public void onUpdateFeatures(ControllerEvent event) {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		List<String> ids = (List<String>) event.getParams().get("featureIds");
		if (ids.size() > 0) {
			GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) context
					.getLayer(GEOMETRY_LAYER_NAME);
			if (probleem instanceof AnderProbleem) {
				((AnderProbleem) probleem).setGeom(layer.getGeometries()
						.iterator().next());
			}
		}
	}

	/**
	 * Zoomen naar bordprobleem op de kaart
	 * 
	 * @param bordProbleem
	 */
	public void zoomToBordProbleem(BordProbleem bordProbleem) {
		MapViewer viewer = getViewer();
		Envelope envelope;
		try {
			envelope = new Envelope(
					((Bord) modelRepository.loadObject(bordProbleem.getBord()))
							.getGeom().getCoordinate());
			viewer.updateCurrentExtent(envelope);
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * Zoomen naar een anderProbleem op de kaart
	 * 
	 * @param bordProbleem
	 */
	public void zoomToAnderProbleem(AnderProbleem anderProbleem) {
		MapViewer viewer = getViewer();
		Envelope envelope;
		GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) viewer
				.getConfiguration().getContext().getLayer(GEOMETRY_LAYER_NAME);
		layer.setHidden(false);
		if (anderProbleem.getGeom() instanceof Point) {
			envelope = new Envelope(anderProbleem.getGeom().getCoordinate());
			viewer.updateCurrentExtent(envelope);
		}
	}

	/**
	 * Valideren ven een probleem
	 * 
	 */
	public void validateProbleem() {
		try {
			modelRepository.saveObject(selectedProbleem);
		} catch (IOException e) {
			LOG.error("Can not save object.", e);
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasBordInfo() {
		if (selectedProbleem != null) {
			if (selectedProbleem instanceof BordProbleem) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
