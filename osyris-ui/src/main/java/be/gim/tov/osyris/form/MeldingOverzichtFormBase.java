package be.gim.tov.osyris.form;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.conscientia.jsf.component.ComponentUtils;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.api.context.RasterMapLayer;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.Traject;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class MeldingOverzichtFormBase extends AbstractListForm<Melding>
		implements Serializable {
	private static final long serialVersionUID = -3077755833706449795L;

	private static final Log LOG = LogFactory
			.getLog(MeldingOverzichtFormBase.class);

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected MapFactory mapFactory;
	protected ResourceIdentifier regio;
	protected String trajectType;
	protected Date vanDatum;
	protected Date totDatum;
	protected ResourceIdentifier trajectId;
	protected String baseLayerName;

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

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
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

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "Melding";
	}

	public String getProbleemType(Probleem probleem) {

		if (probleem instanceof BordProbleem) {
			return "Bordprobleem";
		} else {
			return "Ander probleem";
		}
	}

	@Override
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		if (trajectType != null && trajectId != null) {
			query.addFilter(FilterUtils.equal("traject", trajectId));
		}

		else {

			if (trajectType != null) {
				query.addFilter(FilterUtils.equal("trajectType", trajectType));
			}

			if (regio != null) {
				query.addFilter(FilterUtils.equal("regioId", regio));
			}
		}

		if (vanDatum != null && totDatum != null) {

			query.addFilter(FilterUtils.and(FilterUtils.lessOrEqual(
					"datumLaatsteWijziging", totDatum), FilterUtils
					.greaterOrEqual("datumLaatsteWijziging", vanDatum)));

		}

		if (identity.inGroup("Routedokter", "CUSTOM")) {
			return query;
		}
		if (identity.inGroup("Medewerker", "CUSTOM")) {
			try {
				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceIdentifier(userRepository.loadUser(identity
								.getUser().getId()))));
			} catch (IOException e) {
				LOG.error("Can not load user.", e);
			}
		}

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

			context = configuration.getContext();

			// Reset layers
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.setFilter(null);
				layer.setHidden(true);
				layer.set("selectable", false);
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

			Melding melding = getObject();

			// Get traject
			Traject traject = (Traject) modelRepository.loadObject(melding
					.getTraject());

			// Load layer depending on traject type
			FeatureMapLayer trajectLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(traject.getModelClass()
							.getName()));
			if (trajectLayer != null) {
				trajectLayer.setHidden(false);
				trajectLayer.setFilter(FilterUtils.equal("naam",
						traject.getNaam()));
			}

			// Tonen netwerkKnooppunten indien Segment
			if (traject instanceof NetwerkSegment) {

				NetwerkSegment segment = (NetwerkSegment) traject;
				FeatureMapLayer knooppuntLayer = (FeatureMapLayer) context
						.getLayer(LabelUtils.lowerCamelCase(traject
								.getModelClass().getName()
								.replace("Segment", "Knooppunt")));

				if (knooppuntLayer != null) {

					List<String> ids = new ArrayList<String>();

					ids.add(segment.getVanKnooppunt().getIdPart());
					ids.add(segment.getNaarKnooppunt().getIdPart());

					knooppuntLayer.setHidden(false);
					knooppuntLayer.setFilter(FilterUtils.and(FilterUtils
							.id(ids)));

					ids = null;
				}
			}

			Probleem probleem = object.getProbleem();

			if (probleem instanceof BordProbleem) {
				// Bord Probleem
				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) probleem).getBord());

				Envelope envelope = null;

				// Checken of Bord niet verwijderd is
				if (bord != null) {
					FeatureMapLayer probleemLayer = (FeatureMapLayer) context
							.getLayer(LabelUtils.lowerCamelCase(bord
									.getModelClass().getName()));
					if (probleemLayer != null) {
						probleemLayer.setHidden(false);
						probleemLayer.setFilter(FilterUtils.equal("naam",
								bord.getNaam()));

						probleemLayer.setSelection(Collections
								.singletonList(bord.getId().toString()));

						envelope = GeometryUtils.getEnvelope(bord.getGeom());
						GeometryUtils.expandEnvelope(envelope, 0.04,
								context.getMaxBoundingBox());

					} else {
						envelope = GeometryUtils.getEnvelope(traject.getGeom());
					}
					context.setBoundingBox(envelope);
				}
			} else if (probleem instanceof AnderProbleem) {
				// Ander Probleem
				AnderProbleem anderProbleem = (AnderProbleem) probleem;

				// Bepalen bordLayer voor Routes of Segmenten
				String bordLayerName = null;

				if (traject instanceof Route) {
					bordLayerName = LabelUtils.lowerCamelCase(traject
							.getModelClass().getName().concat("Bord"));
				}

				if (traject instanceof NetwerkSegment) {
					bordLayerName = LabelUtils.lowerCamelCase(traject
							.getModelClass().getName()
							.replace("Segment", "Bord"));
				}
				// Tonen borden
				FeatureMapLayer bordLayer = (FeatureMapLayer) context
						.getLayer(bordLayerName);

				if (bordLayer != null) {
					bordLayer.setHidden(false);
					bordLayer.setFilter(FilterUtils.equal("naam",
							traject.getNaam()));
				}

				GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) mapFactory
						.createGeometryLayer(configuration.getContext(),
								"geometry", null, Point.class, null, true,
								"single", null, null);
				geomLayer.setHidden(false);

				geomLayer.setGeometries(Collections.singletonList(anderProbleem
						.getGeom()));

				Envelope envelope = GeometryUtils.getEnvelope(anderProbleem
						.getGeom());
				GeometryUtils.expandEnvelope(envelope, 0.04,
						context.getMaxBoundingBox());
				context.setBoundingBox(envelope);
			}
			return configuration;
		}
		return null;
	}

	@Override
	public void save() {
		try {
			modelRepository.saveObject(object);
			messages.info("Melding succesvol gevalideerd.");
			clear();
			search();
		} catch (IOException e) {
			messages.error("Fout bij het valideren van de melding: "
					+ e.getMessage());
			LOG.error("Can not save model object.", e);
		}
	}

	@Override
	public void delete() {
		try {

			// Loskoppelen van het probleem aan een melding.
			// Probleem kan nog wel gekoppeld zijn aan een Werkopdracht.
			object.setProbleem(null);
			modelRepository.saveObject(object);

			modelRepository.deleteObject(object);
			messages.info("Melding succesvol verwijderd.");
			clear();
			search();
		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van melding: "
					+ e.getMessage());
			LOG.error("Can not delete model object.", e);
		}
	}

	/**
	 * Check of een probleem van het type BordProbleem is.
	 * 
	 * @param probleem
	 * @return
	 */
	public boolean isBordProbleem(Probleem probleem) {

		if (probleem instanceof BordProbleem) {
			return true;
		}

		else {
			return false;
		}
	}

	/**
	 * Switchen tussen basislagen voor PetersMeters.
	 * 
	 */
	public void switchBaseLayers() {

		getViewer().setBaseLayerId(baseLayerName);
	}
}