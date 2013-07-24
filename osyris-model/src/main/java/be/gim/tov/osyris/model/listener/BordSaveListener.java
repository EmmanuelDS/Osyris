package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;

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
		}

		// Set route voor RouteBord indien niet aanwezig
		if (bord instanceof RouteBord && ((RouteBord) bord).getRoute() == null) {
			if (bord.getNaam() != null) {
				QueryBuilder builder = new QueryBuilder("Traject");
				builder.addFilter(FilterUtils.equal("naam", bord.getNaam()));
				builder.maxResults(1);
				List<Traject> result = (List<Traject>) modelRepository
						.searchObjects(builder.build(), false, false);
				Traject route = result.get(0);
				((RouteBord) bord).setRoute(modelRepository
						.getResourceIdentifier(route));
			}
		}

		// Automatically set X Y coordinates indien niet aanwezig
		if (bord.getGeom() != null && bord.getGeom() instanceof Point) {
			Point p = (Point) bord.getGeom();
			bord.setX(p.getX());
			bord.setY(p.getY());
		}
		// TODO: bordVolgorde sequentie en BordLabels zetten?
	}
}
