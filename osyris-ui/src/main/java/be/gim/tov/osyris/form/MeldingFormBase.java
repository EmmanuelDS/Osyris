package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.security.annotation.RunPrivileged;
import org.conscientia.jsf.component.ComponentUtils;
import org.conscientia.jsf.event.ControllerEvent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.jboss.seam.international.status.Messages;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.commons.resource.ResourceName;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.FeatureSelectionMode;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.NetwerkAnderProbleem;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.RouteAnderProbleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.Provincie;
import be.gim.tov.osyris.model.traject.RouteBord;

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
public class MeldingFormBase implements Serializable {

	private static final long serialVersionUID = -8052917916776585407L;
	private static final String GEOMETRY_LAYER_NAME = "geometry";
	private static final Log LOG = LogFactory.getLog(MeldingFormBase.class);
	private static final Integer MAX_COOKIE_AGE = new Integer(30 * 24 * 60 * 60);
	private static final String COOKIE_NAME = "routedokter-token";
	private static final String COOKIE_VALUE_SEPERATOR = ",";

	// VARIABLES
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected ModelRepository modelRepository;
	@Inject
	protected MapFactory mapFactory;
	@Inject
	protected Messages messages;

	protected Melding object;
	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected String probleemType;
	protected Integer knooppuntNummer;
	protected Envelope envelope = null;
	protected String baseLayerName;

	// GETTERS AND SETTERS
	public Melding getMelding() {

		if (object == null) {
			object = createMelding();
		}
		return object;
	}

	public String getName() {
		return "Melding";
	}

	public void setMelding(Melding melding) {
		this.object = melding;
	}

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
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

	public String getBaseLayerName() {
		return baseLayerName;
	}

	public void setBaseLayerName(String baseLayerName) {
		this.baseLayerName = baseLayerName;
	}

	// METHODS
	/**
	 * Initialisatie van een Melding
	 * 
	 * @return
	 */
	public Melding createMelding() {

		Melding melding = null;

		try {
			String name = "Melding";
			melding = (Melding) modelRepository.createObject(
					modelRepository.getModelClass(name),
					(ResourceName) ResourceName.fromString(name));

			// Invullen gegevens melder indien aanwezig in cookie
			Cookie userCookie = getRoutedokterCookie();
			if (userCookie != null) {

				String[] values = userCookie.getValue().split(
						COOKIE_VALUE_SEPERATOR);

				for (String keyValue : Arrays.asList(values)) {
					String key = keyValue.split(":")[0];
					String value = keyValue.split(":")[1];
					if (key.equals("email")) {
						melding.setEmail(value);
					} else if (key.equals("voornaam")) {
						melding.setVoornaam(value);
					} else if (key.equals("naam")) {
						melding.setNaam(value);
					} else if (key.equals("tel")) {
						melding.setTelefoon(value);
					}
				}
			}
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at creation model object.", e);
		}
		return melding;
	}

	/**
	 * Verstuurt confirmatie mail naar de melder en de medewerker TOV
	 * 
	 * @param melding
	 * @throws Exception
	 */
	private void sendConfirmationMail(Melding melding) {

		try {
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("preferences", preferences);
			variables.put("firstname", melding.getVoornaam());
			variables.put("lastname", melding.getNaam());
			variables.put("phone", melding.getTelefoon());
			variables.put("status", melding.getStatus());
			variables.put("problem", melding.getProbleem());

			// Mail naar melder
			mailSender.sendMail(preferences.getNoreplyEmail(),
					Collections.singleton(melding.getEmail()),
					"/META-INF/resources/core/mails/confirmMelding.fmt",
					variables);

			messages.info("Er is een bevestigingsmail gestuurd naar "
					+ object.getEmail() + ".");

			// Ophalen emailadres Medewerker
			// UserProfile profiel = (UserProfile) modelRepository.loadAspect(
			// modelRepository.getModelClass("UserProfile"),
			// modelRepository.loadObject(melding.getMedewerker()));
			// String medewerkerEmail = profiel.getEmail();

			// DEBUG ONLY
			String testEmail = "kristof.spiessens@gim.be";

			// Mail naar Medewerker TOV
			mailSender.sendMail(preferences.getNoreplyEmail(),
					Collections.singleton(testEmail),
					"/META-INF/resources/core/mails/confirmMelding.fmt",
					variables);

			// messages.info("Er is een bevestigingsmail gestuurd de verantwoordelijke TOV "
			// + medewerkerEmail + ".");

		} catch (IOException e) {

			LOG.error("Can not load object.", e);
		} catch (Exception e) {

			LOG.error("Can not send email.", e);
			messages.error("Fout bij het versturen van bevestigingsmail");
		}
	}

	/**
	 * Haalt de kaartconfiguratie van de kleine kaart op
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

			// Retrieve context instance from configuration.
			context = configuration.getContext();

			// Reset layers
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.setFilter(null);
				layer.setHidden(true);
				layer.setSelection(Collections.EMPTY_LIST);
			}

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
	 * Doorzoekt de lagen van de kaartconfiguratie en voert de correcte
	 * kaartoperaties uit
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void searchTraject() throws IOException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		envelope = getEnvelopeProvincie();

		getMelding().setProbleem(null);
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

			else if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
				// Netwerk
				if (trajectType.contains("Segment")) {
					searchNetwerkLayer(layer);
				}
				// Route
				else {
					searchRouteLayer(layer);
					envelope = viewer.getContentExtent(layer);
				}
			}
			// RouteBord
			else if (layer.getLayerId().equalsIgnoreCase(trajectType + "Bord")) {
				searchRouteBordLayer(layer);
			}
			// NetwerkBord
			// Filtering op de borden
			else if (layer.getLayerId().equalsIgnoreCase(
					trajectType.replace("Segment", "") + "Bord")) {
				layer.setHidden(false);
				searchNetwerkBordLayer(layer);
			}
			// WandelKnooppunt
			else if (layer.getLayerId().contains("Knooppunt")
					&& trajectType.contains("WandelNetwerk")) {
				FeatureMapLayer mapLayer = (FeatureMapLayer) context
						.getLayer("wandelNetwerkKnooppunt");
				searchKnooppuntLayer(mapLayer);
			}

			// FietsKnooppunt
			else if (layer.getLayerId().contains("Knooppunt")
					&& trajectType.contains("FietsNetwerk")) {
				FeatureMapLayer mapLayer = (FeatureMapLayer) context
						.getLayer("fietsNetwerkKnooppunt");
				searchKnooppuntLayer(mapLayer);
			}

			// Intekenen Punt
			else if (layer.getLayerId().equalsIgnoreCase(GEOMETRY_LAYER_NAME)) {
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

	/**
	 * Maakt een door de gebruiker gekozen type probleem aan en configureert de
	 * kaart om problemen te kunnen aanduiden
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void createProbleem() throws InstantiationException,
			IllegalAccessException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		if (getMelding().getProbleem() != null) {
			modelRepository.evictObject(getMelding().getProbleem());
		}
		Probleem probleem = null;
		if ("bord".equals(probleemType)) {

			if (trajectType.endsWith("Route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteBordProbleem", null);

			} else if (trajectType.contains("Netwerk")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkBordProbleem", null);
			}

			// Itereren over de lagen en de correcte lagen selecteerbaar zetten
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.set("selectable", false);

				if (layer.getLayerId().equalsIgnoreCase(trajectType + "Bord")) {
					layer.set("selectable", true);
					layer.set("selectionMode", FeatureSelectionMode.SINGLE);
					layer.setSelection(new ArrayList<String>(1));

				} else if (layer.getLayerId().equalsIgnoreCase(
						trajectType.replace("Segment", "") + "Bord")) {
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

			if (trajectType.endsWith("Route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteAnderProbleem", null);

			} else if (trajectType.contains("Netwerk")) {
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
				}

				// Indien NetwerkSegment laag laag op selecteerbaar zetten
				else if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
					layer.set("selectable", true);
					layer.set("selectionMode", FeatureSelectionMode.SINGLE);
					layer.setSelection(new ArrayList<String>(1));

				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		}

		getMelding().setProbleem(probleem);
		viewer.updateContext(null);
	}

	/**
	 * Bewaart een Melding in de databank
	 * 
	 */
	public String saveMelding() {

		try {
			// Save Melding
			if (checkMelding(object)) {
				modelRepository.saveObject(getMelding());

				messages.info("Melding sucessvol verzonden naar TOV.");

				// Email bevestiging sturen naar melder en medewerker
				Beans.getReference(OsyrisModelFunctions.class)
						.sendConfirmationMailMelding(object);
				// sendConfirmationMail(object);

				return reset();
			}

		} catch (IOException e) {
			messages.error("Melding niet verzonden:" + e.getMessage());
			LOG.error("Can not save model object.", e);
		}
		return null;
	}

	/**
	 * Checkt of een melding mag bewaard worden in de databank.
	 * 
	 * @param melding
	 * @return
	 */
	public boolean checkMelding(Melding melding) {

		if (object.getProbleem() instanceof AnderProbleem) {
			if (((AnderProbleem) object.getProbleem()).getGeom() == null) {
				messages.warn("Melding niet verzonden: gelieve eerst een punt aan te duiden op de kaart.");
				return false;
			}
			// Indien RouteAnderProbleem koppel trajectId aan de melding via de
			// routeNaam
			if (object.getProbleem() instanceof RouteAnderProbleem) {
				MapViewer viewer = getViewer();
				MapContext context = viewer.getConfiguration().getContext();

				for (FeatureMapLayer layer : context.getFeatureLayers()) {
					if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
						layer.setFilter(FilterUtils.equal("naam", trajectNaam));
						searchTrajectId(layer);
					}
				}
			}
			// Indien NetwerkAnderProbleem check of segment geselecteerd is
			if (object.getProbleem() instanceof NetwerkAnderProbleem) {
				if (object.getTraject() == null) {
					messages.warn("Melding niet verzonden: gelieve eerst een segment te selecteren.");
					return false;
				}
			}

		}
		// Indien BordProbleem check of bord geselecteerd is
		if (object.getProbleem() instanceof BordProbleem) {
			BordProbleem b = (BordProbleem) object.getProbleem();
			if (b.getBord() == null) {
				messages.warn("Melding niet verzonden: gelieve eerst een bord op de kaart te selecteren.");
				return false;
			}
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

		// Selecteren segment
		if (object.getProbleem() != null
				&& object.getProbleem() instanceof NetwerkAnderProbleem) {

			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");

			String layerId = (String) event.getParams().get("layerId");
			FeatureMapLayer layer = (FeatureMapLayer) getViewer().getContext()
					.getLayer(layerId);

			if (layer.getSelection().size() == 1) {
				object.setTraject(new ResourceKey("Traject", layer
						.getSelection().get(0)));

			} else {
				messages.error("Gelieve precies 1 segment te selecteren.");
				layer.setSelection(new ArrayList<String>(1));
			}
		}

		// Selecteren bord
		else if (object.getProbleem() != null
				&& object.getProbleem() instanceof BordProbleem) {

			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");
			String layerId = (String) event.getParams().get("layerId");
			FeatureMapLayer layer = (FeatureMapLayer) getViewer().getContext()
					.getLayer(layerId);

			if (layer.getSelection().size() == 1) {
				String id = ids.iterator().next();
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
				messages.error("Er zijn meerdere borden geselecteerd. Mogelijk bevinden deze borden zich op dezelfde locatie. Gelieve in dit geval de 'Tonen attributen' knop te gebruiken om uw selectie te verfijnen.");
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
	 */
	@SuppressWarnings("unchecked")
	public void onUpdateFeatures(ControllerEvent event) {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		List<String> ids = (List<String>) event.getParams().get("featureIds");

		GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_NAME);

		// Slechts 1 punt mag ingegeven worden
		if (layer.getGeometries().size() > 1) {
			layer.getGeometries().remove(0);
		}

		// Precies 1 punt koppelen aan een Anderprobleem
		if (layer.getGeometries().size() == 1) {
			if (object.getProbleem() instanceof AnderProbleem) {
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
						getMelding().setTraject(
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
	 * Zoekt de knooppuntIds
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<ResourceIdentifier> getKnooppuntIds() {

		List<ResourceIdentifier> ids = new ArrayList<ResourceIdentifier>();

		try {

			if (knooppuntNummer != null) {
				DefaultQuery query = new DefaultQuery();
				if (trajectType.contains("Fiets")) {
					query.setModelClassName("FietsNetwerkKnooppunt");
				} else if (trajectType.contains("Wandel")) {
					query.setModelClassName("FietsNetwerkKnooppunt");
				}

				query.setFilter(FilterUtils.equal("nummer", knooppuntNummer));
				List<ModelObject> knooppunten = (List<ModelObject>) modelRepository
						.searchObjects(query, true, true);
				for (ModelObject k : knooppunten) {
					ids.add(modelRepository.getResourceKey(k));
				}
			}

		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
		}
		return ids;
	}

	/**
	 * Operaties op de Netwerklagen
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchNetwerkLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.set("selectable", false);
		layer.setSelection(Collections.EMPTY_LIST);
		Filter filter = null;

		if (regio != null && trajectNaam == null && knooppuntNummer == null) {

			filter = FilterUtils.equal("regio", regio);
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

		else if (trajectNaam != null) {

			filter = FilterUtils.equal("naam", trajectNaam);
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
	 * Operaties op de Routelagen
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchRouteLayer(FeatureMapLayer layer) {

		layer.setHidden(false);

		if (regio != null && trajectNaam == null) {
			layer.setFilter(FilterUtils.equal("regio", regio));
		} else if (trajectNaam != null) {
			layer.setFilter(FilterUtils.like("naam", trajectNaam));
		}
	}

	/**
	 * Operaties op de RouteBordlagen
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchRouteBordLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.setSelection(Collections.EMPTY_LIST);

		if (regio != null && trajectNaam == null) {
			layer.setFilter(FilterUtils.equal("regio", regio));
		} else if (trajectNaam != null) {
			layer.setFilter(FilterUtils.like("naam", trajectNaam));
		}

		layer.set("selectionMode", FeatureSelectionMode.SINGLE);
	}

	/**
	 * Operaties op de NetwerkBord lagen
	 * 
	 * @param layer
	 */
	private void searchNetwerkBordLayer(FeatureMapLayer layer) {

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
		if (regio != null) {
			if (knooppuntNummer == null) {
				filter = FilterUtils.and(FilterUtils.equal("regio", regio));
			} else {
				filter = FilterUtils.and(FilterUtils.equal("regio", regio),
						knooppuntFilter);
			}
		}
		if (trajectNaam != null) {
			if (knooppuntNummer == null) {
				filter = FilterUtils
						.and(FilterUtils.equal("naam", trajectNaam),
								knooppuntFilter);
			} else {
				filter = FilterUtils
						.and(FilterUtils.equal("naam", trajectNaam));
			}
		}
		layer.setFilter(filter);
	}

	/**
	 * Operaties op de knooppuntlagen
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchKnooppuntLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.setFilter(null);
		layer.setSelection(Collections.EMPTY_LIST);
		Filter filter = null;

		if (knooppuntNummer == null) {
			if (regio != null) {
				filter = FilterUtils.equal("regio", regio);
				layer.setFilter(filter);
			}

			if (trajectNaam != null) {
				filter = FilterUtils.equal("naam", trajectNaam);
				layer.setFilter(filter);
			}
		}

		else if (knooppuntNummer != null) {
			if (regio != null) {
				filter = FilterUtils.and(FilterUtils.equal("regio", regio),
						(FilterUtils.equal("nummer", knooppuntNummer)));
				layer.setFilter(filter);
			}

			if (trajectNaam != null) {
				filter = FilterUtils.and(
						FilterUtils.equal("naam", trajectNaam),
						(FilterUtils.equal("nummer", knooppuntNummer)));
				layer.setFilter(filter);
			}

			if (trajectNaam == null && regio == null) {
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
	 * Reset het routedokter formulier. Enkel het gegevensinfo panel moet na het
	 * melden van een probleem ingevuld zijn met eerder ingevoerde gegevens.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 */
	public String reset() {

		// Toevoegen cookie
		addRoutedokterCookie();

		// Opnieuw laden form waarden cookie worden ingevuld via createMelding()
		// bij het opstarten van de pagina
		try {

			return "view";
			// String contextPath = FacesContext.getCurrentInstance()
			// .getExternalContext().getRequestContextPath();
			//
			// FacesContext.getCurrentInstance().getExternalContext()
			// .redirect(contextPath + "/web/view/form:MeldingForm");

		} catch (Exception e1) {
			LOG.error("Can not redirect to MeldingForm.", e1);
			return null;
		}
	}

	/**
	 * Toevoegen van cookie voor Routedokter met info melder.
	 * 
	 */
	@RunPrivileged
	private void addRoutedokterCookie() {

		StringBuilder value = new StringBuilder();
		value.append("email:" + object.getEmail());

		// Lege waardes hoeven niet te worden opgeslagen in cookie
		if (StringUtils.isNotEmpty(object.getVoornaam())) {
			value.append(COOKIE_VALUE_SEPERATOR + "voornaam:"
					+ object.getVoornaam());
		}

		if (StringUtils.isNotEmpty(object.getNaam())) {
			value.append(COOKIE_VALUE_SEPERATOR + "naam:" + object.getNaam());
		}

		if (StringUtils.isNotEmpty(object.getTelefoon())) {
			value.append(COOKIE_VALUE_SEPERATOR + "tel:" + object.getTelefoon());
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("maxAge", MAX_COOKIE_AGE);
		properties.put("domain", FacesContext.getCurrentInstance()
				.getExternalContext().getRequestServerName());
		properties.put("path", FacesContext.getCurrentInstance()
				.getExternalContext().getRequestContextPath());

		FacesContext.getCurrentInstance().getExternalContext()
				.addResponseCookie(COOKIE_NAME, value.toString(), properties);
	}

	/**
	 * Ophalen cookie routedokter.
	 * 
	 * @return
	 */
	private Cookie getRoutedokterCookie() {

		FacesContext context = FacesContext.getCurrentInstance();

		if (context != null) {
			Map<String, Object> requestCookieMap = context.getExternalContext()
					.getRequestCookieMap();
			return (Cookie) requestCookieMap.get(COOKIE_NAME);
		} else {
			return null;
		}
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
	 * Reset zoekvelden trajectNaam en knooppuntNummer bij het wijzigen van het
	 * trajectType zoekveld.
	 */
	public void resetChildSearchParameters() {

		setKnooppuntNummer(null);
		setTrajectNaam(null);
	}

	/**
	 * Switchen tussen basislagen voor PetersMeters.
	 * 
	 */
	public void switchBaseLayers() {

		getViewer().setBaseLayerId(baseLayerName);
	}
}
