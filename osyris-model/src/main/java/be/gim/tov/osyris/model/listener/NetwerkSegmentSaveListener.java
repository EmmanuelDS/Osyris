package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.AfterModelEvent;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;

@Listener(rules = { @Rule(_for = "NetwerkSegment", type = "save") })
public class NetwerkSegmentSaveListener {
	private static final Log log = LogFactory
			.getLog(NetwerkSegmentSaveListener.class);

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		event.addAfterEvent(new AfterModelEvent() {

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
										+ "' cahnged by netwerksegment '"
										+ segment.getId() + "'.", e);
					}
				}
			}
		});
	}
}
