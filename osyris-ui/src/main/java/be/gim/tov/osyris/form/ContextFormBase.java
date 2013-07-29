package be.gim.tov.osyris.form;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.security.Identity;

import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapLayer;
import be.gim.specto.core.layer.DefaultMapGroup;
import be.gim.specto.ui.component.UIMapViewer;
import be.gim.tov.osyris.model.traject.Provincie;
import be.gim.tov.osyris.model.user.MedewerkerProfiel;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Deze klasse definieert het gedrag en acties van het Trajecten mapcontext
 * formulier.
 * 
 * @author kristof
 * 
 */
public class ContextFormBase {

	private static final Log LOG = LogFactory.getLog(ContextFormBase.class);

	// VARIABLES
	@Inject
	private ModelRepository modelRepository;
	@Inject
	private Identity identity;
	private UIMapViewer viewer;

	public UIMapViewer getViewer() {
		return viewer;
	}

	/**
	 * Setten mapviewer waar bepaalde lagen voor een bepaalde groep niet
	 * editeerbaar zijn.
	 * 
	 * @param viewer
	 */
	public void setViewer(UIMapViewer viewer) {
		this.viewer = viewer;
		try {
			MedewerkerProfiel profiel = null;

			List<FeatureMapLayer> mapLayers = getViewer().getContext()
					.getFeatureLayers();

			// Medewerker
			if (identity.inGroup("Medewerker", "CUSTOM")) {
				User medewerker = (User) modelRepository
						.loadObject(new ResourceName("user", identity.getUser()
								.getId()));
				profiel = (MedewerkerProfiel) modelRepository.loadAspect(
						modelRepository.getModelClass("MedewerkerProfiel"),
						medewerker);

				for (FeatureMapLayer mapLayer : mapLayers) {
					mapLayer.set("editable", false);
				}
				// Get the MapGroups corresponding with the trajectType
				for (String type : profiel.getTrajectType()) {
					DefaultMapGroup group = (DefaultMapGroup) getViewer()
							.getContext().getLayer((type.toLowerCase()));
					for (MapLayer layer : group.getLayers()) {
						// TODO: Lussen should not be editable?
						layer.set("editable", true);
					}
				}
			}

			// Routedokter or admin
			if (identity.inGroup("Routedokter", "CUSTOM")
					|| identity.inGroup("admin", "CUSTOM")) {
				// Every layer is editable except regio and gemeente
				for (FeatureMapLayer mapLayer : mapLayers) {
					mapLayer.set("editable", true);
					if (mapLayer.getLayerId().equals("regio")
							|| mapLayer.getLayerId().equals("gemeente")
							|| mapLayer.getLayerId().equals("provincie")) {
						mapLayer.set("editable", false);
					}
				}
			}

			// Start configuratie zoomt naar Provincie OVL
			Provincie provincie = (Provincie) modelRepository
					.getUniqueResult(modelRepository.searchObjects(
							new DefaultQuery("Provincie"), true, true));
			Envelope envelope = GeometryUtils.getEnvelope(provincie.getGeom());
			viewer.setCurrentExtent(envelope);

		} catch (Exception e) {
			LOG.error("Can not set MapViewer.", e);
		}
	}
}
