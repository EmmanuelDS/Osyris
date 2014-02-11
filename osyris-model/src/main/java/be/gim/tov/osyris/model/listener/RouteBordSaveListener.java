package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.Regio;
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

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForBord(routeBord);
			routeBord.setRegio(modelRepository.getResourceKey(regio));
		}

		// Set route voor RouteBord indien niet aanwezig
		if (routeBord.getRoute() == null && routeBord.getNaam() != null) {

			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.equal("naam", routeBord.getNaam()));
			builder.maxResults(1);
			List<Traject> result = (List<Traject>) modelRepository
					.searchObjects(builder.build(), false, false);
			Traject route = result.get(0);
			routeBord.setRoute(modelRepository.getResourceIdentifier(route));
		}

		// TODO zoeken welk traject het dichtste bij ligt
		if (routeBord.getRoute() == null && routeBord.getNaam() == null) {

			Traject traject = Beans.getReference(OsyrisModelFunctions.class)
					.getRouteForRouteBord(routeBord);

			routeBord.setRoute(modelRepository.getResourceKey(traject));
		}

	}
}
