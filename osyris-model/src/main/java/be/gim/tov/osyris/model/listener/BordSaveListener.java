package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;

import be.gim.commons.bean.Beans;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.Regio;

import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Bord") })
public class BordSaveListener {

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		Bord bord = (Bord) event.getModelObject();

		// Indien nieuw Bord
		if (bord.getId() == null) {

			// Set correcte id range voor elk subtype
			ModelStore modelStore = modelRepository
					.getModelStore("OsyrisDataStore");
			Long maxId = (Long) modelStore.searchObjects(
					"SELECT MAX(id) FROM " + bord.getModelClass().getName())
					.get(0);
			Long newId = maxId + 1;
			bord.setId(newId);
			// bord.setLabelId(newId.toString());
		}
		// Set LabelId for SLD
		bord.setLabelId(bord.getId().toString());

		// Automatically set X Y coordinates indien niet aanwezig
		if (bord.getGeom() != null && bord.getGeom() instanceof Point) {

			Point p = (Point) bord.getGeom();
			bord.setX(p.getX());
			bord.setY(p.getY());
		}

		// Automatically set Regio
		if (bord.getRegio() == null) {

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForBord(bord);
			bord.setRegio(modelRepository.getResourceKey(regio));
		}

		// Automatically set Gemeente
		if (bord.getGemeente() == null) {

			String gemeente = Beans.getReference(OsyrisModelFunctions.class)
					.getGemeenteForBord(bord);
			bord.setGemeente(gemeente);
		}
	}
}
