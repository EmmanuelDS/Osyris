package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;

import be.gim.commons.label.LabelUtils;
import be.gim.tov.osyris.model.traject.NetwerkLus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "create", _for = "NetwerkLus") })
public class NetwerkLusCreateListener {

	private static final Log log = LogFactory
			.getLog(NetwerkLusCreateListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkLus lus = (NetwerkLus) event.getModelObject();

		// Indien nieuwe Lus
		if (lus.getId() == null) {
			// Set correcte id range voor elk subtype
			ModelStore modelStore = modelRepository
					.getModelStore("OsyrisDataStore");
			Long maxId = (Long) modelStore.searchObjects(
					"SELECT MAX(id) FROM " + lus.getModelClass().getName())
					.get(0);
			Long newId = maxId + 1;
			lus.setId(newId);
		}

		lus.setActief(true);
		lus.setNaam(LabelUtils.upperSpaced(lus.getModelClass().getName()
				.replace("Netwerk", "").toLowerCase())
				+ " " + lus.getId().toString());
	}
}
