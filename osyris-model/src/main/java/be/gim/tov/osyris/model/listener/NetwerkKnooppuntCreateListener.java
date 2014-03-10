package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.DefaultQuery;

import be.gim.commons.collections.CollectionUtils;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.Regio;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "create", _for = "NetwerkKnooppunt") })
public class NetwerkKnooppuntCreateListener {

	private static final Log log = LogFactory
			.getLog(NetwerkKnooppuntCreateListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) event.getModelObject();

		// Indien regio bij het aanmaken leeg is automatisch regio zoeken
		Geometry geom = knooppunt.getGeom();
		if (knooppunt.getRegio() == null && geom != null) {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");
			query.addFilter(FilterUtils.intersects("geom", geom));

			Regio regio = (Regio) CollectionUtils.first(modelRepository
					.searchObjects(query, false, false));
			if (regio != null) {
				knooppunt
						.setRegio(modelRepository.getResourceIdentifier(regio));
			}

		}

		// Indien naam bij aanmaken leeg is automatisch invullen naam regio
		if (knooppunt.getNaam() == null || knooppunt.getNaam().isEmpty()) {
			Regio regio = (Regio) modelRepository.loadObject(knooppunt
					.getRegio());
			if (regio != null) {
				knooppunt.setNaam(regio.getNaam());
			}
		}

		// Automatically set X Y coordinates indien niet aanwezig
		if (geom != null && geom instanceof Point) {
			Point p = (Point) geom;
			knooppunt.setX(p.getX());
			knooppunt.setY(p.getY());
		}
	}
}
