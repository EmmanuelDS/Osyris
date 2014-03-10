package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;

import be.gim.commons.bean.Beans;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.FietsNetwerkSegment;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Regio;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "create", _for = "NetwerkSegment") })
public class NetwerkSegmentCreateListener {

	private static final Log log = LogFactory
			.getLog(NetwerkSegmentCreateListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkSegment segment = (NetwerkSegment) event.getModelObject();

		// Automatisch setten Regio
		if (segment.getRegio() == null) {

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForSegment(segment);
			if (regio != null) {
				segment.setRegio(modelRepository.getResourceKey(regio));
			}
		}

		// FNW Segment naam is regioNaam
		if (segment instanceof FietsNetwerkSegment) {

			Regio regio = (Regio) Beans.getReference(ModelRepository.class)
					.loadObject(segment.getRegio());
			if (regio != null) {
				((FietsNetwerkSegment) segment).setNaam(regio.getNaam());
			}
		}
	}
}
