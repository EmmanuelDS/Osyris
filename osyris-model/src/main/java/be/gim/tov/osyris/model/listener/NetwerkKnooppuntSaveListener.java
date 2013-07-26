package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;

import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;

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

		// Automatically set X Y coordinates indien niet aanwezig
		if (knooppunt.getGeom() != null && knooppunt.getGeom() instanceof Point) {
			Point p = (Point) knooppunt.getGeom();
			knooppunt.setX(p.getX());
			knooppunt.setY(p.getY());
		}
	}
}