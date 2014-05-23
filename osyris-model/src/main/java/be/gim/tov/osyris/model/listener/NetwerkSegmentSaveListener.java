package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.AfterModelEvent;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Regio;

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

		// Conversie MultiLineString naar LineString indien van toepassing
		if (segment.getGeom() != null) {
			segment.setGeom(GeometryUtils.connect(segment.getGeom()));
		}

		// Automatisch setten Regio
		if (segment.getRegio() == null) {

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForSegment(segment);
			if (regio != null) {
				segment.setRegio(modelRepository.getResourceKey(regio));
			}
		}

		// Automatisch setten VanKpNr
		if (ResourceIdentifier.isNotEmpty(segment.getVanKnooppunt())) {
			NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
					.loadObject(segment.getVanKnooppunt());
			if (knooppunt != null) {
				segment.setVanKpNr(knooppunt.getNummer());
			}
		}

		// Automatisch setten NaarKpNr
		if (ResourceIdentifier.isNotEmpty(segment.getNaarKnooppunt())) {
			NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
					.loadObject(segment.getNaarKnooppunt());
			if (knooppunt != null) {
				segment.setNaarKpNr(knooppunt.getNummer());
			}
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
