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
import org.conscientia.api.user.User;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.codes.PeterMeterNaamCode;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "delete", _for = "User") })
public class UserDeleteListener {

	private static final Log LOG = LogFactory.getLog(UserSaveListener.class);

	@Inject
	private ModelRepository modelRepository;

	@SuppressWarnings("unchecked")
	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		User user = (User) event.getModelObject();
		ResourceName resourceName = new ResourceName("user", user.getUsername());

		PeterMeterProfiel profiel = (PeterMeterProfiel) user
				.getAspect("PeterMeterProfiel");

		if (profiel != null) {

			// Search and delete PeterMeterNaamCode
			QueryBuilder builder = new QueryBuilder("PeterMeterNaamCode");
			builder.addFilter(FilterUtils.equal("code", resourceName.toString()));

			List<PeterMeterNaamCode> codes = (List<PeterMeterNaamCode>) modelRepository
					.searchObjects(builder.build(), false, false);

			if (!codes.isEmpty()) {
				for (PeterMeterNaamCode code : codes) {
					modelRepository.deleteObject(code);
				}
			}
		}
	}
}
