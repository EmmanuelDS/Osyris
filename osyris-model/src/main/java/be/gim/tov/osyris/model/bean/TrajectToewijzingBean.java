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

@Named
public class TrajectToewijzingBean {

	private static final Log LOG = LogFactory
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

			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Traject");
			query.setFilter(FilterUtils.or(
					FilterUtils.equal("peterMeter1", resourceKey),
					FilterUtils.equal("peterMeter2", resourceKey),
					FilterUtils.equal("peterMeter3", resourceKey)));

			List<Traject> trajecten = (List<Traject>) modelRepository
					.searchObjects(query, true, true);

			for (Traject traject : trajecten) {
				if (traject.getPeterMeter1() != null
						&& traject.getPeterMeter1().equals(resourceKey)) {
					result.add("Periode Lente: "
							+ traject.getClass().getSimpleName() + ", "
							+ traject.getNaam() + ", " + traject.getLengte()
							+ " km");
				}
				if (traject.getPeterMeter2() != null
						&& traject.getPeterMeter2().equals(resourceKey)) {
					result.add("Periode Zomer: "
							+ traject.getClass().getSimpleName() + ", "
							+ traject.getNaam() + ", " + traject.getLengte()
							+ " km");
				}
				if (traject.getPeterMeter3() != null
						&& traject.getPeterMeter3().equals(resourceKey)) {
					result.add("Periode Herfst: "
							+ traject.getClass().getSimpleName() + ", "
							+ traject.getNaam() + ", " + traject.getLengte()
							+ " km");
				}
			}
			if (!result.isEmpty()) {
				return result;
			}

		} catch (IOException e) {
			LOG.error("Can not get trajecttoewijzing.", e);
		}

		result.add("Nog geen trajecten toegewezen");
		return result;
	}
}
