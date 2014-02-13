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
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "create", _for = "RouteBord") })
public class RouteBordCreateListener {

	private static final Log log = LogFactory
			.getLog(NetwerkBordSaveListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		RouteBord routeBord = (RouteBord) event.getModelObject();

		routeBord.setActief("1");

		Traject traject;
		if (routeBord.getRoute() == null && routeBord.getNaam() == null) {
			traject = Beans.getReference(OsyrisModelFunctions.class)
					.getRouteForRouteBord(routeBord);

			if (traject != null)
				routeBord.setRoute(modelRepository.getResourceKey(traject));
		} else {
			traject = (Traject) modelRepository
					.loadObject(routeBord.getRegio());
		}

		if (traject != null && routeBord.getRoute() != null
				&& routeBord.getNaam() == null) {
			routeBord.setNaam(traject.getNaam());
		}

		// Automatically set Regio
		if (traject != null && routeBord.getRegio() == null) {
			routeBord.setRegio(traject.getRegio());
		}

		// Automatically set Gemeente
		if (routeBord.getGemeente() == null) {

			String gemeente = Beans.getReference(OsyrisModelFunctions.class)
					.getGemeenteForBord(routeBord);
			routeBord.setGemeente(gemeente);
		}
	}
}
