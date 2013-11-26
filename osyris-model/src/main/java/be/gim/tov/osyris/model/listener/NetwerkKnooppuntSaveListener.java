package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;
import org.conscientia.core.search.DefaultQuery;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.Regio;

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

			// Indien regio bij het aanmaken leeg is automatisch regio zoeken
			if (knooppunt.getRegio() == null) {

				DefaultQuery query = new DefaultQuery();
				query.setModelClassName("Regio");
				query.addFilter(FilterUtils.intersects("geom",
						knooppunt.getGeom()));

				Regio regio = (Regio) modelRepository.searchObjects(query,
						false, false).get(0);
				knooppunt
						.setRegio(modelRepository.getResourceIdentifier(regio));

			}

			// Indien naam bij aanmaken leeg is automatisch invullen naam regio
			if (knooppunt.getNaam() == null || knooppunt.getNaam().isEmpty()) {

				Regio regio = (Regio) modelRepository.loadObject(knooppunt
						.getRegio());
				knooppunt.setNaam(regio.getNaam());

			}
		}

		// Automatically set X Y coordinates indien niet aanwezig
		if (knooppunt.getGeom() != null && knooppunt.getGeom() instanceof Point) {
			Point p = (Point) knooppunt.getGeom();
			knooppunt.setX(p.getX());
			knooppunt.setY(p.getY());
		}
	}
}
