package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.group.Group;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.QueryBuilder;
import org.jboss.seam.international.status.Messages;
import org.opengis.filter.Filter;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;

/**
 * 
 * @author kristof
 * 
 */
@Named
// TODO: Segmenten van een lus toewijzen, hoe opvangen dat meerdere PeterMeters
// per periode aan een segment kunnen worden toegewezen
// TODO: eventueel verder opsplitsen trajectprioriteiten indien nodig
// TODO: Feedback omtrent eerst deleten van de toewijzingen bij alle trajecten
// TODO: Checken bij meerdere voorkeuren routes of het dezelde route is, indien
// wel mag PM aan meerdere periodes toegewezen worden
public class PeterMeterToewijzingBean {

	private static final Log LOG = LogFactory
			.getLog(PeterMeterToewijzingBean.class);

	private static final String PERIODE_LENTE = "1";
	private static final String PERIODE_ZOMER = "2";
	private static final String PERIODE_HERFST = "3";
	private static final int MAX_TRAJECT_PER_PERIODE = 2;

	@Inject
	protected ModelRepository modelRepository;

	@Inject
	protected Messages messages;

	/**
	 * Ophalen van alle PetersMeters.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<User> getAllPetersMeters() {

		try {
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", "PeterMeter"));

			DefaultQuery q = new DefaultQuery("User");
			List<Filter> filters = new ArrayList<Filter>();

			for (ResourceName name : group.getMembers()) {
				Filter filter = FilterUtils.equal("username",
						name.getNamePart());
				filters.add(filter);
			}
			// Ophalen Users binnen de groep PeterMeter
			q.addFilter(FilterUtils.or(filters));
			List<User> petersMeters = (List<User>) modelRepository
					.searchObjects(q, false, false);
			return petersMeters;

		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			return null;
		}
	}

	/**
	 * Toekennen van Peters en Meters aan Trajecten, voorlopig enkel voor
	 * PetersMeters met 1 specifieke voorkeur.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void kenPetersMetersToe() {

		try {
			// Leegmaken toewijzingen Traject
			resetPetersMeters();

			// Ophalen PetersMeters en Profiel
			List<User> petersMeters = getAllPetersMeters();

			for (User peterMeter : petersMeters) {
				PeterMeterProfiel profiel = (PeterMeterProfiel) peterMeter
						.getAspect("PeterMeterProfiel");
				ResourceName resourceName = modelRepository
						.getResourceName(peterMeter);

				// Voorrang voor PeterMeters met 1 voorkeur
				if (profiel.getVoorkeuren().size() == 1) {
					PeterMeterVoorkeur voorkeur = profiel.getVoorkeuren()
							.get(0);

					processToekenning(voorkeur, resourceName);
				}

				// Daarna pas meerdere voorkeuren behandelen
				if (profiel.getVoorkeuren().size() > 1) {

					for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {

						// Check of PeterMeter dit jaar reeds toegewezen aan de
						// voorkeur
						processToekenning(voorkeur, resourceName);

					}
				}
			}
			messages.info("Toekenning van peters en meters succesvol uitgevoerd.");

		} catch (IOException e) {
			LOG.error("Can not search trajecten", e);
			messages.error("Fout tijdens het toekennen van peters en meters: "
					+ e.getMessage());
		}
	}

	/**
	 * Opsplitsen toekenning in Lussen en Routes.
	 * 
	 * @param voorkeur
	 * @param resourceName
	 * @throws IOException
	 */
	private void processToekenning(PeterMeterVoorkeur voorkeur,
			ResourceName peterMeter) throws IOException {

		if (checkMaxTrajecten(voorkeur, peterMeter)) {
			// Toekenning Lus
			if (voorkeur.getTrajectType().contains("Netwerk")) {
				assignLus(voorkeur, peterMeter);
			}

			// Toekenning Route
			else if (voorkeur.getTrajectType().contains("Route")) {
				assignRoute(voorkeur, peterMeter);
			}
		}
	}

	/**
	 * Eigenlijke toekenning van de PeterMeter aan een Route in een bepaalde
	 * periode. De PeterMeter mag per periode aan slechts 2 trajecten worden
	 * toegewezen. In geval van conflict wordt de PeterMeter met langste staat
	 * van dienst gekozen.
	 * 
	 **/
	private void assignRoute(PeterMeterVoorkeur voorkeur,
			ResourceName peterMeter) {

		try {

			DefaultQuery q = new DefaultQuery();
			q.setModelClassName("Traject");
			q.addFilter(FilterUtils.equal("naam", voorkeur.getTrajectNaam()));

			// Zoeken naar voorkeursroute
			Traject route = (Traject) modelRepository.searchObjects(q, false,
					false).get(0);

			// Check of PeterMeter voor 1 van de 3 periodes reeds is toegewezen
			// aan een traject
			if (!checkAlreadyAssigned(route, peterMeter)
					&& !checkAlreadyAssignedToType(route, voorkeur, peterMeter)) {

				ResourceName compareToPeterMeter = null;
				ResourceName assignedPeterMeter = null;
				if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
					compareToPeterMeter = (ResourceName) route.getPeterMeter1();

				}
				if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
					compareToPeterMeter = (ResourceName) route.getPeterMeter2();

				}
				if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
					compareToPeterMeter = (ResourceName) route.getPeterMeter3();
				}
				// Indien al een andere peterMeter is toegekend
				if (compareToPeterMeter != null
						&& compareToPeterMeter != peterMeter) {

					PeterMeterProfiel profielPM1 = (PeterMeterProfiel) modelRepository
							.loadObject(peterMeter).getAspect(
									"PeterMeterProfiel");
					PeterMeterProfiel profielPM2 = (PeterMeterProfiel) modelRepository
							.loadObject(compareToPeterMeter).getAspect(
									"PeterMeterProfiel");

					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					cal1.setTime(profielPM1.getActiefSinds());
					cal2.setTime(profielPM2.getActiefSinds());

					// Degene met de langste staat van dienst wordt toegekend
					if (cal1.before(cal2)) {
						assignedPeterMeter = peterMeter;
					} else {
						assignedPeterMeter = compareToPeterMeter;
					}
				} else {
					assignedPeterMeter = peterMeter;
				}
				if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
					route.setPeterMeter1(assignedPeterMeter);
				} else if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
					route.setPeterMeter2(assignedPeterMeter);
				} else if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
					route.setPeterMeter3(assignedPeterMeter);
				}
				modelRepository.saveObject(route);
			}

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * Toewijzen aan lus
	 * 
	 * @param voorkeur
	 * @param peterMeter
	 */
	@SuppressWarnings("unchecked")
	private void assignLus(PeterMeterVoorkeur voorkeur, ResourceName peterMeter) {

		try {

			// Zoeken naar nog vrije lussen die overeenkomen met de opgegeven
			// voorkeur
			if (!getVrijeLussen(voorkeur).isEmpty()) {

				NetwerkLus vrijeLus = getVrijeLussen(voorkeur).get(0);

				// Check of voor 1 vd 3 periodes al toegekend
				if (!checkAlreadyAssigned(vrijeLus, peterMeter)
						&& !checkAlreadyAssignedToType(vrijeLus, voorkeur,
								peterMeter)) {

					if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
						vrijeLus.setPeterMeter1(peterMeter);
					}
					if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
						vrijeLus.setPeterMeter1(peterMeter);
					}
					if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
						vrijeLus.setPeterMeter3(peterMeter);
					}
					modelRepository.saveObject(vrijeLus);
				}
			}

			// Indien alle lussen bezet
			else if (getVrijeLussen(voorkeur).isEmpty()) {

				List<NetwerkLus> lussen = new ArrayList<NetwerkLus>();

				// Fietslussen
				if (voorkeur.getTrajectType().contains("Fiets")) {

					QueryBuilder builder = new QueryBuilder("FietsNetwerkLus");
					builder.addFilter(FilterUtils.equal("regio",
							voorkeur.getRegio()));
					builder.addFilter(FilterUtils.lessOrEqual("lengte",
							voorkeur.getMaxAfstand()));
					lussen = (List<NetwerkLus>) modelRepository.searchObjects(
							builder.build(), false, false);
				}

				// WandelLussen
				else if (voorkeur.getTrajectType().contains("Wandel")) {

					QueryBuilder builder = new QueryBuilder("WandelNetwerkLus");
					builder.addFilter(FilterUtils.equal("regio",
							voorkeur.getRegio()));
					builder.addFilter(FilterUtils.lessOrEqual("lengte",
							voorkeur.getMaxAfstand()));
					lussen = (List<NetwerkLus>) modelRepository.searchObjects(
							builder.build(), false, false);
				}

				// Voor elke lus nagaan of er al een PeterMeter is
				// toegewezen
				for (NetwerkLus lus : lussen) {

					if (!checkAlreadyAssigned(lus, peterMeter)
							&& !checkAlreadyAssignedToType(lus, voorkeur,
									peterMeter)) {

						ResourceName compareToPeterMeter = null;
						ResourceName assignedPeterMeter = null;

						if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
							compareToPeterMeter = (ResourceName) lus
									.getPeterMeter1();
						}
						if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
							compareToPeterMeter = (ResourceName) lus
									.getPeterMeter2();
						}
						if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
							compareToPeterMeter = (ResourceName) lus
									.getPeterMeter3();
						}

						// Indien al een andere peterMeter is toegekend
						// toewijzen aan degene met kleinste aantal opegegeven
						// kilometers
						if (compareToPeterMeter != null
								&& compareToPeterMeter != peterMeter) {

							// Opzoeken maxafstand van reeds toegewezen
							// PeterMeter
							PeterMeterProfiel profielPM2 = (PeterMeterProfiel) modelRepository
									.loadObject(compareToPeterMeter).getAspect(
											"PeterMeterProfiel");
							for (PeterMeterVoorkeur pref : profielPM2
									.getVoorkeuren()) {
								if (pref.getTrajectType().equals(
										voorkeur.getTrajectType())
										&& voorkeur.getMaxAfstand() < pref
												.getMaxAfstand()) {
									assignedPeterMeter = peterMeter;
									break;
								} else {
									assignedPeterMeter = compareToPeterMeter;
								}
							}
						}

						if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
							lus.setPeterMeter1(assignedPeterMeter);
						} else if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
							lus.setPeterMeter2(assignedPeterMeter);
						} else if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
							lus.setPeterMeter3(assignedPeterMeter);
						}
						modelRepository.saveObject(lus);
					}
				}
			}
		} catch (IOException e) {
			LOG.error("Can not search NetwerkLussen.", e);
		}

		// Indien conflict toewijzen aan degene met kleinste aantal opegegeven
		// kilometers, indien dat gelijk op anncieniteit
	}

	/**
	 * Resetten van alle peterMeter toekenningen.
	 * 
	 * @param traject
	 */
	@SuppressWarnings("unchecked")
	private void resetPetersMeters() {

		try {
			QueryBuilder builder = new QueryBuilder();
			builder.modelClassName("Traject");
			builder.addFilter(FilterUtils.or(
					FilterUtils.notEqual("peterMeter1", null),
					FilterUtils.notEqual("peterMeter2", null),
					FilterUtils.notEqual("peterMeter3", null)));

			List<Traject> trajecten = (List<Traject>) modelRepository
					.searchObjects(builder.build(), false, false);

			for (Traject t : trajecten) {

				t.setPeterMeter1(null);
				t.setPeterMeter2(null);
				t.setPeterMeter3(null);
				modelRepository.saveObject(t);
			}
		} catch (IOException e) {
			LOG.error("Can not save traject.", e);
		}
	}

	/**
	 * Checken of de peterMeter aan minder dan 2 trajecten in een bepaalde
	 * periode is toegewezen.
	 * 
	 * @param voorkeur
	 * @param peterMeter
	 * @return
	 * @throws IOException
	 */
	private boolean checkMaxTrajecten(PeterMeterVoorkeur voorkeur,
			ResourceName peterMeter) throws IOException {

		int trajectCounter = 0;

		if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.equal("peterMeter1", peterMeter));
			trajectCounter = modelRepository.countObjects(builder.build(),
					false);
		}
		if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.equal("peterMeter2", peterMeter));
			trajectCounter = modelRepository.countObjects(builder.build(),
					false);
		}
		if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
			QueryBuilder builder = new QueryBuilder("Traject");
			builder.addFilter(FilterUtils.equal("peterMeter3", peterMeter));
			trajectCounter = modelRepository.countObjects(builder.build(),
					false);
		}

		// PeterMeter mag nog traject toegewezen krijgen indien onder het max
		// aantal
		if (trajectCounter < MAX_TRAJECT_PER_PERIODE) {
			return true;
		}

		else {
			return false;
		}
	}

	/**
	 * Checken of een Peter meter in 1 van de 3 periodes reeds aan een bepaald
	 * Traject is toegewezen geweest. Dit is om gewenning tegen te gaan. Voor
	 * PetersMeters met slechts 1 voorkeur wordt een uitzondering gemaakt.
	 * 
	 * @return
	 */
	private boolean checkAlreadyAssigned(Traject voorkeursTraject,
			ResourceName peterMeter) {

		int counter = 0;

		if (voorkeursTraject.getPeterMeter1() != null) {
			counter++;
		}
		if (voorkeursTraject.getPeterMeter2() != null) {
			counter++;
		}
		if (voorkeursTraject.getPeterMeter3() != null) {
			counter++;
		}

		if (counter > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checken of het toe te wijzen trajectType slechts 1 keer periode voorkomt.
	 * 
	 * @throws IOException
	 * 
	 */
	@SuppressWarnings("unchecked")
	private boolean checkAlreadyAssignedToType(Traject traject,
			PeterMeterVoorkeur voorkeur, ResourceName peterMeter)
			throws IOException {

		QueryBuilder builder = new QueryBuilder("Traject");
		builder.addFilter(FilterUtils.equal("peterMeter1", peterMeter));
		boolean isAlreadyAssigned = false;

		List<Traject> toegewezenTrajecten = (List<Traject>) modelRepository
				.searchObjects(builder.build(), false, false);

		if (!toegewezenTrajecten.isEmpty()) {
			for (Traject t : toegewezenTrajecten) {

				if (t.getModelClass().getName()
						.equalsIgnoreCase(voorkeur.getTrajectType())) {
					isAlreadyAssigned = true;
				}
			}
		}
		return isAlreadyAssigned;
	}

	@SuppressWarnings("unchecked")
	private List<NetwerkLus> getVrijeLussen(PeterMeterVoorkeur voorkeur)
			throws IOException {

		// Checken vrije lussen die aan de opgegeven voorkeur voldoen
		List<NetwerkLus> vrijeLussen = new ArrayList<NetwerkLus>();

		if (voorkeur.getTrajectType().contains("Fiets")) {
			QueryBuilder builder = new QueryBuilder("FietsNetwerkLus");
			builder.addFilter(FilterUtils.equal("regio", voorkeur.getRegio()));
			builder.addFilter(FilterUtils.lessOrEqual("lengte",
					voorkeur.getMaxAfstand()));
			if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
				builder.addFilter(FilterUtils.equal("peterMeter1", null));
			}
			if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
				builder.addFilter(FilterUtils.equal("peterMeter2", null));
			}
			if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
				builder.addFilter(FilterUtils.equal("peterMeter3", null));
			}
			vrijeLussen = (List<NetwerkLus>) modelRepository.searchObjects(
					builder.build(), false, false);
		}

		if (voorkeur.getTrajectType().contains("Wandel")) {
			QueryBuilder builder = new QueryBuilder("WandelNetwerkLus");
			builder.addFilter(FilterUtils.equal("regio", voorkeur.getRegio()));
			builder.addFilter(FilterUtils.lessOrEqual("lengte",
					voorkeur.getMaxAfstand()));
			if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
				builder.addFilter(FilterUtils.equal("peterMeter1", null));
			}
			if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
				builder.addFilter(FilterUtils.equal("peterMeter2", null));
			}
			if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
				builder.addFilter(FilterUtils.equal("peterMeter3", null));
			}
			vrijeLussen = (List<NetwerkLus>) modelRepository.searchObjects(
					builder.build(), false, false);
		}

		return vrijeLussen;
	}
}
