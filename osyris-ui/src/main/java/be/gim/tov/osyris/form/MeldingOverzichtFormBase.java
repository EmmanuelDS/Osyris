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
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.jsf.component.ComponentUtils;

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
import be.gim.tov.osyris.model.controle.Melding;
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
public class MeldingOverzichtFormBase extends AbstractListForm<Melding> {
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
	protected String trajectNaam;
	protected Date vanDatum;
	protected Date totDatum;

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

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "Melding";
	}

	@Override
	public Query getQuery() {

		if (query == null) {
			query = getDefaultQuery();
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

		// FIXME: Test zoeken naar traject via cascading dropdown boxes
		Query q = new DefaultQuery("Traject");
		q.addFilter(FilterUtils.equal("naam", trajectNaam));
		List<Traject> trajecten;
		try {
			trajecten = (List<Traject>) modelRepository.searchObjects(q, true,
					true);

			if (trajecten.size() == 1) {
				Traject t = trajecten.get(0);
				query.addFilter(FilterUtils.equal("traject",
						modelRepository.getResourceIdentifier(t)));
			}
		} catch (IOException e) {
			LOG.error("Can not find Traject.", e);
		}
		return query;
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
	 * 
	 * @param melding
	 */
	public void showMeldingMapData(Melding melding) {
		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		FeatureMapLayer layer = null;
		String layerId = null;
		try {
			// Get traject
			Traject t = (Traject) modelRepository.loadObject(melding
					.getTraject());

			// Load layer depending on traject type
			layerId = Character.toLowerCase(t.getModelClass().getName()
					.charAt(0))
					+ t.getModelClass().getName().substring(1);
			layer = (FeatureMapLayer) context.getLayer(layerId);
			layer.setHidden(false);
			layer.setFilter(FilterUtils.equal("naam", t.getNaam()));

			// Get borden voor bordproblemen
			if (object.getProbleem() instanceof BordProbleem) {
				Bord b = (Bord) modelRepository
						.loadObject(((BordProbleem) object.getProbleem())
								.getBord());

				layerId = Character.toLowerCase(b.getModelClass().getName()
						.charAt(0))
						+ b.getModelClass().getName().substring(1);
				layer = (FeatureMapLayer) context.getLayer(layerId);
				layer.setHidden(false);
				layer.setFilter(FilterUtils.equal("naam", b.getNaam()));
				List<String> ids = new ArrayList<String>();
				ids.add(b.getId().toString());
				layer.setSelection(ids);
			}
			if (object.getProbleem() instanceof AnderProbleem) {
				AnderProbleem p = (AnderProbleem) object.getProbleem();
				GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) context
						.getLayer("geometry");
				List<Geometry> list = new ArrayList<Geometry>();
				list.add(p.getGeom());
				geomLayer.setGeometries(list);
				geomLayer.setHidden(false);
			}

			context.setBoundingBox(viewer.getContentExtent());
			viewer.updateContext(null);

		} catch (IOException e) {
			LOG.error("Can load object", e);
		}
	}
}