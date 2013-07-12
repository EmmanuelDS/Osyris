package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.group.Group;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.functions.ModelFunctions;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.Gemeente;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.MedewerkerProfiel;
import be.gim.tov.osyris.model.user.UitvoerderBedrijf;
import be.gim.tov.osyris.model.user.UitvoerderProfiel;
import be.gim.tov.osyris.model.werk.WerkOpdracht;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class OsyrisModelFunctions {
	private static final Log LOG = LogFactory
			.getLog(OsyrisModelFunctions.class);

	protected static final String GEEN_PETER_METER = "Geen PeterMeter toegewezen";

	@Inject
	protected ModelRepository modelRepository;
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected CacheProducer cacheProducer;

	/**
	 * Get waarden status StockMateriaal.
	 * 
	 * @return
	 */
	public List<Object[]> getStockMateriaalStates() {
		List<Object[]> stockMateriaalStates = new ArrayList<Object[]>();
		Object[] statusBesteld = { "1", "Besteld" };
		Object[] statusnietBesteld = { "0", "Niet besteld" };

		stockMateriaalStates.add(statusBesteld);
		stockMateriaalStates.add(statusnietBesteld);
		return stockMateriaalStates;
	}

	/**
	 * Get canonieke waardes Ja Nee.
	 * 
	 * @return
	 */
	public List<Object[]> getCanonicalBoolean() {
		List<Object[]> booleans = new ArrayList<Object[]>();
		Object[] boolTrue = { "1", "Ja" };
		Object[] boolFalse = { "0", "Nee" };

		booleans.add(boolTrue);
		booleans.add(boolFalse);
		return booleans;
	}

	/**
	 * Get imageCodes
	 * 
	 * @return
	 */
	public List<Object[]> getImageCodes() {
		List<Object[]> imageCodes = new ArrayList<Object[]>();
		Object[] code1 = { "1" };
		Object[] code2 = { "2" };
		Object[] code3 = { "3" };

		imageCodes.add(code1);
		imageCodes.add(code2);
		imageCodes.add(code3);
		return imageCodes;
	}

	/**
	 * Gets all TrajectTypes
	 * 
	 * @return
	 */
	public List<Object[]> getTrajectTypes() {
		List<Object[]> trajectTypes = new ArrayList<Object[]>();
		Collection<ModelClass> subClassesRoute = Collections.emptyList();
		subClassesRoute = modelRepository.getModelClass("Route")
				.getSubClasses();
		for (ModelClass modelClass : subClassesRoute) {
			Object[] object = { modelClass.getName(), modelClass.getLabel() };
			trajectTypes.add(object);
		}

		Collection<ModelClass> subClassesNetwerk = Collections.emptyList();
		subClassesNetwerk = modelRepository.getModelClass("NetwerkSegment")
				.getSubClasses();
		for (ModelClass modelClass : subClassesNetwerk) {
			Object[] object = { modelClass.getName(), modelClass.getLabel() };
			trajectTypes.add(object);
		}
		return trajectTypes;
	}

	/**
	 * Haalt de trajectTypes op afhankelijk van het gekozen controleOpdracht
	 * type
	 * 
	 * @param controleOpdrachtType
	 * @return
	 */
	public List<Object[]> getTrajectTypes(String controleOpdrachtType) {

		List<Object[]> trajectTypes = new ArrayList<Object[]>();

		if (controleOpdrachtType != null
				&& controleOpdrachtType.contains("route")) {
			Collection<ModelClass> subClassesRoute = Collections.emptyList();
			subClassesRoute = modelRepository.getModelClass("Route")
					.getSubClasses();
			for (ModelClass modelClass : subClassesRoute) {
				Object[] object = { modelClass.getName(), modelClass.getLabel() };
				trajectTypes.add(object);
			}
		}

		if (controleOpdrachtType != null
				&& controleOpdrachtType.contains("netwerk")) {
			Collection<ModelClass> subClassesNetwerk = Collections.emptyList();
			subClassesNetwerk = modelRepository.getModelClass("NetwerkSegment")
					.getSubClasses();
			for (ModelClass modelClass : subClassesNetwerk) {
				Object[] object = { modelClass.getName(), modelClass.getLabel() };
				trajectTypes.add(object);
			}
		}
		return trajectTypes;
	}

	/**
	 * Gets codes and labels for a given modelClass.
	 * 
	 * @param modelClassName
	 * @return
	 */
	@SuppressWarnings({ "deprecation" })
	public List<?> getCodeList(String modelClassName) {
		List<?> codeList = Collections.emptyList();

		try {
			codeList = ModelFunctions.searchEnumValues("OsyrisDataStore",
					modelClassName, "code", "label", null, null);

		} catch (IOException e) {
			LOG.error("Can not get list of codes for class " + modelClassName,
					e);
		}
		return codeList;
	}

	/**
	 * Zoeken gebruikers in een bepaalde groep.
	 * 
	 * @param groupName
	 * @return
	 */
	public List<ResourceName> getUsersInGroup(String groupName) {

		try {
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", groupName));

			if (group != null) {
				return group.getMembers();
			}
		} catch (IOException e) {
			LOG.error("Can not lookup users for group '" + groupName + "'.", e);
		}

		return Collections.emptyList();
	}

	/**
	 * Get the suggestions for users in a certain group.
	 * 
	 * @param groupName
	 * @return
	 */
	public List<? extends ResourceIdentifier> getSuggestions(String groupName) {

		List<ResourceName> users = getUsersInGroup(groupName);
		List<ResourceIdentifier> suggestions = new ArrayList<ResourceIdentifier>();
		suggestions.addAll(users);
		return suggestions;
	}

	/**
	 * Get suggestielijst voor PetersMeters.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getPeterMeterSuggestions() {

		return (List<Object[]>) cacheProducer.getCache(
				"PeterMeterSuggestionCache", new Transformer() {

					@Override
					public Object transform(Object key) {
						List<Object[]> suggestions = new ArrayList<Object[]>();
						try {
							Object[] geenPeterMeter = { GEEN_PETER_METER,
									GEEN_PETER_METER };

							List<ResourceName> users = getUsersInGroup("PeterMeter");
							for (ResourceName user : users) {
								User peterMeter = (User) modelRepository
										.loadObject(user);
								UserProfile profiel = (UserProfile) peterMeter
										.getAspect("UserProfile",
												modelRepository, true);
								Object[] object = {
										user,
										profiel.getFirstName() + " "
												+ profiel.getLastName() };
								suggestions.add(object);
							}
							suggestions.add(geenPeterMeter);

						} catch (IOException e) {
							LOG.error("Can not load user.", e);
						} catch (InstantiationException e) {
							LOG.error("Can not instantiate UserProfile.", e);
						} catch (IllegalAccessException e) {
							LOG.error("Illegal access at UserProfile.", e);
						}
						return suggestions;

					}
				}).get(null);
	}

	/**
	 * Get regios Oost Vlaanderen
	 * 
	 * @return
	 */
	public List<Object[]> getRegiosOostVlaanderen() {

		List<Object[]> regios = new ArrayList<Object[]>();
		Object[] code1 = { new ResourceKey("Regio@1"), "Leiestreek" };
		Object[] code2 = { new ResourceKey("Regio@2"), "Meetjesland" };
		Object[] code3 = { new ResourceKey("Regio@3"), "Vlaamse Ardennen" };
		Object[] code4 = { new ResourceKey("Regio@4"), "Waasland" };
		Object[] code5 = { new ResourceKey("Regio@5"), "Scheldeland" };
		Object[] code6 = { new ResourceKey("Regio@9"), "Zeeuws-Vlaanderen" };

		regios.add(code1);
		regios.add(code2);
		regios.add(code3);
		regios.add(code4);
		regios.add(code5);
		regios.add(code6);

		return regios;
	}

	/**
	 * Gets gemeentes
	 * 
	 * @return
	 */
	public List<?> getGemeentes() throws IOException {

		QueryBuilder builder = new QueryBuilder("Gemeente");
		builder.results(FilterUtils.properties("naam"));
		builder.groupBy(FilterUtils.properties("naam"));

		return modelRepository.searchObjects(builder.build(), true, true);
	}

	/**
	 * Gets traject namen
	 * 
	 * @return
	 */
	public List<?> getTrajectNamen(ResourceIdentifier regio, String trajectType)
			throws IOException {

		if (trajectType != null && !trajectType.isEmpty()) {
			QueryBuilder builder = new QueryBuilder(trajectType);
			if (regio != null) {
				builder.filter(FilterUtils.equal("regio", regio));
			}
			builder.results(FilterUtils.properties("naam"));
			builder.groupBy(FilterUtils.properties("naam"));

			return modelRepository.searchObjects(builder.build(), true, true);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Gets straatNamen
	 * 
	 * @return
	 */
	public List<?> getStraten() throws IOException {

		QueryBuilder builder = new QueryBuilder("Bord");
		builder.results(FilterUtils.properties("straatnaam"));
		builder.groupBy(FilterUtils.properties("straatnaam"));
		return modelRepository.searchObjects(builder.build(), true, true);
	}

	/**
	 * Zoekt knooppuntnummers
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<?> getKnooppuntNummers() throws IOException {
		QueryBuilder builder = new QueryBuilder("NetwerkKnooppunt");
		builder.results(FilterUtils.properties("nummer"));
		builder.groupBy(FilterUtils.properties("nummer"));
		builder.orderBy(new DefaultQueryOrderBy(FilterUtils.property("nummer")));
		return modelRepository.searchObjects(builder.build(), true, true);
	}

	/**
	 * Zoekt de meest recente datum van een statuswijziging voor wat betreft een
	 * ControleOpdracht
	 * 
	 * @param controleOpdracht
	 * @return
	 */
	public Date getDatumLaatsteWijziging(ControleOpdracht controleOpdracht) {
		List<Date> dates = new ArrayList<Date>();
		dates.add(controleOpdracht.getDatumTeControleren());
		dates.add(controleOpdracht.getDatumUitTeVoeren());
		dates.add(controleOpdracht.getDatumGerapporteerd());
		dates.add(controleOpdracht.getDatumGevalideerd());
		dates.removeAll(Collections.singleton(null));
		if (dates.size() > 0) {
			Date mostRecent = Collections.max(dates);
			return mostRecent;
		} else {
			return null;
		}
	}

	/**
	 * Zoekt de meest recente datum van een statuswijziging voor wat betreft een
	 * Melding
	 * 
	 * @param melding
	 * @return
	 */
	public Date getDatumLaatsteWijziging(Melding melding) {
		List<Date> dates = new ArrayList<Date>();
		dates.add(melding.getDatumVaststelling());
		dates.add(melding.getDatumGemeld());
		dates.add(melding.getDatumGevalideerd());
		dates.removeAll(Collections.singleton(null));
		if (dates.size() > 0) {
			Date mostRecent = Collections.max(dates);
			return mostRecent;
		} else {
			return null;
		}
	}

	/**
	 * Zoekt de meest recente datum van een statuswijziging voor wat betreft een
	 * Werkopdracht
	 * 
	 * @param werkopdracht
	 * @return
	 */
	public Date getDatumLaatsteWijziging(WerkOpdracht werkOpdracht) {
		List<Date> dates = new ArrayList<Date>();
		dates.add(werkOpdracht.getDatumTeControleren());
		dates.add(werkOpdracht.getDatumUitTeVoeren());
		dates.add(werkOpdracht.getDatumGerapporteerd());
		dates.add(werkOpdracht.getDatumGevalideerd());
		dates.add(werkOpdracht.getDatumGerapporteerd());
		dates.add(werkOpdracht.getDatumGeannuleerd());
		dates.removeAll(Collections.singleton(null));
		if (dates.size() > 0) {
			Date mostRecent = Collections.max(dates);
			return mostRecent;
		} else {
			return null;
		}
	}

	/**
	 * Zoekt de regionaam van een traject om te tonen in het overzichtsformulier
	 * 
	 * @param trajectId
	 * @return Het label van het traject.
	 */
	public String getTrajectRegio(ResourceIdentifier trajectId) {

		return (String) cacheProducer.getCache("trajectRegioCache",
				new Transformer() {

					@Override
					public Object transform(Object key) {

						try {
							Traject t = (Traject) modelRepository
									.loadObject((ResourceIdentifier) key);
							return modelRepository.getObjectLabel(t.getRegio());
						} catch (IOException e) {
							LOG.error("Can not load object.", e);
						}
						return null;
					}
				}).get(trajectId);
	}

	/**
	 * Zoekt het type traject om te tonen in het overzicht formulier
	 * 
	 * @param trajectId
	 * @return
	 */
	public String getTrajectType(ResourceIdentifier trajectId) {
		return LabelUtils.upperLowerSpaced(modelRepository.getObjectClassName(
				trajectId).toLowerCase());
	}

	/**
	 * Zoekt de naam van het traject om te tonen in het overzicht formulier
	 * 
	 * @param trajectId
	 * @return
	 */
	public String getTrajectNaam(ResourceIdentifier trajectId) {
		return modelRepository.getObjectLabel(trajectId);
	}

	/**
	 * Zoekt de gemeente waar het probleem van de werkopdracht zich bevindt.
	 * 
	 * @param probleem
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getWerkOpdrachtGemeente(Probleem probleem) {

		return (String) cacheProducer.getCache("WerkOpdrachtGemeenteCache",
				new Transformer() {

					@Override
					public Object transform(Object probleem) {

						try {
							if (probleem instanceof BordProbleem) {
								Bord bord = (Bord) modelRepository
										.loadObject(((BordProbleem) probleem)
												.getBord());
								return bord.getGemeente();
							}

							else if (probleem instanceof AnderProbleem) {
								DefaultQuery query = new DefaultQuery();
								query.setModelClassName("Gemeente");
								query.addFilter(FilterUtils.intersects("geom",
										((AnderProbleem) probleem).getGeom()));
								List<Gemeente> gemeentes = (List<Gemeente>) modelRepository
										.searchObjects(query, true, true);
								Gemeente gemeente = (Gemeente) modelRepository
										.getUniqueResult(gemeentes);
								return gemeente.getNaam();
							}

						} catch (IOException e) {
							LOG.error("Can not load traject.", e);
						}
						return null;
					}
				}).get(probleem);
	}

	/**
	 * Zoekt het Bordvolgnummer aan de hand van de resourceIdentifier
	 * 
	 * @param BordId
	 * @return
	 */
	public String getBordVolg(ResourceIdentifier BordId) {
		try {
			Bord bord = (Bord) modelRepository.loadObject(BordId);
			if (bord instanceof RouteBord) {
				return ((RouteBord) bord).getVolg();
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
		return null;
	}

	/**
	 * Zoekt de Bord straatnaam aan de hand van de resourceIdentifier
	 * 
	 * @param BordId
	 * @return
	 */
	public String getBordStraat(ResourceIdentifier BordId) {
		try {
			Bord bord = (Bord) modelRepository.loadObject(BordId);
			if (bord instanceof RouteBord) {
				return ((RouteBord) bord).getStraatnaam();
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
		return null;
	}

	/**
	 * Zoekt de straatnaam van het Bord aan de hand van de resourceIdentifier
	 * 
	 * @param BordId
	 * @return
	 */
	public String getBordStraatNaam(ResourceIdentifier BordId) {
		try {
			Bord bord = (Bord) modelRepository.loadObject(BordId);
			if (bord instanceof RouteBord) {
				return ((RouteBord) bord).getStraatnaam();
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
		return null;
	}

	/**
	 * Zoekt de uitvoerder aan de hand van de regioID
	 * 
	 * @param regio
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ResourceName zoekUitvoerder(ResourceIdentifier regioID) {
		try {
			Regio regio = (Regio) modelRepository.loadObject(regioID);
			ResourceIdentifier identifier = regio.getUitvoerder();
			DefaultQuery query = new DefaultQuery("UitvoerderProfiel");
			query.addFilter(FilterUtils.equal("bedrijf", identifier));
			List<UitvoerderProfiel> profiel = (List<UitvoerderProfiel>) modelRepository
					.searchObjects(query, false, false);
			User uitvoerder = (User) modelRepository.loadObject(profiel.get(0)
					.getFor());
			return modelRepository.getResourceName(uitvoerder);
		} catch (IOException e) {
			LOG.error("Can not load Traject.", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getWerkOpdrachten() {
		try {
			List<Object[]> opdrachten = new ArrayList<Object[]>();
			QueryBuilder builder = new QueryBuilder("WerkOpdracht");
			builder.results(FilterUtils.properties("id"));
			List<Long> list = (List<Long>) modelRepository.searchObjects(
					builder.build(), true, true);
			for (Long id : list) {
				Object[] object = {
						new ResourceKey("WerkOpdracht", id.toString()),
						id.toString() };
				opdrachten.add(object);
			}
			return opdrachten;
		} catch (IOException e) {
			LOG.error("Can not search WerkOpdracht ids.", e);
		}
		return null;
	}

	/**
	 * Zoekt de naam van de uitvoerder aan de hand van de ResourceIdentifier
	 * 
	 * @param id
	 */
	public String getUitvoerderNaam(ResourceIdentifier id) {
		String naam = null;
		try {
			User user = (User) modelRepository.loadObject(id);
			UitvoerderProfiel profiel = (UitvoerderProfiel) user.getAspect(
					"UitvoerderProfiel", modelRepository, true);
			UitvoerderBedrijf bedrijf = (UitvoerderBedrijf) modelRepository
					.loadObject(profiel.getBedrijf());
			naam = bedrijf.getNaam();
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate aspect.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at object.", e);
		}
		return naam;
	}

	/**
	 * Zoekt de regio waarmee het aangeduide probleem een intersectie heeft.
	 * 
	 * @param geometry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ResourceIdentifier searchRegioForProbleem(Geometry geometry) {
		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");
			query.addFilter(FilterUtils.intersects("geom", geometry));
			List<Regio> regios = (List<Regio>) modelRepository.searchObjects(
					query, true, true);
			Regio regio = (Regio) modelRepository.getUniqueResult(regios);
			return modelRepository.getResourceIdentifier(regio);

		} catch (IOException e) {
			LOG.error("Can not search regios.", e);
		}
		return null;
	}

	/**
	 * Zoekt verantwoordelijke medewerker voor een bepaald traject aan de hand
	 * van het MedewerkerProfiel.
	 * 
	 * @param traject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ResourceName zoekVerantwoordelijke(ResourceIdentifier traject) {
		try {
			Traject t = (Traject) modelRepository.loadObject(traject);
			List<User> users = new ArrayList<User>();
			List<User> medewerkers = new ArrayList<User>();
			users = (List<User>) modelRepository.searchObjects(
					new DefaultQuery("User"), true, true);
			for (User user : users) {
				if (userRepository.listGroupnames(user).contains("Medewerker")) {
					medewerkers.add(user);
				}
			}

			for (User u : medewerkers) {
				MedewerkerProfiel profiel = (MedewerkerProfiel) u.getAspect(
						"MedewerkerProfiel", modelRepository, true);
				if (profiel != null) {
					for (String trajectType : profiel.getTrajectType()) {
						if (trajectType.equals(t.getModelClass().getName())) {
							return modelRepository.getResourceName(u);
						}
					}
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Can not access object.", e);
		}
		return null;
	}
}
