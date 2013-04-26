package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.search.DefaultQuery;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceKey;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.traject.TrajectToewijzing;

@Named
public class TrajectToewijzingBean {

	private static final Log log = LogFactory
			.getLog(TrajectToewijzingBean.class);
	@Inject
	private ModelRepository modelRepository;

	@Inject
	private UserRepository userRepository;

	@SuppressWarnings("unchecked")
	public List<String> getTrajectToewijzingen(String username) {

		List<String> result = new ArrayList<String>();

		try {
			ResourceKey resourceKey = modelRepository
					.getResourceKey(userRepository.loadUser(username));

			DefaultQuery q = new DefaultQuery();
			q.setModelClassName("TrajectToewijzing");
			q.setFilter(FilterUtils.or(
					FilterUtils.equal("peterMeter1", resourceKey),
					FilterUtils.equal("peterMeter2", resourceKey),
					FilterUtils.equal("peterMeter3", resourceKey)));

			List<TrajectToewijzing> trajectToewijzingen = new ArrayList<TrajectToewijzing>();

			trajectToewijzingen = (List<TrajectToewijzing>) modelRepository
					.searchObjects(q, true, true);

			if (!trajectToewijzingen.isEmpty()) {

				DefaultQuery query = new DefaultQuery();
				query.setModelClassName("Traject");
				query.addFilter(FilterUtils.in("trajectToewijzing",
						trajectToewijzingen));

				List<Traject> trajecten = (List<Traject>) modelRepository
						.searchObjects(query, true, true);

				for (Traject traject : trajecten) {
					result.add("periode "
							+ traject.getTrajectToewijzing().getJaar() + ": "
							+ traject.getClass().getSimpleName() + ", "
							+ traject.getNaam() + ", " + traject.getLengte()
							+ " km");
				}

			}
			if (!result.isEmpty()) {
				return result;
			}

		} catch (IOException e) {
			log.error("Can not get trajecttoewijzing.", e);
		}

		result.add("Nog geen trajecten toegewezen");
		return result;
	}
}
