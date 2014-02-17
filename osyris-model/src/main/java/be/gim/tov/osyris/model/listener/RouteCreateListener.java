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
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.Route;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "create", _for = "Route") })
public class RouteCreateListener {

	private static final Log log = LogFactory
			.getLog(NetwerkBordCreateListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		Route route = (Route) event.getModelObject();

		// Automatisch setten Regio
		if (route.getRegio() == null) {

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForTraject(route);
			if (regio != null) {
				route.setRegio(modelRepository.getResourceKey(regio));
			}
		}
	}
}
