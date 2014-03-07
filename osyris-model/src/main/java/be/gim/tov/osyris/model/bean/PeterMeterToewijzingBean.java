package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

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

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.PeterMeterProfiel;
import be.gim.tov.osyris.model.user.PeterMeterVoorkeur;
import be.gim.tov.osyris.model.user.status.PeterMeterStatus;

/**
 * Bean klasse voor het toewijzen van Peters en Meters aan Routes en Lussen op
 * basis van de door hun opgestelde voorkeuren.
 * 
 * Bij het runnen van het algortime worden alle eerdere toewijzingen verwijderd.
 * 
 * Peters en Meters worden niet toegewezen aan segmenten.
 * 
 * @author kristof
 * 
 */
@Named
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
			return (List) modelRepository.loadObjects(group.getMembers());

		} catch (IOException e) {
			LOG.error("Can not get search results.", e);
			return null;
		}
	}

	/**
	 * Toekennen van Peters en Meters aan Trajecten, voorlopig enkel voor
	 * PetersMeters met 1 specifieke voorkeur.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * 
	 */
	public void kenPetersMetersToe() throws InstantiationException,
			IllegalAccessException {

		try {
			// Leegmaken toewijzingen Traject
			resetPetersMeters();

			// Alle PetersMeters met 1 enkele voorkeur
			List<User> usersWithSingleVoorkeur = getPeterMetersWithSingleVoorkeur(
					getAllPetersMeters(), true);

			// Alle PetersMeters met meerdere voorkeuren
			List<User> usersWithMultipleVoorkeuren = getPeterMetersWithSingleVoorkeur(
					getAllPetersMeters(), false);

			// Voorrang voor PeterMeters met 1 voorkeur, toekenning gebeurt
			// volgens trajectType prioriteitenlijst
			for (String type : getPriorityList()) {

				for (User peterMeter : usersWithSingleVoorkeur) {

					PeterMeterProfiel profiel = (PeterMeterProfiel) peterMeter
							.getAspect("PeterMeterProfiel");
					ResourceName resourceName = modelRepository
							.getResourceName(peterMeter);

					if (profiel != null) {

						PeterMeterVoorkeur voorkeur = profiel.getVoorkeuren()
								.get(0);

						if (voorkeur != null) {
							if (voorkeur.getTrajectType().contains(type)) {
								processToekenning(voorkeur, resourceName);
							}
						}
					}
				}
			}
			// Daarna pas meerdere voorkeuren behandelen, toekenning volgens
			// trajectType prioriteitenlijst
			for (String type : getPriorityList()) {

				for (User peterMeter : usersWithMultipleVoorkeuren) {

					PeterMeterProfiel profiel = (PeterMeterProfiel) peterMeter
							.getAspect("PeterMeterProfiel");

					ResourceName resourceName = modelRepository
							.getResourceName(peterMeter);

					if (profiel != null) {

						for (PeterMeterVoorkeur voorkeur : profiel
								.getVoorkeuren()) {

							if (voorkeur != null) {
								if (voorkeur.getTrajectType().contains(type)) {
									processToekenning(voorkeur, resourceName);
								}
							}
						}
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
	 * Opsplitsen toekenning voor Lussen en Routes.
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

				if (voorkeur.getTrajectNaam() != null) {
					assignRoute(voorkeur, peterMeter);
				}

				else {
					assignRandomRoute(voorkeur, peterMeter);
				}
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
	@SuppressWarnings("unchecked")
	private void assignRoute(PeterMeterVoorkeur voorkeur,
			ResourceName peterMeter) {

		try {

			Traject route = null;

			// Assign route met naam
			if (voorkeur.getTrajectNaam() != null) {

				DefaultQuery q = new DefaultQuery();
				q.setModelClassName("Traject");
				q.addFilter(FilterUtils.equal("naam", voorkeur.getTrajectNaam()));

				// Checken op route effectief gevonden is
				List<Traject> result = (List<Traject>) modelRepository
						.searchObjects(q, false, false);

				if (!result.isEmpty()) {
					route = result.get(0);
				}
			}

			if (route != null) {

				// Check of PeterMeter voor 1 van de 3 periodes reeds is
				// toegewezen
				// aan een traject en of reeds toegewezen aan een bepaald
				// type
				// traject
				// Hierin zit ook de check om te kijken of een PeterMeter 1
				// bepaalde
				// route wil controleren
				if (!checkAlreadyAssigned(route, peterMeter)
						&& !checkAlreadyAssignedToType(route, voorkeur,
								peterMeter)) {

					ResourceName compareToPeterMeter = null;
					ResourceName assignedPeterMeter = null;
					if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
						compareToPeterMeter = (ResourceName) route
								.getPeterMeter1();

					}
					if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
						compareToPeterMeter = (ResourceName) route
								.getPeterMeter2();

					}
					if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
						compareToPeterMeter = (ResourceName) route
								.getPeterMeter3();
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

						if (profielPM1 != null && profielPM2 != null) {

							Calendar cal1 = Calendar.getInstance();
							Calendar cal2 = Calendar.getInstance();
							cal1.setTime(profielPM1.getActiefSinds());
							cal2.setTime(profielPM2.getActiefSinds());

							// Degene met de langste staat van dienst wordt
							// toegekend
							if (cal1.before(cal2)) {
								assignedPeterMeter = peterMeter;
							} else {
								assignedPeterMeter = compareToPeterMeter;
							}
						}
					} else {
						assignedPeterMeter = peterMeter;
					}

					// Bewaren toewijzing
					if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
						route.setPeterMeter1(assignedPeterMeter);
					} else if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
						route.setPeterMeter2(assignedPeterMeter);
					} else if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
						route.setPeterMeter3(assignedPeterMeter);
					}
					modelRepository.saveObject(route);
				}
			}

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * Toewijzen van een route waarvoor in de voorkeur enkel een regio en geen
	 * specifieke naam is opgegeven. Er wordt gezocht naar routes die voor de
	 * voorkeursperiode nog vrij zijn voor wat betreft de voorkeursregio. Zijn
	 * er geen vrije routes meer dan krijgt de PeterMeter met de langste staat
	 * van dienst prioriteit.
	 * 
	 * @param voorkeur
	 * @param peterMeter
	 */
	private void assignRandomRoute(PeterMeterVoorkeur voorkeur,
			ResourceName peterMeter) {

		try {
			// Zoeken vrije routes in de opgegeven regio
			List<Route> vrijeRoutes = getVrijeRoutes(voorkeur);

			if (!vrijeRoutes.isEmpty()) {

				Random randomGenerator = new Random();
				int randomIndex = randomGenerator.nextInt(vrijeRoutes.size());

				Route vrijeRoute = vrijeRoutes.get(randomIndex);

				// Check of voor 1 vd 3 periodes al toegekend
				if (!checkAlreadyAssigned(vrijeRoute, peterMeter)
						&& !checkAlreadyAssignedToType(vrijeRoute, voorkeur,
								peterMeter)) {

					if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
						vrijeRoute.setPeterMeter1(peterMeter);
					}
					if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
						vrijeRoute.setPeterMeter2(peterMeter);
					}
					if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
						vrijeRoute.setPeterMeter3(peterMeter);
					}
					modelRepository.saveObject(vrijeRoute);
				}
			}

			// Indien alle routes bezet zijn
			else if (vrijeRoutes.isEmpty()) {

				List<Route> routes = new ArrayList<Route>();

				QueryBuilder builder = new QueryBuilder(
						voorkeur.getTrajectType());
				builder.addFilter(FilterUtils.equal("regio",
						voorkeur.getRegio()));
				routes = (List<Route>) modelRepository.searchObjects(
						builder.build(), false, false);

				// Voor elke lus nagaan of er al een PeterMeter is
				// toegewezen
				for (Route r : routes) {

					if (!checkAlreadyAssigned(r, peterMeter)
							&& !checkAlreadyAssignedToType(r, voorkeur,
									peterMeter)) {

						ResourceName compareToPeterMeter = null;
						ResourceName assignedPeterMeter = null;

						if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
							compareToPeterMeter = (ResourceName) r
									.getPeterMeter1();
						}
						if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
							compareToPeterMeter = (ResourceName) r
									.getPeterMeter2();
						}
						if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
							compareToPeterMeter = (ResourceName) r
									.getPeterMeter3();
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

							if (profielPM1 != null && profielPM2 != null) {
								// Enkel checken indien actiefSinds is ingevuld.
								if (profielPM1.getActiefSinds() != null
										&& profielPM2.getActiefSinds() != null) {

									Calendar cal1 = Calendar.getInstance();
									Calendar cal2 = Calendar.getInstance();
									cal1.setTime(profielPM1.getActiefSinds());
									cal2.setTime(profielPM2.getActiefSinds());

									// Degene met de langste staat van dienst
									// wordt
									// toegekend
									if (cal1.before(cal2)) {
										assignedPeterMeter = peterMeter;
									} else {
										assignedPeterMeter = compareToPeterMeter;
									}
								} else {
									assignedPeterMeter = peterMeter;
								}

								// Bewaren toewijzing
								if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
									r.setPeterMeter1(assignedPeterMeter);
								} else if (voorkeur.getPeriode().equals(
										PERIODE_ZOMER)) {
									r.setPeterMeter2(assignedPeterMeter);
								} else if (voorkeur.getPeriode().equals(
										PERIODE_HERFST)) {
									r.setPeterMeter3(assignedPeterMeter);
								}
								modelRepository.saveObject(r);
							}
						}
					}
				}
			}

		} catch (IOException e) {

			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * * Eigenlijke toekenning van de PeterMeter aan een Lus in een bepaalde
	 * periode. De PeterMeter mag per periode aan slechts 2 trajecten worden
	 * toegewezen. In geval van conflict krijgt de PeterMeter met het kleinste
	 * aantal max kilometers de voorkeur
	 * 
	 * @param voorkeur
	 * @param peterMeter
	 */
	@SuppressWarnings("unchecked")
	private void assignLus(PeterMeterVoorkeur voorkeur, ResourceName peterMeter) {

		try {

			// Zoeken naar nog vrije lussen die overeenkomen met de opgegeven
			// voorkeur
			List<NetwerkLus> vrijeLussen = getVrijeLussen(voorkeur);

			if (!vrijeLussen.isEmpty()) {

				Random randomGenerator = new Random();
				int randomIndex = randomGenerator.nextInt(vrijeLussen.size());

				NetwerkLus vrijeLus = vrijeLussen.get(randomIndex);

				// Check of voor 1 vd 3 periodes al toegekend
				if (!checkAlreadyAssigned(vrijeLus, peterMeter)
						&& !checkAlreadyAssignedToType(vrijeLus, voorkeur,
								peterMeter)) {

					if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
						vrijeLus.setPeterMeter1(peterMeter);
					}
					if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
						vrijeLus.setPeterMeter2(peterMeter);
					}
					if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
						vrijeLus.setPeterMeter3(peterMeter);
					}
					modelRepository.saveObject(vrijeLus);
				}
			}

			// Indien alle lussen bezet
			else if (vrijeLussen.isEmpty()) {

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
						// toewijzen aan degene met kleinste aantal max
						// kilometers, indien nog gelijk op ancienniteit
						if (compareToPeterMeter != null
								&& compareToPeterMeter != peterMeter) {

							// Opzoeken maxafstand van reeds toegewezen
							// PeterMeter
							PeterMeterProfiel profielPM2 = (PeterMeterProfiel) modelRepository
									.loadObject(compareToPeterMeter).getAspect(
											"PeterMeterProfiel");

							if (profielPM2 != null) {
								for (PeterMeterVoorkeur pref : profielPM2
										.getVoorkeuren()) {

									if (pref != null) {

										if (pref.getTrajectType().equals(
												voorkeur.getTrajectType())
												&& voorkeur.getMaxAfstand() < pref
														.getMaxAfstand()) {
											assignedPeterMeter = peterMeter;
											break;
										}

									} else {
										assignedPeterMeter = compareToPeterMeter;
									}
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

		// Uitzondering: Peters/Meters die uitsluitend 1 bepaalde route willen
		// controleren
		if (wantsToCheckSingleRoute(peterMeter)) {
			return false;
		}

		int counter = 0;

		if (voorkeursTraject.getPeterMeter1() != null
				&& voorkeursTraject.getPeterMeter1().equals(peterMeter)) {
			counter++;
		}
		if (voorkeursTraject.getPeterMeter2() != null
				&& voorkeursTraject.getPeterMeter2().equals(peterMeter)) {
			counter++;
		}
		if (voorkeursTraject.getPeterMeter3() != null
				&& voorkeursTraject.getPeterMeter3().equals(peterMeter)) {
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

		// Uitzondering: Peters/Meters die uitsluitend 1 bepaalde route willen
		// controleren
		if (wantsToCheckSingleRoute(peterMeter)) {
			return false;
		}

		QueryBuilder builder = new QueryBuilder("Traject");
		boolean isAlreadyAssigned = false;

		// Check per periode of het trajectType verschilt
		if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
			builder.addFilter(FilterUtils.equal("peterMeter1", peterMeter));
		}
		if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
			builder.addFilter(FilterUtils.equal("peterMeter2", peterMeter));
		}
		if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
			builder.addFilter(FilterUtils.equal("peterMeter3", peterMeter));
		}

		List<Traject> toegewezenTrajectenInPeriode = (List<Traject>) modelRepository
				.searchObjects(builder.build(), false, false);

		if (!toegewezenTrajectenInPeriode.isEmpty()) {
			for (Traject t : toegewezenTrajectenInPeriode) {

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
			if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
				builder.addFilter(FilterUtils.equal("peterMeter2", null));
			}
			if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
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
			if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
				builder.addFilter(FilterUtils.equal("peterMeter2", null));
			}
			if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
				builder.addFilter(FilterUtils.equal("peterMeter3", null));
			}
			vrijeLussen = (List<NetwerkLus>) modelRepository.searchObjects(
					builder.build(), false, false);
		}

		return vrijeLussen;
	}

	@SuppressWarnings("unchecked")
	private List<Route> getVrijeRoutes(PeterMeterVoorkeur voorkeur)
			throws IOException {

		// Checken vrije lussen die aan de opgegeven voorkeur voldoen
		List<Route> vrijeRoutes = new ArrayList<Route>();

		QueryBuilder builder = new QueryBuilder(voorkeur.getTrajectType());
		builder.addFilter(FilterUtils.equal("regio", voorkeur.getRegio()));

		if (voorkeur.getPeriode().equals(PERIODE_LENTE)) {
			builder.addFilter(FilterUtils.equal("peterMeter1", null));
		}
		if (voorkeur.getPeriode().equals(PERIODE_ZOMER)) {
			builder.addFilter(FilterUtils.equal("peterMeter2", null));
		}
		if (voorkeur.getPeriode().equals(PERIODE_HERFST)) {
			builder.addFilter(FilterUtils.equal("peterMeter3", null));
		}
		vrijeRoutes = (List<Route>) modelRepository.searchObjects(
				builder.build(), false, false);

		return vrijeRoutes;
	}

	/**
	 * Ophalen van alle Peters en Meters met ofwel 1 voorkeur ofwel meerdere
	 * voorkeuren. Enkel PeterMeters met status ACTIEF worden opgenomen0
	 * 
	 * @param allPetersMeters
	 * @param single
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 */
	private List<User> getPeterMetersWithSingleVoorkeur(
			List<User> allPetersMeters, boolean singleVoorkeur)
			throws IOException, InstantiationException, IllegalAccessException {

		List<User> result = new ArrayList<User>();

		for (User peterMeter : allPetersMeters) {
			PeterMeterProfiel profiel = (PeterMeterProfiel) peterMeter
					.getAspect("PeterMeterProfiel", modelRepository, true);

			if (profiel != null) {
				if (profiel.getStatus().equals(PeterMeterStatus.ACTIEF)) {

					if (singleVoorkeur) {
						if (profiel.getVoorkeuren() != null
								&& profiel.getVoorkeuren().size() == 1) {
							result.add(peterMeter);
						}
					}

					else {
						if (profiel.getVoorkeuren() != null
								&& profiel.getVoorkeuren().size() > 1) {
							result.add(peterMeter);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Ophalen prioriteitslijst trajectTypes bij toekenning.
	 * 
	 * @return
	 */
	private List<String> getPriorityList() {

		List<String> trajectTypes = new ArrayList<String>();
		trajectTypes.add("FietsNetwerk");
		trajectTypes.add("WandelNetwerk");
		trajectTypes.add("FietsRoute");
		trajectTypes.add("WandelRoute");
		trajectTypes.add("RuiterRoute");
		trajectTypes.add("AutoRoute");

		return trajectTypes;
	}

	/**
	 * Check of een peterMeter 1 bepaalde Route wil controleren.
	 * 
	 * @param voorkeur
	 * @param peterMeter
	 * @return
	 */
	private boolean wantsToCheckSingleRoute(ResourceName peterMeter) {

		try {
			boolean flag = true;

			User u = (User) modelRepository.loadObject(peterMeter);

			PeterMeterProfiel profiel = (PeterMeterProfiel) u
					.getAspect("PeterMeterProfiel");

			if (profiel != null) {
				// Ophalen van een trajectNaam ter referentie, trajectNaam is
				// uniek
				// voor een route
				String compareNaam = null;
				for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {

					if (voorkeur != null) {
						if (voorkeur.getTrajectNaam() != null) {
							compareNaam = voorkeur.getTrajectNaam();
							break;
						}

						// Route zonder naam gespecifieerd wordt beschouwd als
						// willen
						// controleren van meerdere routes
						else {
							return false;
						}
					}
				}

				// Check of er nog andere trajectNaam voorkomt, zoja wil
				// PeterMeter
				// meerdere routes controleren
				for (PeterMeterVoorkeur voorkeur : profiel.getVoorkeuren()) {

					if (voorkeur != null) {
						if (voorkeur.getTrajectNaam() != null
								&& !voorkeur.getTrajectNaam().equals(
										compareNaam)) {
							flag = false;
						}
					}
				}

				return flag;
			}

		} catch (IOException e) {
			LOG.error("Can not load PeterMeter.", e);
		}
		return false;
	}
}
