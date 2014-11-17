package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.bean.Beans;
import be.gim.commons.collections.CollectionUtils;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "RouteBord") })
public class RouteBordSaveListener {

	private static final Log log = LogFactory
			.getLog(RouteBordSaveListener.class);

	@Inject
	private ModelRepository modelRepository;

	@SuppressWarnings("unchecked")
	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		RouteBord routeBord = (RouteBord) event.getModelObject();

		// Automatically set Regio
		if (routeBord.getRegio() == null) {

			// Ophalen regio van de gekoppelde route
			Traject traject = Beans.getReference(OsyrisModelFunctions.class)
					.getRouteForRouteBord(routeBord);
			if (traject != null) {
				routeBord.setRegio(traject.getRegio());
			}
		}

		if (routeBord.getNaam() != null && !routeBord.getNaam().isEmpty()) {

			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.equal("naam", routeBord.getNaam()));
			builder.maxResults(1);
			Traject traject = (Traject) CollectionUtils.first(modelRepository
					.searchObjects(builder.build(), false, false));
			if (traject != null) {
				routeBord.setRoute(modelRepository
						.getResourceIdentifier(traject));
			}
		}

		// Set route voor RouteBord indien niet aanwezig
		if (routeBord.getNaam() == null || routeBord.getNaam().isEmpty()
				|| routeBord.getRoute() == null) {

			Traject traject = Beans.getReference(OsyrisModelFunctions.class)
					.getRouteForRouteBord(routeBord);
			if (traject != null) {
				routeBord.setRoute(modelRepository.getResourceKey(traject));
				routeBord.setNaam(traject.getNaam());
			}
		}
	}
}
