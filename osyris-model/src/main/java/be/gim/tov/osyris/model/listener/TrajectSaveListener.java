package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Traject") })
public class TrajectSaveListener {

	private static final String GEEN_PETER_METER = "Geen PeterMeter toegewezen";

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		Traject traject = (Traject) event.getModelObject();

		// Indien nieuw Traject
		if (traject.getId() == null) {
			// Set correcte id range voor elk subtype
			ModelStore modelStore = modelRepository
					.getModelStore("OsyrisDataStore");
			Long maxId = (Long) modelStore.searchObjects(
					"SELECT MAX(id) FROM " + traject.getModelClass().getName())
					.get(0);
			Long newId = maxId + 1;
			traject.setId(newId);
		}

		if (traject instanceof Route) {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName(traject.getModelClass().getName());
			query.setFilter(FilterUtils.equal("naam", traject.getNaam()));
			Integer count = modelRepository.countObjects(query, false);
			if (count > 0) {
				String message = "De naam van een route moet uniek zijn.";
				Beans.getReference(Messages.class).error(message);
				throw new IOException(message);
			}
		}

		// Indien geen PeterMeter opgegeven veld op nulll zetten
		if (traject.getPeterMeter1() != null
				&& traject.getPeterMeter1().equals(GEEN_PETER_METER)) {
			traject.setPeterMeter1(null);
		}
		if (traject.getPeterMeter2() != null
				&& traject.getPeterMeter2().equals(GEEN_PETER_METER)) {
			traject.setPeterMeter2(null);
		}
		if (traject.getPeterMeter3() != null
				&& traject.getPeterMeter3().equals(GEEN_PETER_METER)) {
			traject.setPeterMeter3(null);
		}

		// Set lengte traject in km
		if (traject.getGeom() != null) {
			traject.setLengte((float) (traject.getGeom().getLength() / 1000.0));
		}
	}
}
