package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;

import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkSegment;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "NetwerkSegment") })
public class NetwerkSegmentSaveListener {

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkSegment segment = (NetwerkSegment) event.getModelObject();

		// Indien nieuw segment
		if (segment.getId() == null) {
			// Set correcte id range voor elk subtype
			ModelStore modelStore = modelRepository
					.getModelStore("OsyrisDataStore");
			Long maxId = (Long) modelStore.searchObjects(
					"SELECT MAX(id) FROM " + segment.getModelClass().getName())
					.get(0);
			Long newId = maxId + 1;
			segment.setId(newId);
		}

		// Automatisch setten VanKpNr en NaarKpNr
		if (segment.getVanKnooppunt() != null
				&& StringUtils.isNotBlank(segment.getVanKnooppunt().toString())) {
			NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
					.loadObject(segment.getVanKnooppunt());
			segment.setVanKpNr(knooppunt.getNummer());
		}

		if (segment.getNaarKnooppunt() != null
				&& StringUtils
						.isNotBlank(segment.getNaarKnooppunt().toString())) {
			NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
					.loadObject(segment.getNaarKnooppunt());
			segment.setNaarKpNr(knooppunt.getNummer());
		}
	}
}
