package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.AfterModelEvent;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "NetwerkSegment") })
public class NetwerkSegmentSaveListener {

	private static final Log log = LogFactory
			.getLog(NetwerkSegmentSaveListener.class);

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

		// Automatisch setten VanKpNr
		if (segment.getVanKnooppunt() != null
				&& StringUtils.isNotBlank(segment.getVanKnooppunt().toString())) {
			NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
					.loadObject(segment.getVanKnooppunt());
			segment.setVanKpNr(knooppunt.getNummer());
		}

		// Automatisch setten NaarKpNr
		if (segment.getNaarKnooppunt() != null
				&& StringUtils
						.isNotBlank(segment.getNaarKnooppunt().toString())) {
			NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
					.loadObject(segment.getNaarKnooppunt());
			segment.setNaarKpNr(knooppunt.getNummer());
		}

		event.addAfterEvent(new AfterModelEvent() {

			@Override
			@SuppressWarnings("unchecked")
			public void processEvent(ModelEvent event) throws Exception {

				ModelRepository modelRepository = event.getSource();

				NetwerkSegment segment = (NetwerkSegment) event
						.getModelObject();

				QueryBuilder builder = new QueryBuilder("NetwerkLus");

				builder.addFilter(FilterUtils.equal("segmenten",
						modelRepository.getResourceKey(segment)));

				List<NetwerkLus> lussen = (List) modelRepository.searchObjects(
						builder.build(), false, false);

				for (NetwerkLus lus : lussen) {
					try {
						modelRepository.saveObject(lus);
					} catch (Exception e) {
						log.error(
								"Can not save netwerklus '" + lus.getId()
										+ "' changed by netwerksegment '"
										+ segment.getId() + "'.", e);
					}
				}
			}
		});
	}
}
