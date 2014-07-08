package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;
import org.conscientia.core.search.DefaultQuery;

import be.gim.commons.bean.Beans;
import be.gim.commons.collections.CollectionUtils;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.WandelNetwerkKnooppunt;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "NetwerkKnooppunt") })
public class NetwerkKnooppuntSaveListener {

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) event.getModelObject();

		// Indien nieuw NetwerkKnooppunt
		if (knooppunt.getId() == null) {
			// Set correcte id range voor elk subtype
			ModelStore modelStore = modelRepository
					.getModelStore("OsyrisDataStore");
			Long maxId = (Long) modelStore.searchObjects(
					"SELECT MAX(id) FROM "
							+ knooppunt.getModelClass().getName()).get(0);
			Long newId = maxId + 1;
			knooppunt.setId(newId);
		}

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

			String naam = Beans.getReference(OsyrisModelFunctions.class)
					.getNaamForWandelNetwerkKnooppunt(knooppunt);

			// Indien WNW knooppunt naam van het dichtsbijzijnde WNW segment,
			// indien geen segment naam regio
			if (knooppunt instanceof WandelNetwerkKnooppunt && naam != null) {
				knooppunt.setNaam(naam);
			}
			// Naam regio
			else {
				Regio regio = (Regio) modelRepository.loadObject(knooppunt
						.getRegio());
				if (regio != null) {
					knooppunt.setNaam(regio.getNaam());
				}
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
