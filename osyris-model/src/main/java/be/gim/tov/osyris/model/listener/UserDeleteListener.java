package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.group.Group;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.codes.PeterMeterNaamCode;
import be.gim.tov.osyris.model.traject.Traject;
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

			// Delete toewijzingen
			deleteToewijzingen(user);

			// Delete user from PM group
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", "PeterMeter"));
			group.getMembers().remove(modelRepository.getResourceName(user));
			modelRepository.saveObject(group);

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

	/**
	 * Verwijdert de trajectToewijzingen bij het deleten van PeterMeter.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void deleteToewijzingen(User user) {

		try {
			ResourceName peterMeter = new ResourceName("user",
					user.getUsername());

			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.or(
					FilterUtils.equal("peterMeter1", peterMeter),
					FilterUtils.equal("peterMeter2", peterMeter),
					FilterUtils.equal("peterMeter3", peterMeter)));

			List<Traject> result = (List<Traject>) modelRepository
					.searchObjects(builder.build(), false, false);

			for (Traject traject : result) {
				if (traject.getPeterMeter1() != null
						&& traject.getPeterMeter1().equals(peterMeter)) {
					traject.setPeterMeter1(null);
				}
				if (traject.getPeterMeter2() != null
						&& traject.getPeterMeter2().equals(peterMeter)) {
					traject.setPeterMeter2(null);
				}
				if (traject.getPeterMeter3() != null
						&& traject.getPeterMeter3().equals(peterMeter)) {
					traject.setPeterMeter3(null);
				}

				modelRepository.saveObject(traject);
			}

		} catch (IOException e) {
			LOG.error("Can not search Trajecten.", e);
		}
	}
}
