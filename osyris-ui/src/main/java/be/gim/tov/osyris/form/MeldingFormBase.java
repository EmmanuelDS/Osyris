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
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.UserProfile;
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

	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected String probleemType;

	protected Melding object;

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

	public void searchTraject() {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		getMelding().setProbleem(null);
		probleemType = StringUtils.EMPTY;

		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			if (layer.getLayerId().equalsIgnoreCase(trajectType)) {

				if (trajectType.contains("Segment")) {
					layer.setHidden(false);
					layer.setFilter(FilterUtils.equal("regio", regio));

					if (trajectNaam != null) {
						layer.setFilter(FilterUtils.equal("naam", trajectNaam));
					}
				} else {
					layer.setHidden(false);
					layer.setFilter(FilterUtils.equal("naam", trajectNaam));
				}

			} else if (layer.getLayerId()
					.equalsIgnoreCase(trajectType + "Bord")) {
				layer.setHidden(false);
				layer.setFilter(FilterUtils.equal("naam", trajectNaam));
			}

			else if (layer.getLayerId().equalsIgnoreCase(
					trajectType.replace("Segment", "") + "Bord")) {
				layer.setHidden(false);
				layer.setFilter(FilterUtils.equal("regio", regio));
				if (trajectNaam != null) {
					layer.setFilter(FilterUtils.equal("naam", trajectNaam));
				}

			} else if (layer.getLayerId().equalsIgnoreCase("geometry")) {
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

			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				if (layer.getLayerId().equalsIgnoreCase(trajectType + "Bord")) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
				} else if (layer.getLayerId().equalsIgnoreCase(
						trajectType.replace("Segment", "") + "Bord")
						|| trajectType.contains("Netwerk")) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));
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

	public void saveMelding() {

		try {
			// GET TRAJECT ID FOR ROUTE VIA TRAJECTNAAM
			if (trajectType.contains("Route")) {
				MapViewer viewer = getViewer();
				MapContext context = viewer.getConfiguration().getContext();

				for (FeatureMapLayer layer : context.getFeatureLayers()) {
					if (layer.getLayerId().equalsIgnoreCase(trajectType)) {
						layer.setFilter(FilterUtils.equal("naam", trajectNaam));
						// In case of route get the traject id via trajectnaam
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
			}

			else {
				messages.error("Melding niet verzonden: Er is geen segment geselecteerd.");
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
			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");
			if (ids.size() > 0) {
				String id = ids.iterator().next();
				((BordProbleem) object.getProbleem()).setBord(new ResourceKey(
						"Bord", id));
			}
		}
	}

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

	@SuppressWarnings("unchecked")
	public void onDeleteFeatures(ControllerEvent event) {

		List<String> ids = (List<String>) event.getParams().get("featureIds");
		if (ids.size() > 0) {
			((AnderProbleem) object.getProbleem()).setGeom(null);
		}
	}

	// SEARCH TRAJECT ID FOR ROUTES: GET ID FROM TRAJECTNAAM
	/**
	 * Filters the layer based on trajectNaam and sets the TrajectID via
	 * trajectNaam
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
}
