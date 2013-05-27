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
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * 
 * @author kristof
 * 
 */
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
			ResourceName resourceName = modelRepository
					.getResourceName((userRepository.loadUser(username)));

			// Get Trajecten with PM
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Traject");
			query.setFilter(FilterUtils.or(
					FilterUtils.equal("peterMeter1", resourceName),
					FilterUtils.equal("peterMeter2", resourceName),
					FilterUtils.equal("peterMeter3", resourceName)));

			List<Traject> trajecten = (List<Traject>) modelRepository
					.searchObjects(query, true, true);

			// Add results foreach periode
			for (Traject traject : trajecten) {
				if (traject.getPeterMeter1() != null
						&& traject.getPeterMeter1().equals(resourceName)) {
					result.add("Periode Lente: " + getTrajectFormat(traject));
				}
				if (traject.getPeterMeter2() != null
						&& traject.getPeterMeter2().equals(resourceName)) {
					result.add("Periode Zomer: " + getTrajectFormat(traject));
				}
				if (traject.getPeterMeter3() != null
						&& traject.getPeterMeter3().equals(resourceName)) {
					result.add("Periode Herfst: " + getTrajectFormat(traject));
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

	private String getTrajectFormat(Traject traject) {
		return traject.getClass().getSimpleName() + ", " + traject.getNaam()
				+ ", " + traject.getLengte() + " km";
	}
}
