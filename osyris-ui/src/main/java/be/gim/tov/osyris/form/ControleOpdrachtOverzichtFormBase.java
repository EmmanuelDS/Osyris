package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.jsf.component.ComponentUtils;
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
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;
import be.gim.tov.osyris.model.traject.Bord;

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

	protected String controleOpdrachtType;
	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected Date vanDatum;
	protected Date totDatum;
	protected List<Bord> bewegwijzering;

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
				query.addFilter(FilterUtils.notEqual("status",
						ControleOpdrachtStatus.GEVALIDEERD));
				query.addFilter(FilterUtils.notEqual("status",
						ControleOpdrachtStatus.GEANNULEERD));
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

			// Kan dit op een betere manier gebeuren?
			if (vanDatum != null && totDatum != null) {
				List<ControleOpdracht> list = (List<ControleOpdracht>) modelRepository
						.searchObjects(getQuery(), true, true, true);
				List<ControleOpdracht> filteredList = new ArrayList<ControleOpdracht>();
				for (ControleOpdracht c : list) {
					if (c.getDatumLaatsteWijziging().before(totDatum)
							&& c.getDatumLaatsteWijziging().after(vanDatum)) {
						filteredList.add(c);
					}
				}
				results = filteredList;
			}

			else {
				results = (List<ControleOpdracht>) modelRepository
						.searchObjects(getQuery(), true, true, true);
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
					"geometry", null, Point.class, null, true, "single", null,
					null);

			return configuration;
		}
		return null;
	}

	/**
	 * Zoekt de borden die bij de gekozen route behoren
	 * 
	 */
	public void getBewegwijzeringVerslag() {

		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Bord");
			query.addFilter(FilterUtils.equal("naam", trajectNaam));
			bewegwijzering = (List<Bord>) modelRepository.searchObjects(query,
					true, true);
		} catch (IOException e) {
			LOG.error("", e);
			bewegwijzering = null;
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
}
