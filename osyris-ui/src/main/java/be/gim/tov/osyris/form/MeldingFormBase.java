package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.UserProfile;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.jsf.component.ComponentUtils;
import org.conscientia.jsf.event.ControllerEvent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.jboss.seam.international.status.Messages;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import be.gim.commons.decoder.api.DecoderException;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.commons.resource.ResourceName;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.NetwerkAnderProbleem;
import be.gim.tov.osyris.model.controle.NetwerkBordProbleem;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;

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

	private static final Log LOG = LogFactory.getLog(MeldingFormBase.class);

	// VARIABLES
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected Messages messages;
	@Inject
	protected ModelRepository modelRepository;
	@Inject
	protected MapFactory mapFactory;

	protected Melding object;
	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected String probleemType;
	protected int knooppuntNummer;

	// GETTERS AND SETTERS
	public Melding getMelding() {

		if (object == null) {
			object = createMelding();
		}
		return object;
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

	public int getKnooppuntNummer() {
		return knooppuntNummer;
	}

	public void setKnooppuntNummer(int knooppuntNummer) {
		this.knooppuntNummer = knooppuntNummer;
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
	private void sendConfirmationMail(Melding melding) throws Exception {

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);
		variables.put("firstname", melding.getVoornaam());
		variables.put("lastname", melding.getNaam());
		variables.put("phone", melding.getTelefoon());
		variables.put("status", melding.getStatus());
		variables.put("problem", melding.getProbleem());

		// Send mail to Melder
		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(melding.getEmail()),
				"/META-INF/resources/core/mails/confirmMelding.fmt", variables);

		// Get email Medewerker
		UserProfile profiel = (UserProfile) modelRepository.loadAspect(
				modelRepository.getModelClass("UserProfile"),
				modelRepository.loadObject(melding.getMedewerker()));
		String medewerkerEmail = profiel.getEmail();

		// Only for testing
		String testEmail = "kristof.spiessens@gim.be";
		// Send mail to Medewerker TOV
		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(testEmail),
				"/META-INF/resources/core/mails/confirmMelding.fmt", variables);
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

			mapFactory.createGeometryLayer(configuration.getContext(),
					"geometry", null, Point.class, null, true, "single", null,
					null);

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

		getMelding().setProbleem(null);
		probleemType = StringUtils.EMPTY;

		// Itereren over de lagen en de correcte operaties uitvoeren
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
			// NetwerkBord
			// Filtering op de borden
			else if (layer.getLayerId().equalsIgnoreCase(
					trajectType.replace("Segment", "") + "Bord")) {
				layer.setHidden(false);
				searchNetwerkBordLayer(layer, viewer);
			}
			// WandelKnooppunt
			else if (layer.getLayerId().contains("Knooppunt")
					&& trajectType.contains("WandelNetwerk")) {

				FeatureMapLayer mapLayer = (FeatureMapLayer) context
						.getLayer("wandelNetwerkKnooppunt");
				searchKnooppuntLayer(mapLayer, viewer);
			}

			// FietsKnooppunt
			else if (layer.getLayerId().contains("Knooppunt")
					&& trajectType.contains("FietsNetwerk")) {

				FeatureMapLayer mapLayer = (FeatureMapLayer) context
						.getLayer("fietsNetwerkKnooppunt");
				searchKnooppuntLayer(mapLayer, viewer);
			}

			else if (layer.getLayerId().equalsIgnoreCase("geometry")) {
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

		context.setBoundingBox(viewer.getContentExtent());
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

		Probleem probleem = null;
		if ("bord".equals(probleemType)) {
			if (trajectType.endsWith("Route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteBordProbleem", null);
			} else if (trajectType.contains("Netwerk")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkBordProbleem", null);
			}

			// Itereren over de lagen en de correctie lagen selecteerbaar zetten
			for (FeatureMapLayer layer : context.getFeatureLayers()) {

				if (layer.getLayerId().equalsIgnoreCase(trajectType + "Bord")) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
				} else if (layer.getLayerId().equalsIgnoreCase(
						trajectType.replace("Segment", "") + "Bord")) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
				}

				else if (layer.getLayerId().contains("Knooppunt")) {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}

				else if (layer.getLayerId().equalsIgnoreCase("geometry")) {
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
				if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
				} else if (layer.getLayerId().equalsIgnoreCase("geometry")) {
					layer.setHidden(false);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(new ArrayList<Geometry>(1));
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
	public void saveMelding() {

		try {
			// Indien route achterhalen van de TrajectId via de routeNaam
			if (trajectType.contains("Route")) {
				MapViewer viewer = getViewer();
				MapContext context = viewer.getConfiguration().getContext();

				for (FeatureMapLayer layer : context.getFeatureLayers()) {
					if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
						layer.setFilter(FilterUtils.equal("naam", trajectNaam));
						searchTrajectId(layer);
					}
				}
			}
			// Save Melding
			if (object.getTraject() != null) {
				modelRepository.saveObject(getMelding());
				messages.info("Melding sucessvol verzonden naar TOV.");

				// Email bevestiging sturen naar melder en medewerker
				// sendConfirmationMail(object);
				messages.info("Er is een bevestigingsmail gestuurd naar "
						+ object.getEmail() + ".");

				object = createMelding();
				getMelding().setProbleem(null);
				reset();
			}

			else if (object.getProbleem() instanceof BordProbleem) {
				BordProbleem b = (BordProbleem) object.getProbleem();
				if (b.getBord() == null) {
					messages.error("Melding niet verzonden: Geen bord geselecteerd.");
				}
			}

		} catch (IOException e) {
			LOG.error("Can not save model object.", e);
			messages.error("Melding niet verzonden.");
		} catch (Exception e) {
			LOG.error("Can not send mail.", e);
			messages.error("Bevestigingsmail niet verstuurd.");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Event bij het selecteren van features op de kaart
	 * 
	 * @param event
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void onSelectFeatures(ControllerEvent event) throws IOException {

		// Get selected segment
		if (object.getProbleem() instanceof NetwerkAnderProbleem) {
			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");
			if (ids.size() > 0) {
				String id = ids.iterator().next();
				object.setTraject(new ResourceKey("Traject", id));

			}
		}

		// FIXME: Get selected bord and segment
		else if (object.getProbleem() instanceof NetwerkBordProbleem) {
			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");
			if (ids.size() > 0) {
				String id = ids.iterator().next();

				if ((Traject) modelRepository.loadObject(new ResourceKey(
						"Traject", id)) != null) {
					object.setTraject(new ResourceKey("Traject", id));
				}

				if ((Bord) modelRepository.loadObject(new ResourceKey("Bord",
						id)) != null) {
					((BordProbleem) object.getProbleem())
							.setBord(new ResourceKey("Bord", id));
				}
			}
		}

		// Get selected bord
		else {
			if (object.getProbleem() != null) {
				List<String> ids = (List<String>) event.getParams().get(
						"featureIds");
				if (ids.size() > 0) {
					String id = ids.iterator().next();
					((BordProbleem) object.getProbleem())
							.setBord(new ResourceKey("Bord", id));

					// Set traject behorend bij routebord
					RouteBord selectedBord = (RouteBord) modelRepository
							.loadObject(new ResourceKey("Bord", id));
					object.setTraject(selectedBord.getRoute());
				}
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
			layer.setSelection(null);
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
		if (ids.size() > 0) {
			GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) context
					.getLayer("geometry");
			((AnderProbleem) object.getProbleem()).setGeom(layer
					.getGeometries().iterator().next());
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
	public List<ResourceIdentifier> getKnooppuntIds() {
		List<ResourceIdentifier> ids = new ArrayList<ResourceIdentifier>();
		try {
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

		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
		}
		return ids;
	}

	/**
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchNetwerkLayer(FeatureMapLayer layer, MapViewer viewer) {
		layer.setHidden(false);
		// layer.setFilter(FilterUtils.equal("regio", regio));
		if (regio != null && trajectNaam == null) {
			viewer.updateCurrentExtent(viewer.getFeatureExtent(layer,
					FilterUtils.equal("regio", regio)));
		}

		else if (trajectNaam != null) {
			// layer.setFilter(FilterUtils.equal("naam", trajectNaam));
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
		if (regio != null && trajectNaam == null) {
			layer.setFilter(FilterUtils.equal("regio", regio));
		} else if (trajectNaam != null) {
			layer.setFilter(FilterUtils.like("naam", trajectNaam));
		}
	}

	/**
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchNetwerkBordLayer(FeatureMapLayer layer, MapViewer viewer) {
		layer.setHidden(false);
		layer.setFilter(FilterUtils.equal("regio", regio));
		// Voorlopig Filter de borden die verbonden zijn met het opgegeven
		// knooppunt nummer
		if (knooppuntNummer != 0 && regio != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("regio", regio),
					FilterUtils.or(FilterUtils.equal("kpnr0", knooppuntNummer),
							FilterUtils.equal("kpnr1", knooppuntNummer),
							FilterUtils.equal("kpnr2", knooppuntNummer),
							FilterUtils.equal("kpnr3", knooppuntNummer))));
		}

		if (knooppuntNummer != 0 && trajectNaam != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("naam",
					trajectNaam), FilterUtils.or(
					FilterUtils.equal("kpnr0", knooppuntNummer),
					FilterUtils.equal("kpnr1", knooppuntNummer),
					FilterUtils.equal("kpnr2", knooppuntNummer),
					FilterUtils.equal("kpnr3", knooppuntNummer))));

		}

		if (knooppuntNummer != 0) {
			layer.setFilter(FilterUtils.or(
					FilterUtils.equal("kpnr0", knooppuntNummer),
					FilterUtils.equal("kpnr1", knooppuntNummer),
					FilterUtils.equal("kpnr2", knooppuntNummer),
					FilterUtils.equal("kpnr3", knooppuntNummer)));
		}

		if (knooppuntNummer == 0 && trajectNaam != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("naam",
					trajectNaam), FilterUtils.or(
					FilterUtils.equal("kpnr0", knooppuntNummer),
					FilterUtils.equal("kpnr1", knooppuntNummer),
					FilterUtils.equal("kpnr2", knooppuntNummer),
					FilterUtils.equal("kpnr3", knooppuntNummer))));

		}

		if (knooppuntNummer == 0 && regio != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("regio", regio),
					FilterUtils.or(FilterUtils.equal("kpnr0", knooppuntNummer),
							FilterUtils.equal("kpnr1", knooppuntNummer),
							FilterUtils.equal("kpnr2", knooppuntNummer),
							FilterUtils.equal("kpnr3", knooppuntNummer))));
		}
	}

	/**
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchKnooppuntLayer(FeatureMapLayer layer, MapViewer viewer) {
		layer.setHidden(false);
		layer.setFilter(null);
		if (knooppuntNummer == 0 && regio != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("regio", regio)));
		}

		if (knooppuntNummer == 0 && trajectNaam != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("naam",
					trajectNaam)));
		}
		if (knooppuntNummer != 0 && regio != null) {
			layer.setFilter(FilterUtils.and(FilterUtils.equal("regio", regio),
					(FilterUtils.equal("nummer", knooppuntNummer))));
		}

		if (knooppuntNummer != 0 && trajectNaam != null) {
			layer.setFilter(FilterUtils.and(
					FilterUtils.equal("naam", trajectNaam),
					(FilterUtils.equal("nummer", knooppuntNummer))));
		}
	}

	public void reset() {
		setTrajectType(null);
		setRegio(null);
		setTrajectNaam(null);
		String email = object.getEmail();
		object = null;
		object = createMelding();
		object.setEmail(email);

		// Reset map
		MapViewer viewer = getViewer();
		try {
			viewer.resetMapContext();
		} catch (DecoderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error("Can not reset map.", e);
		}
	}
}
