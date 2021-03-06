package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.group.Group;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.core.functions.ModelFunctions;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.core.security.annotation.RunPrivileged;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Identity;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.codes.PeterMeterNaamCode;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.FietsNetwerkSegment;
import be.gim.tov.osyris.model.traject.Gemeente;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.RichtingEnum;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.traject.WandelNetwerkSegment;
import be.gim.tov.osyris.model.user.MedewerkerProfiel;
import be.gim.tov.osyris.model.user.UitvoerderBedrijf;
import be.gim.tov.osyris.model.user.UitvoerderProfiel;
import be.gim.tov.osyris.model.utils.DropdownListSorting;
import be.gim.tov.osyris.model.werk.StockMateriaal;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.ValidatieStatus;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import com.vividsolutions.jts.operation.distance.DistanceOp;

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
	protected static final Double BUFFER_BORD_METER = 3000.0;
	protected static final Double BUFFER_KNOOPPUNT_METER = 300.0;

	@Inject
	protected ModelRepository modelRepository;
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected CacheProducer cacheProducer;
	@Inject
	protected Identity identity;
	@Inject
	protected Preferences preferences;
	@Inject
	protected Messages messages;
	@Inject
	protected MailSender mailSender;

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

		Object[] boolFalse = { "0", "Nee" };
		Object[] boolTrue = { "1", "Ja" };

		booleans.add(boolFalse);
		booleans.add(boolTrue);
		return booleans;
	}

	/**
	 * Get imageCodes
	 * 
	 * @return
	 */
	public List<Object[]> getImageCodes() {

		List<Object[]> imageCodes = new ArrayList<Object[]>();
		Object[] code1 = { "1", "LINKS" };
		Object[] code2 = { "2", "RECHTS" };
		Object[] code3 = { "3", "RECHTDOOR" };

		imageCodes.add(code1);
		imageCodes.add(code2);
		imageCodes.add(code3);

		return imageCodes;
	}

	/**
	 * Get KpNummers
	 * 
	 * @return
	 */
	public List<Object[]> getKpNummers() {

		List<Object[]> kpNummerCodes = new ArrayList<Object[]>();

		for (int i = 1; i < 100; i++) {
			Object[] code = { Integer.valueOf(i), Integer.valueOf(i).toString() };
			kpNummerCodes.add(code);
		}

		return kpNummerCodes;
	}

	/**
	 * Ophalen alle trajectTypes met betrekking to controleOpdrachten.
	 * 
	 * @return
	 */
	public List<Object[]> getTrajectTypesCO() {

		List<Object[]> trajectTypes = new ArrayList<Object[]>();
		Collection<ModelClass> subClassesRoute = Collections.emptyList();
		subClassesRoute = modelRepository.getModelClass("Route")
				.getSubClasses();
		for (ModelClass modelClass : subClassesRoute) {
			Object[] object = { modelClass.getName(),
					modelClass.getLabel().toString() };
			trajectTypes.add(object);
		}

		Collection<ModelClass> subClassesNetwerkLus = Collections.emptyList();
		subClassesNetwerkLus = modelRepository.getModelClass("NetwerkLus")
				.getSubClasses();
		for (ModelClass modelClass : subClassesNetwerkLus) {
			Object[] object = { modelClass.getName(),
					modelClass.getLabel().toString() };
			trajectTypes.add(object);
		}

		Collections.sort(trajectTypes, new DropdownListSorting());
		return trajectTypes;
	}

	/**
	 * Ophalen alle trajectTypes met betrekking to werkOpdrachten.
	 * 
	 * @return
	 */
	public List<Object[]> getTrajectTypesWO() {

		List<Object[]> trajectTypes = new ArrayList<Object[]>();
		Collection<ModelClass> subClassesRoute = Collections.emptyList();
		subClassesRoute = modelRepository.getModelClass("Route")
				.getSubClasses();
		for (ModelClass modelClass : subClassesRoute) {
			Object[] object = { modelClass.getName(),
					modelClass.getLabel().toString() };
			trajectTypes.add(object);
		}

		Collection<ModelClass> subClassesNetwerkLus = Collections.emptyList();
		subClassesNetwerkLus = modelRepository.getModelClass("NetwerkLus")
				.getSubClasses();
		for (ModelClass modelClass : subClassesNetwerkLus) {
			Object[] object = { modelClass.getName(),
					modelClass.getLabel().toString() };
			trajectTypes.add(object);
		}

		Collection<ModelClass> subClassesNetwerkSegment = Collections
				.emptyList();
		subClassesNetwerkSegment = modelRepository.getModelClass(
				"NetwerkSegment").getSubClasses();
		for (ModelClass modelClass : subClassesNetwerkSegment) {
			Object[] object = { modelClass.getName(),
					modelClass.getLabel().toString() };
			trajectTypes.add(object);
		}

		Collections.sort(trajectTypes, new DropdownListSorting());
		return trajectTypes;
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
			Object[] object = { modelClass.getName(),
					modelClass.getLabel().toString() };
			trajectTypes.add(object);
		}

		Collection<ModelClass> subClassesNetwerk = Collections.emptyList();
		subClassesNetwerk = modelRepository.getModelClass("NetwerkSegment")
				.getSubClasses();
		for (ModelClass modelClass : subClassesNetwerk) {
			Object[] object = { modelClass.getName(),
					modelClass.getName().replace("NetwerkSegment", "netwerk") };
			trajectTypes.add(object);
		}

		Collections.sort(trajectTypes, new DropdownListSorting());
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
				Object[] object = { modelClass.getName(),
						modelClass.getLabel().toString() };
				trajectTypes.add(object);
			}
		}

		// Voor NetwerkControleOpdrachten werkt men met NetwerkLussen
		if (controleOpdrachtType != null
				&& controleOpdrachtType.contains("netwerk")) {
			Collection<ModelClass> subClassesNetwerkLus = Collections
					.emptyList();
			subClassesNetwerkLus = modelRepository.getModelClass("NetwerkLus")
					.getSubClasses();
			for (ModelClass modelClass : subClassesNetwerkLus) {
				Object[] object = { modelClass.getName(),
						modelClass.getLabel().toString() };
				trajectTypes.add(object);
			}
		}

		Collections.sort(trajectTypes, new DropdownListSorting());
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

	/*
	 * public List<?> getCodes(String modelClassName) {
	 * 
	 * try {
	 * 
	 * QueryBuilder builder = new QueryBuilder(modelClassName);
	 * builder.results(FilterUtils.properties("label"));
	 * builder.groupBy(FilterUtils.properties("label"));
	 * 
	 * return modelRepository.searchObjects(builder.build(), true, true);
	 * 
	 * } catch (IOException e) {
	 * LOG.error("Can not get list of codes for class " + modelClassName, e); }
	 * 
	 * return Collections.emptyList(); }
	 */

	/**
	 * Zoeken gebruikers in een bepaalde groep.
	 * 
	 * @param groupName
	 * @return
	 */
	@RunPrivileged
	public List<ResourceIdentifier> getUsersInGroup(String groupName) {

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
	 * Get suggestielijst voor een bepaalde groep.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getSuggestions(final String groupName) {

		return (List<Object[]>) cacheProducer.getCache(
				groupName + "SuggestionCache", new Transformer() {

					@Override
					public Object transform(Object key) {
						List<Object[]> suggestions = new ArrayList<Object[]>();

						try {
							List<User> users = getUserObjectsInGroup(groupName);

							for (User user : users) {

								UserProfile profiel = (UserProfile) user
										.getAspect("UserProfile",
												modelRepository, false);

								suggestions.add(new Object[] {
										modelRepository.getResourceName(user),
										profiel.getLastName() + " "
												+ profiel.getFirstName() });

								// Sorteren suggesties
								Collections.sort(suggestions,
										new DropdownListSorting());
							}

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
	 * Ophalen van de medewerkers en routdokters werkzaam bij TOV. Deze lijst
	 * wordt gebruikt bij het toewijzen van trajectverantwoordelijkheden.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getVerantwoordelijkenTOV() {

		return (List<Object[]>) cacheProducer.getCache(
				"VerantwoordelijkenTOVCache", new Transformer() {

					@Override
					public Object transform(Object key) {
						List<Object[]> verantwoordelijken = new ArrayList<Object[]>();

						try {

							// Use set to remove duplicates. Some users are
							// defined in multiple groups
							Set<User> users = new HashSet<User>();
							users.addAll(getUserObjectsInGroup("Medewerker"));
							users.addAll(getUserObjectsInGroup("Routedokter"));

							for (User user : users) {

								UserProfile profiel = (UserProfile) user
										.getAspect("UserProfile",
												modelRepository, false);

								verantwoordelijken.add(new Object[] {
										modelRepository.getResourceName(user),
										profiel.getLastName() + " "
												+ profiel.getFirstName() });

								// Sorteren suggesties
								Collections.sort(verantwoordelijken,
										new DropdownListSorting());
							}

						} catch (IOException e) {
							LOG.error("Can not load user.", e);
						} catch (InstantiationException e) {
							LOG.error("Can not instantiate UserProfile.", e);
						} catch (IllegalAccessException e) {
							LOG.error("Illegal access at UserProfile.", e);
						}

						return verantwoordelijken;
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
		Object[] code7 = { new ResourceKey("Regio@6"), "Brugse Ommeland" };
		Object[] code8 = { new ResourceKey("Regio@7"), "Groene Gordel" };
		Object[] code9 = { new ResourceKey("Regio@99"), "Wallonië" };

		regios.add(code1);
		regios.add(code2);
		regios.add(code3);
		regios.add(code4);
		regios.add(code5);
		regios.add(code6);
		regios.add(code7);
		regios.add(code8);
		regios.add(code9);

		Collections.sort(regios, new DropdownListSorting());
		return regios;
	}

	/**
	 * Get regios Oost Vlaanderen zichtbaar voor peterMeter voorkeuren
	 * 
	 * @return
	 */
	public List<Object[]> getRegiosOostVlaanderenVoorkeuren() {

		List<Object[]> regios = new ArrayList<Object[]>();
		Object[] code1 = { new ResourceKey("Regio@1"), "Leiestreek" };
		Object[] code2 = { new ResourceKey("Regio@2"), "Meetjesland" };
		Object[] code3 = { new ResourceKey("Regio@3"), "Vlaamse Ardennen" };
		Object[] code4 = { new ResourceKey("Regio@4"), "Waasland" };
		Object[] code5 = { new ResourceKey("Regio@5"), "Scheldeland" };

		regios.add(code1);
		regios.add(code2);
		regios.add(code3);
		regios.add(code4);
		regios.add(code5);

		Collections.sort(regios, new DropdownListSorting());
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
	@RunPrivileged
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
	 * Ophalen Trajectnamen aan de hand van de Bordtype modelklasse voor de
	 * editeer functie van borden.
	 * 
	 * @param modelClass
	 * @return
	 * @throws IOException
	 */
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public List<String> getTrajectNamen(ModelClass modelClass,
			ResourceIdentifier regio) throws IOException {

		List<String> result = new ArrayList<String>();

		if (modelClass != null) {
			// NetwerkBorden
			if (modelClass.getSuperClass().getName().equals("NetwerkBord")) {

				List<String> segmentNamen = new ArrayList<String>();
				QueryBuilder builder = new QueryBuilder(modelClass.getName()
						.replace("Bord", "Segment"));

				if (regio != null) {
					builder.addFilter(FilterUtils.equal("regio", regio));
				}
				builder.results(FilterUtils.properties("naam"));
				builder.groupBy(FilterUtils.properties("naam"));
				segmentNamen = (List<String>) modelRepository.searchObjects(
						builder.build(), true, true);

				List<String> lusNamen = new ArrayList<String>();
				builder = new QueryBuilder(modelClass.getName().replace("Bord",
						"Lus"));
				builder.results(FilterUtils.properties("naam"));
				builder.groupBy(FilterUtils.properties("naam"));
				lusNamen = (List<String>) modelRepository.searchObjects(
						builder.build(), true, true);

				result.addAll(segmentNamen);
				result.addAll(lusNamen);
			}
			// Borden
			if (modelClass.getSuperClass().getName().equals("RouteBord")) {

				QueryBuilder builder = new QueryBuilder(modelClass.getName()
						.replace("Bord", ""));
				if (regio != null) {
					builder.addFilter(FilterUtils.equal("regio", regio));
				}
				builder.results(FilterUtils.properties("naam"));
				builder.groupBy(FilterUtils.properties("naam"));
				result = (List<String>) modelRepository.searchObjects(
						builder.build(), true, true);
			}
		}

		else {

			QueryBuilder builder = new QueryBuilder("Traject");
			if (regio != null) {
				builder.addFilter(FilterUtils.equal("regio", regio));
			}
			builder.results(FilterUtils.properties("naam"));
			builder.groupBy(FilterUtils.properties("naam"));
			builder.orderBy(new DefaultQueryOrderBy(FilterUtils
					.property("naam")));
			result = (List<String>) modelRepository.searchObjects(
					builder.build(), true, true);
		}
		return result;
	}

	/**
	 * Gets straatNamen
	 * 
	 * @return
	 */
	@RunPrivileged
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
	@SuppressWarnings("unchecked")
	@RunPrivileged
	public List<Integer> getKnooppuntNummers() throws IOException {

		QueryBuilder builder = new QueryBuilder();
		builder.filter(FilterUtils.notEqual("nummer", 0, true));

		builder.results(FilterUtils.properties("nummer"));
		builder.groupBy(FilterUtils.properties("nummer"));
		builder.orderBy(new DefaultQueryOrderBy(FilterUtils.property("nummer")));

		return (List<Integer>) modelRepository.searchObjects(builder.build(),
				true, true);
	}

	/**
	 * Zoekt knooppuntnummers op basis van trajectType, regio en trajectnaam.
	 * 
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@RunPrivileged
	public List<Integer> getKnooppuntNummers(String trajectType,
			ResourceIdentifier regio, String naam) throws IOException {

		QueryBuilder builder = new QueryBuilder();
		builder.filter(FilterUtils.notEqual("nummer", 0, true));

		if (trajectType.contains("WandelNetwerk")) {
			builder.modelClassName("WandelNetwerkKnooppunt");
		}

		else if (trajectType.contains("FietsNetwerk")) {
			builder.modelClassName("FietsNetwerkKnooppunt");
		}

		if (regio != null) {
			builder.addFilter(FilterUtils.equal("regio", regio));
		}

		if (naam != null && !naam.isEmpty()) {
			builder.addFilter(FilterUtils.equal("naam", naam));
		}
		builder.results(FilterUtils.properties("nummer"));
		builder.groupBy(FilterUtils.properties("nummer"));
		builder.orderBy(new DefaultQueryOrderBy(FilterUtils.property("nummer")));

		return (List<Integer>) modelRepository.searchObjects(builder.build(),
				true, true);
	}

	/**
	 * Zoekt de regionaam van een traject om te tonen in het overzichtsformulier
	 * 
	 * @param trajectId
	 * @return Het label van het traject.
	 */
	@RunPrivileged
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
	@RunPrivileged
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

								if (gemeente != null) {
									return gemeente.getNaam();
								}
							}

						} catch (IOException e) {
							LOG.error("Can not load traject.", e);
						}
						return "Onbekend";
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

			if (bord != null) {
				return bord.getStraatnaam();
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
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public ResourceName zoekUitvoerder(ResourceIdentifier regioID) {

		try {
			Regio regio = (Regio) modelRepository.loadObject(regioID);
			ResourceIdentifier identifier = regio.getUitvoerder();

			DefaultQuery query = new DefaultQuery("UitvoerderProfiel");

			if (identifier != null) {
				query.addFilter(FilterUtils.equal("bedrijf", identifier));

				List<UitvoerderProfiel> profiel = (List<UitvoerderProfiel>) modelRepository
						.searchObjects(query, false, false);
				User uitvoerder = (User) modelRepository.loadObject(profiel
						.get(0).getFor());

				return modelRepository.getResourceName(uitvoerder);

			} else {
				User technischeDienst = userRepository.loadUser("tdtoerisme");

				if (technischeDienst != null) {
					return modelRepository.getResourceName(technischeDienst);
				}
			}

		} catch (IOException e) {
			LOG.error("Can not load Regio.", e);
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
	@RunPrivileged
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
	 * Indien de geometrie van een AnderProbleem een LineString is die met
	 * meerdere regios een intersectie heeft, wordt de regio genomen met de
	 * grootste intersectie.
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

			Regio regio = null;

			// Indien 1 regio
			if (regios.size() == 1) {
				regio = (Regio) modelRepository.getUniqueResult(regios);
			}

			// Indien intersectie met meerdere regios
			else if (regios.size() > 1) {
				Map<Regio, Double> intersections = new HashMap<Regio, Double>();

				for (Regio r : regios) {

					Geometry g1 = r.getGeom().intersection(geometry);
					intersections.put(r, g1.getLength());
				}

				double maxValueInMap = (Collections.max(intersections.values()));
				for (Entry<Regio, Double> entry : intersections.entrySet()) {
					if (entry.getValue() == maxValueInMap) {

						regio = entry.getKey();
					}
				}
			}

			return modelRepository.getResourceIdentifier(regio);

		} catch (IOException e) {
			LOG.error("Can not search regios.", e);
		}
		return null;
	}

	/**
	 * Zoekt verantwoordelijke medewerker voor een bepaald traject via de
	 * TrajectVerantwoordelijkheid. Is er geen medewerker gevonden dan valt het
	 * systeem terug op de oude manier van werken via de instellingen in de
	 * MedewerkerProfielen.
	 * 
	 * @param traject
	 * @return
	 */
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public ResourceName zoekVerantwoordelijke(ResourceIdentifier traject) {

		try {
			Traject t = (Traject) modelRepository.loadObject(traject);

			QueryBuilder builder = new QueryBuilder(
					"TrajectVerantwoordelijkheid");
			List<ResourceName> result = new ArrayList<ResourceName>();

			// Indien route haal de medewerker op uit de
			// TrajectVerantwoordelijkheid
			if (t instanceof Route) {
				builder.addFilter(FilterUtils.equal("trajectType", t
						.getModelClass().getName()));
				builder.results(FilterUtils.properties("medewerker"));

				result = (List<ResourceName>) modelRepository.searchObjects(
						builder.build(), false, false);
				if (!result.isEmpty()) {
					return result.get(0);
				}
			}

			// Indien FietsNetwerkSegment haal de medewerker op voor de regio
			// van het
			// traject
			else if (t instanceof FietsNetwerkSegment) {
				builder.addFilter(FilterUtils.equal("trajectType", t
						.getModelClass().getName()));
				builder.addFilter(FilterUtils.equal("regio", t.getRegio()));
				builder.results(FilterUtils.properties("medewerker"));

				result = (List<ResourceName>) modelRepository.searchObjects(
						builder.build(), false, false);
				if (!result.isEmpty()) {
					return result.get(0);
				}
			}

			// Indien WandelNetwerkSegment haal de medewerker op voor de
			// trajectNaam van het
			// traject
			else if (t instanceof WandelNetwerkSegment) {
				builder.addFilter(FilterUtils.equal("trajectType", t
						.getModelClass().getName()));
				builder.addFilter(FilterUtils.equal("trajectNaam", t.getNaam()));
				builder.results(FilterUtils.properties("medewerker"));

				result = (List<ResourceName>) modelRepository.searchObjects(
						builder.build(), false, false);
				if (!result.isEmpty()) {
					return result.get(0);
				}
			}

			else if (t instanceof NetwerkLus) {
				builder.addFilter(FilterUtils.equal("trajectType", t
						.getModelClass().getName().replace("Lus", "Segment")));
				builder.addFilter(FilterUtils.equal("regio", t.getRegio()));
				builder.results(FilterUtils.properties("medewerker"));

				result = (List<ResourceName>) modelRepository.searchObjects(
						builder.build(), false, false);
				if (!result.isEmpty()) {
					return result.get(0);
				}
			}

			// Indien geen medewerkers gevonden via het nieuwe systeem, fallback
			// op het oude systeem
			if (result.isEmpty()) {

				// Group medewerkers opzoeken
				DefaultQuery query = new DefaultQuery("Group");
				query.addFilter(FilterUtils.equal("groupname", "Medewerker"));
				List<Group> groups = new ArrayList<Group>();
				groups = (List<Group>) modelRepository.searchObjects(query,
						false, false);
				Group group = (Group) ModelRepository.getUniqueResult(groups);

				// Users uit medewerker group halen
				for (ResourceIdentifier id : group.getMembers()) {
					User medewerker = (User) modelRepository.loadObject(id);

					if (medewerker != null) {
						// Check welke Medewerker verantwoordelijk is voor het
						// gekozen trajectType
						MedewerkerProfiel profiel = (MedewerkerProfiel) medewerker
								.getAspect("MedewerkerProfiel",
										modelRepository, true);
						if (profiel != null) {
							for (String trajectType : profiel.getTrajectType()) {
								if (t.getModelClass().getName().contains("Lus")) {
									String className = t.getModelClass()
											.getName()
											.replace("Lus", "Segment");
									if (className.equals(trajectType)) {
										return modelRepository
												.getResourceName(medewerker);
									}
								} else if (trajectType.equals(t.getModelClass()
										.getName())) {
									return modelRepository
											.getResourceName(medewerker);
								}
							}
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

	/**
	 * Zoekt verantwoordelijke medewerker voor een bepaald traject aan de hand
	 * van het MedewerkerProfiel.
	 * 
	 * @param traject
	 * @return
	 */
	/*
	 * @RunPrivileged
	 * 
	 * @SuppressWarnings("unchecked") public ResourceName
	 * zoekVerantwoordelijke(ResourceIdentifier traject) {
	 * 
	 * try { Traject t = (Traject) modelRepository.loadObject(traject);
	 * 
	 * // Group medewerkers opzoeken DefaultQuery query = new
	 * DefaultQuery("Group"); query.addFilter(FilterUtils.equal("groupname",
	 * "Medewerker")); List<Group> groups = new ArrayList<Group>(); groups =
	 * (List<Group>) modelRepository.searchObjects(query, false, false); Group
	 * group = (Group) ModelRepository.getUniqueResult(groups);
	 * 
	 * // Users uit medewerker group halen for (ResourceIdentifier id :
	 * group.getMembers()) { User medewerker = (User)
	 * modelRepository.loadObject(id);
	 * 
	 * if (medewerker != null) { // Check welke Medewerker verantwoordelijk is
	 * voor het // gekozen trajectType MedewerkerProfiel profiel =
	 * (MedewerkerProfiel) medewerker .getAspect("MedewerkerProfiel",
	 * modelRepository, true); if (profiel != null) { for (String trajectType :
	 * profiel.getTrajectType()) { if
	 * (t.getModelClass().getName().contains("Lus")) { String className =
	 * t.getModelClass().getName() .replace("Lus", "Segment"); if
	 * (className.equals(trajectType)) { return modelRepository
	 * .getResourceName(medewerker); } } else if
	 * (trajectType.equals(t.getModelClass() .getName())) { return
	 * modelRepository .getResourceName(medewerker); } } } } }
	 * 
	 * } catch (IOException e) { LOG.error("Can not load object.", e); } catch
	 * (InstantiationException e) { LOG.error("Can not instantiate object.", e);
	 * } catch (IllegalAccessException e) { LOG.error("Can not access object.",
	 * e); } return null; }
	 */

	/**
	 * DEPRECATED NIET MEER GEBRUIKT
	 * 
	 * Zoekt verantwoordelijke medewerker voor een bepaald trajectType aan de
	 * hand van het MedewerkerProfiel.
	 * 
	 * @param traject
	 * @return
	 */
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public ResourceName zoekVerantwoordelijke(String trajectType) {

		try {

			// Group medewerkers opzoeken
			DefaultQuery query = new DefaultQuery("Group");
			query.addFilter(FilterUtils.equal("groupname", "Medewerker"));
			List<Group> groups = new ArrayList<Group>();
			groups = (List<Group>) modelRepository.searchObjects(query, false,
					false);
			Group group = (Group) ModelRepository.getUniqueResult(groups);

			// Users uit medewerker group halen
			for (ResourceIdentifier id : group.getMembers()) {
				User medewerker = (User) modelRepository.loadObject(id);

				if (medewerker != null) {
					// Check welke Medewerker verantwoordelijk is voor het
					// gekozen trajectType
					MedewerkerProfiel profiel = (MedewerkerProfiel) medewerker
							.getAspect("MedewerkerProfiel", modelRepository,
									true);
					if (profiel != null) {
						for (String type : profiel.getTrajectType()) {

							if (type.equals(trajectType)) {
								return modelRepository
										.getResourceName(medewerker);
							}
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

	/**
	 * Validaties voor WerkOpdrachten.
	 * 
	 * @return
	 */
	public List<Object[]> getWerkOpdrachtValidatie() {

		List<Object[]> validaties = new ArrayList<Object[]>();
		Object[] code1 = { ValidatieStatus.OPNIEUW_UITVOEREN,
				"Opnieuw uitvoeren" };
		Object[] code2 = { ValidatieStatus.NIET_OPNIEUW_UITVOEREN,
				"Niet opnieuw uitvoeren" };
		Object[] code3 = { ValidatieStatus.LATER_OPNIEUW_UITVOEREN_VANAF,
				"Later opnieuw uitvoeren vanaf..." };
		Object[] code4 = { ValidatieStatus.FOUT_GERAPPORTEERD,
				"Fout gerapporteerd" };
		Object[] code5 = { ValidatieStatus.OK_VOOR_FACTURATIE,
				"OK voor facturatie" };

		validaties.add(code1);
		validaties.add(code2);
		validaties.add(code3);
		validaties.add(code4);
		validaties.add(code5);

		Collections.sort(validaties, new DropdownListSorting());
		return validaties;
	}

	/**
	 * Zoeken subcategorie aan de hand van de parent categorie.
	 * 
	 * @param categorie
	 * @return
	 * @throws IOException
	 */
	public List<?> getStockSubCategories(String categorie) throws IOException {

		if (categorie != null && !categorie.isEmpty()) {
			QueryBuilder builder = new QueryBuilder("StockMateriaal");
			builder.filter(FilterUtils.equal("categorie", categorie));
			builder.results(FilterUtils.properties("subCategorie"));
			builder.groupBy(FilterUtils.properties("subCategorie"));

			return modelRepository.searchObjects(builder.build(), false, false,
					false);

		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Zoeken van van StockMateriaal types aan de hand van de opgegeven
	 * categorie en subcategorie
	 * 
	 * @param categorie
	 * @param subCategorie
	 * @return
	 * @throws IOException
	 */
	public List<?> getStockTypes(String categorie, String subCategorie)
			throws IOException {

		if (categorie == null) {
			return Collections.emptyList();
		}

		if (subCategorie != null && !subCategorie.isEmpty()) {
			QueryBuilder builder = new QueryBuilder("StockMateriaal");
			if (categorie != null) {
				builder.filter(FilterUtils.equal("categorie", categorie));
			}
			builder.filter(FilterUtils.equal("subCategorie", subCategorie));
			builder.results(FilterUtils.properties("type"));
			builder.groupBy(FilterUtils.properties("type"));

			return modelRepository.searchObjects(builder.build(), false, false,
					false);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Zoeken van StockMateriaalNamen aan de hand van de opegegven categorie,
	 * subcategorie en type.
	 * 
	 * @param categorie
	 * @param subCategorie
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public List<?> getStockMateriaalNamen(String categorie,
			String subCategorie, String type) throws IOException {

		if (categorie == null) {
			return Collections.emptyList();
		}

		if (subCategorie != null && !subCategorie.isEmpty()) {
			QueryBuilder builder = new QueryBuilder("StockMateriaal");
			if (type != null) {
				builder.filter(FilterUtils.equal("type", type));
			}
			if (categorie != null) {
				builder.filter(FilterUtils.equal("categorie", categorie));
			}
			builder.filter(FilterUtils.equal("subCategorie", subCategorie));
			builder.results(FilterUtils.properties("naam"));
			builder.groupBy(FilterUtils.properties("naam"));

			return modelRepository.searchObjects(builder.build(), false, false,
					false);

		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Filteren van de beschikbare Regios voor de ingelogde uitvoerder.
	 * 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getRegiosUitvoerder() {

		List<Object[]> uitvoerderRegios = new ArrayList<Object[]>();

		try {
			if (identity.inGroup("Uitvoerder", "CUSTOM")) {
				User user = (User) modelRepository.loadObject(new ResourceName(
						"User", identity.getUser().getKey()));

				UitvoerderProfiel profiel = (UitvoerderProfiel) modelRepository
						.getAspect(modelRepository
								.getModelClass("UitvoerderProfiel"), user, true);

				if (!profiel.getBedrijf().equals(
						new ResourceKey("UitvoerderBedrijf", "3"))) {

					QueryBuilder builder = new QueryBuilder("Regio");
					builder.addFilter(FilterUtils.equal("uitvoerder",
							profiel.getBedrijf()));
					// builder.results(FilterUtils.properties("naam"));
					// builder.groupBy(FilterUtils.properties("naam"));
					List<Regio> regios = (List<Regio>) modelRepository
							.searchObjects(builder.build(), false, false);

					for (Regio regio : regios) {
						Object[] object = {
								new ResourceKey("Regio", regio.getId()
										.toString()), regio.getNaam() };
						uitvoerderRegios.add(object);
					}
				}
			}

			if (uitvoerderRegios.isEmpty()) {
				uitvoerderRegios = getRegiosOostVlaanderen();
			}

			Collections.sort(uitvoerderRegios, new DropdownListSorting());

		} catch (IOException e) {
			LOG.error("Can not search Regio.", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate User.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at object.", e);
		}
		return uitvoerderRegios;
	}

	/**
	 * Ophalen ResourceIdentifiers van Routes
	 * 
	 * @return
	 */
	public List<ResourceIdentifier> getRoutes() {
		List<ResourceIdentifier> ids = Collections.emptyList();

		try {
			DefaultQuery query = new DefaultQuery("Route");
			List<Traject> routes = (List<Traject>) modelRepository
					.searchObjects(query, false, false);

			for (Traject t : routes) {
				ids.add(modelRepository.getResourceIdentifier(t));
			}
		} catch (IOException e) {
			LOG.error("Can not search Routes.", e);
		}
		return ids;
	}

	/**
	 * Get Gefilterede trajectnamen voor cascading dropdowns in zoekfunctie
	 * overzicht forms.
	 * 
	 * @return
	 */
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public List<Object[]> getTrajectNamenSearch(ResourceIdentifier regio,
			String trajectType) {

		try {
			if (trajectType != null && !trajectType.isEmpty()) {

				QueryBuilder builder = new QueryBuilder(trajectType);

				if (regio != null) {
					builder.filter(FilterUtils.equal("regio", regio));
				}

				List<Traject> trajecten = (List<Traject>) modelRepository
						.searchObjects(builder.build(), true, true);

				List<Object[]> namen = new ArrayList<Object[]>();

				for (Traject t : trajecten) {
					Object[] object = {
							new ResourceKey("Traject", t.getId().toString()),
							t.getNaam() };
					namen.add(object);
				}

				Collections.sort(namen, new DropdownListSorting());
				return namen;
			}

		} catch (IOException e) {
			LOG.error("Can not search " + trajectType + ".", e);
		}
		return Collections.emptyList();
	}

	/**
	 * Get Gefilterede trajectnamen voor cascading dropdowns in zoekfunctie
	 * overzicht forms.
	 * 
	 * @return
	 */
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public List<Object[]> getTrajectNamenNetwerkSearch(
			ResourceIdentifier regio, String trajectType) {

		try {
			if (trajectType != null && !trajectType.isEmpty()) {

				QueryBuilder builder = new QueryBuilder(trajectType);

				if (regio != null) {
					builder.filter(FilterUtils.equal("regio", regio));
				}
				builder.results(FilterUtils.properties("naam"));
				builder.groupBy(FilterUtils.properties("naam"));

				List<String> results = (List<String>) modelRepository
						.searchObjects(builder.build(), true, true);

				List<Object[]> namen = new ArrayList<Object[]>();

				for (String naam : results) {
					Object[] object = { naam, naam };
					namen.add(object);
				}

				Collections.sort(namen, new DropdownListSorting());
				return namen;
			}

		} catch (IOException e) {
			LOG.error("Can not search " + trajectType + ".", e);
		}
		return Collections.emptyList();
	}

	/**
	 * Zoekt de regioId van een traject
	 * 
	 * @param trajectId
	 * @return Het label van het traject.
	 */
	public ResourceIdentifier getTrajectRegioId(ResourceIdentifier trajectId) {

		try {
			Traject t = (Traject) modelRepository.loadObject(trajectId);

			return t.getRegio();
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
		return null;

	}

	/**
	 * Ophalen URL voor Routedokter Help pagina.
	 * 
	 * @return
	 */
	public String getHelpUrlRoutedokter() {
		String contextPath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestContextPath();
		return contextPath + "/web/view/routedokterhelp";
	}

	/**
	 * Ophalen URL voor Help pagina voor ingelogde gebruikers.
	 * 
	 * @return
	 */
	public String getHelpUrl() {
		String contextPath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestContextPath();
		return contextPath + "/web/view/help";
	}

	/**
	 * Ophalen User objecten die tot een bepaalde groep behoren.
	 * 
	 * @return
	 */
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public List<User> getUserObjectsInGroup(String groupName) {

		try {
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", groupName));

			return (List) modelRepository.loadObjects(group.getMembers());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Ophalen volgorde NetwerkBorden in een lus.
	 * 
	 * @param lus
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Bord> getNetwerkBordVolgordeLus(NetwerkLus lus) {

		if (lus != null) {
			return (List<Bord>) cacheProducer.getCache(
					"NetwerkBordVolgordeLus", new Transformer() {

						@Override
						public Object transform(Object lus) {

							try {
								List<ResourceIdentifier> segmenten = ((NetwerkLus) lus)
										.getSegmenten();

								List<Bord> result = new ArrayList<Bord>(
										segmenten.size() * 6);

								// ---------------
								for (int i = 0; i < segmenten.size(); i++) {
									NetwerkSegment current = (NetwerkSegment) modelRepository
											.loadObject(segmenten.get(i));
									NetwerkSegment next = (NetwerkSegment) modelRepository.loadObject(segmenten.get(i != segmenten
											.size() - 1 ? i + 1 : 0));

									ResourceIdentifier naarKpId = current
											.getNaarKnooppunt();

									RichtingEnum richting = naarKpId
											.equals(next.getVanKnooppunt())
											|| naarKpId.equals(next
													.getNaarKnooppunt()) ? RichtingEnum.FT
											: RichtingEnum.TF;

									QueryBuilder builder = new QueryBuilder(
											"NetwerkBord");

									builder.addFilter(FilterUtils.equal(
											"segmenten",
											modelRepository
													.getResourceIdentifier(current)));
									builder.addFilter(FilterUtils.equal(
											"richting", richting.toString()));

									builder.addFilter(FilterUtils.equal(
											"actief", "1"));

									List<Bord> borden = (List<Bord>) modelRepository
											.searchObjects(builder.build(),
													false, false);

									if (borden != null && !borden.isEmpty()) {
										Geometry geom = GeometryUtils
												.connect(current.getGeom());

										boolean reverse = richting == RichtingEnum.TF;

										// Controleren of het segment omgekeerd
										// is ingetekend.
										NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
												.loadObject(naarKpId);
										if (naarKp != null
												&& naarKp.getGeom() != null) {
											Geometry naarKpGeom = naarKp
													.getGeom();
											if (naarKpGeom.distance(GeometryUtils
													.getFirstPoint((LineString) geom)) < naarKpGeom.distance(GeometryUtils
													.getLastPoint((LineString) geom))) {
												reverse = !reverse;
											}
										}

										LocationIndexedLine indexedLine = new LocationIndexedLine(
												geom);

										Comparator<Double> comparator = new ComparableComparator();
										if (reverse) {
											comparator = new ReverseComparator(
													comparator);
										}

										Map<Double, Bord> bordByFractie = new TreeMap<Double, Bord>(
												comparator);

										for (Bord bord : borden) {
											LinearLocation localion = indexedLine
													.project(bord.getGeom()
															.getCoordinate());
											double fractie = localion
													.getSegmentIndex()
													+ localion
															.getSegmentFraction();
											bordByFractie.put(fractie, bord);
										}

										result.addAll(bordByFractie.values());
									}
								}

								return result;
							} catch (IOException e) {
								LOG.error("Can not search segmenten.", e);
							}

							return Collections.emptyList();
						}
					}).get(lus);
		} else {
			return null;
		}
	}

	/**
	 * Verstuurt confirmatie mail naar de melder en de medewerker TOV
	 * 
	 * @param melding
	 * @throws Exception
	 */
	@RunPrivileged
	public void sendConfirmationMailMelding(Melding melding) {

		try {
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("preferences", preferences);
			variables.put("firstname", melding.getVoornaam());
			variables.put("lastname", melding.getNaam());
			variables.put("phone", melding.getTelefoon());
			variables.put("status", melding.getStatus());
			if (melding.getProbleem() instanceof BordProbleem) {
				variables.put("categorie",
						((BordProbleem) melding.getProbleem()).getType());
			} else if (melding.getProbleem() instanceof AnderProbleem) {
				variables.put("categorie",
						((AnderProbleem) melding.getProbleem()).getCategorie());
			}
			variables.put("commentaar", melding.getProbleem().getCommentaar());

			// Mail naar melder
			String mailServiceStatusMelder = DefaultConfiguration.instance()
					.getString("service.mail.melding.melder");

			if (mailServiceStatusMelder.equalsIgnoreCase("on")) {

				mailSender.sendMail(preferences.getNoreplyEmail(),
						Collections.singleton(melding.getEmail()),
						"/META-INF/resources/core/mails/confirmMelding.fmt",
						variables);

				messages.info("Er is een bevestigingsmail gestuurd naar "
						+ melding.getEmail() + ".");
			}

			// Ophalen emailadres Medewerker
			UserProfile profiel = (UserProfile) modelRepository.loadAspect(
					modelRepository.getModelClass("UserProfile"),
					modelRepository.loadObject(melding.getMedewerker()));
			String medewerkerEmail = profiel.getEmail();

			// Mail naar Medewerker TOV
			String mailServiceStatusMedewerker = DefaultConfiguration
					.instance().getString("service.mail.melding.medewerker");

			if (mailServiceStatusMedewerker.equalsIgnoreCase("on")) {

				mailSender.sendMail(preferences.getNoreplyEmail(),
						Collections.singleton(medewerkerEmail),
						"/META-INF/resources/core/mails/confirmMelding.fmt",
						variables);

				// Debug
				// messages.info("Er is een bevestigingsmail gestuurd naar de verantwoordelijke TOV "
				// + medewerkerEmail + ".");
			}

			// Testmail
			// if (mailServiceStatusMedewerker.equalsIgnoreCase("off")) {

			// String testEmail = DefaultConfiguration.instance().getString(
			// "service.mail.testEmail");

			// mailSender.sendMail(preferences.getNoreplyEmail(),
			// Collections.singleton(testEmail),
			// "/META-INF/resources/core/mails/confirmMelding.fmt",
			// variables);
			// }

		} catch (IOException e) {

			LOG.error("Can not load object.", e);
		} catch (Exception e) {

			LOG.error("Can not send email.", e);
			messages.error("Fout bij het versturen van bevestigingsmail");
		}
	}

	/**
	 * Stuurt een email naar de Routedokter wanneer een stock item onder de
	 * minimum limiet zit.
	 * 
	 * @param stockMateriaal
	 */
	@RunPrivileged
	public void sendMailStockMateriaalLimiet(StockMateriaal stockMateriaal) {

		try {
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("preferences", preferences);
			variables.put("magazijn", stockMateriaal.getMagazijn());
			variables.put("categorie", stockMateriaal.getCategorie());
			variables.put("subCategorie", stockMateriaal.getSubCategorie());
			variables.put("type", stockMateriaal.getType());
			variables.put("naam", stockMateriaal.getNaam());
			variables.put("nummer", stockMateriaal.getNummer());
			variables.put("inStock", stockMateriaal.getInStock());
			variables.put("min", stockMateriaal.getMin());
			variables.put("max", stockMateriaal.getMax());

			// Ophalen emailadres Routedokters
			List<ResourceIdentifier> users = getUsersInGroup("RouteDokter");

			for (ResourceIdentifier user : users) {

				User routeDokter = (User) modelRepository.loadObject(user);
				UserProfile profiel = (UserProfile) routeDokter.getAspect(
						"UserProfile", modelRepository, true);

				mailSender.sendMail(preferences.getNoreplyEmail(),
						Collections.singleton(profiel.getEmail()),
						"/META-INF/resources/core/mails/stockAlert.fmt",
						variables);
			}

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		} catch (Exception e) {
			LOG.error("Can not send email.", e);
		}
	}

	/**
	 * Ophalen van de PeterMeter namen (naam en voornaam).
	 * 
	 * @param hasGeenPeterMeter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getPeterMeterNaamCodes(boolean hasGeenPeterMeter) {

		List<Object[]> peterMeterNaamCodes = new ArrayList<Object[]>();

		try {
			QueryBuilder builder = new QueryBuilder("PeterMeterNaamCode");
			builder.orderBy(new DefaultQueryOrderBy(FilterUtils
					.property("label")));

			List<PeterMeterNaamCode> codeList = (List<PeterMeterNaamCode>) modelRepository
					.searchObjects(builder.build(), false, false);

			for (PeterMeterNaamCode code : codeList) {

				Object[] object = { code.getCode(), code.getLabel() };
				peterMeterNaamCodes.add(object);
			}

			if (hasGeenPeterMeter) {

				Object[] geenPeterMeter = { GEEN_PETER_METER, GEEN_PETER_METER };

				peterMeterNaamCodes.add(0, geenPeterMeter);
			}

			return peterMeterNaamCodes;

		} catch (IOException e) {
			LOG.error("Can not load PeterMeterNaamCodes.", e);
		}
		return peterMeterNaamCodes;
	}

	/**
	 * Checken of een PeterMeterNaamCode al bestaat of niet.
	 * 
	 * @param resourceName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean checkPeterMeterNaamCodeExists(ResourceName resourceName) {

		try {
			QueryBuilder builder = new QueryBuilder("PeterMeterNaamCode");
			builder.addFilter(FilterUtils.equal("code", resourceName.toString()));

			List<PeterMeterNaamCode> codes = (List<PeterMeterNaamCode>) modelRepository
					.searchObjects(builder.build(), false, false);

			if (codes.isEmpty()) {
				return false;
			}

			return true;

		} catch (IOException e) {
			LOG.error("Can not search PeterMeterNaamCode", e);
		}
		return false;
	}

	/**
	 * Bepalen van de Gemeente waarin een bord zich bevindt.
	 * 
	 * @param geometry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getGemeenteForBord(Bord bord) {

		try {

			if (bord.getGeom() != null) {

				DefaultQuery query = new DefaultQuery();
				query.setModelClassName("Gemeente");
				query.addFilter(FilterUtils.intersects("geom", bord.getGeom()));

				List<Gemeente> gemeentes = (List<Gemeente>) modelRepository
						.searchObjects(query, true, true);

				if (gemeentes.size() == 1) {
					Gemeente gemeente = (Gemeente) modelRepository
							.getUniqueResult(gemeentes);
					if (gemeente != null) {
						return gemeente.getNaam();
					}
				}
			}

		} catch (IOException e) {
			LOG.error("Can not search Gemeente", e);
		}

		return null;
	}

	/**
	 * Bepalen van de Regio waarin een bord zich bevindt.
	 * 
	 * @param geometry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Regio getRegioForBord(Bord bord) {

		try {
			if (bord.getGeom() != null) {

				if (bord instanceof NetwerkBord) {
					NetwerkBord netwerkBord = (NetwerkBord) bord;

					if (netwerkBord.getBordType() != null) {
						if (netwerkBord.getBordType().equals("knooppunt")
								&& netwerkBord.getKpid0() != null) {
							NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
									.loadObject(netwerkBord.getKpid0());
							return (Regio) modelRepository.loadObject(knooppunt
									.getRegio());
						}
					}
				}

				DefaultQuery query = new DefaultQuery();
				query.setModelClassName("Regio");
				query.addFilter(FilterUtils.intersects("geom", bord.getGeom()));

				List<Regio> regios = (List<Regio>) modelRepository
						.searchObjects(query, true, true);
				Regio regio = (Regio) modelRepository.getUniqueResult(regios);

				return regio;
			}

		} catch (IOException e) {
			LOG.error("Can not search Regio", e);
		}

		return null;
	}

	/**
	 * Bepalen van de Route waartoe het RouteBord zich het dichtst bij bevindt.
	 * 
	 * @param geometry
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Traject getRouteForRouteBord(RouteBord bord) throws IOException {

		Route route = null;

		DefaultQuery query = new DefaultQuery();
		query.setModelClassName(bord.getModelClass().getName()
				.replace("Bord", ""));

		// Query op buffer 3km rond bord
		query.addFilter(FilterUtils.intersects(bord.getGeom().buffer(
				BUFFER_BORD_METER)));

		List<Route> routes = (List<Route>) modelRepository.searchObjects(query,
				false, false);

		Map<Route, Double> distances = new HashMap<Route, Double>();

		for (Route r : routes) {

			double distance = DistanceOp.distance(bord.getGeom(), r.getGeom());
			distances.put(r, distance);
		}

		double minValueInMap = (Collections.min(distances.values()));
		for (Entry<Route, Double> entry : distances.entrySet()) {
			if (entry.getValue() == minValueInMap) {

				route = entry.getKey();
			}
		}

		return route;
	}

	/**
	 * Bepalen van de Segmenten waartoe het NetwerkBord zich het dichtsbij
	 * bevindt.
	 * 
	 * @param geometry
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public List<ResourceIdentifier> getSegmentenForNetwerkBord(NetwerkBord bord)
			throws IOException {

		List<ResourceIdentifier> segmentIds = new ArrayList<ResourceIdentifier>();

		if (bord.getRegio() != null && bord.getGeom() != null) {

			DefaultQuery query = new DefaultQuery();
			query.setModelClassName(bord.getModelClass().getName()
					.replace("Bord", "Segment"));

			// Query op buffer 3km rond bord
			query.addFilter(FilterUtils.intersects(bord.getGeom().buffer(
					BUFFER_BORD_METER)));

			List<NetwerkSegment> segmenten = (List<NetwerkSegment>) modelRepository
					.searchObjects(query, false, false);

			Map<ResourceIdentifier, Double> distances = new HashMap<ResourceIdentifier, Double>();

			for (NetwerkSegment s : segmenten) {
				double distance = DistanceOp.distance(bord.getGeom(),
						s.getGeom());
				distances.put(modelRepository.getResourceIdentifier(s),
						distance);
			}

			if (!segmenten.isEmpty()) {

				double minValueInMap = (Collections.min(distances.values()));

				for (Entry<ResourceIdentifier, Double> entry : distances
						.entrySet()) {
					if (entry.getValue() == minValueInMap) {

						ResourceIdentifier minSegmentId = entry.getKey();
						segmentIds.add(minSegmentId);
					}
				}
			}
		}
		return segmentIds;

	}

	/**
	 * Zoeken van van de naam van het dichtsbijzijnde WNW NetwerkSegment bij een
	 * NetwerkKnooppunt.
	 * 
	 * @param knooppunt
	 * @return
	 * @throws IOException
	 */
	public String getNaamForWandelNetwerkKnooppunt(NetwerkKnooppunt knooppunt)
			throws IOException {

		String naam = null;
		if (knooppunt.getGeom() != null) {

			DefaultQuery query = new DefaultQuery();
			query.setModelClassName(knooppunt.getModelClass().getName()
					.replace("Knooppunt", "Segment"));

			// Query op buffer 300m rond knooppunt
			query.addFilter(FilterUtils.intersects(knooppunt.getGeom().buffer(
					BUFFER_KNOOPPUNT_METER)));

			List<NetwerkSegment> segmenten = (List<NetwerkSegment>) modelRepository
					.searchObjects(query, false, false);

			Map<ResourceIdentifier, Double> distances = new HashMap<ResourceIdentifier, Double>();

			for (NetwerkSegment s : segmenten) {
				double distance = DistanceOp.distance(knooppunt.getGeom(),
						s.getGeom());
				distances.put(modelRepository.getResourceIdentifier(s),
						distance);
			}

			if (!segmenten.isEmpty()) {

				double minValueInMap = (Collections.min(distances.values()));

				for (Entry<ResourceIdentifier, Double> entry : distances
						.entrySet()) {
					if (entry.getValue() == minValueInMap) {

						ResourceIdentifier minSegmentId = entry.getKey();
						NetwerkSegment segment = (NetwerkSegment) modelRepository
								.loadObject(minSegmentId);
						naam = segment.getNaam();
					}
				}
			}
		}
		return naam;
	}

	public Bord getNearestBord(Geometry geom, ResourceIdentifier traject)
			throws IOException {

		if (geom != null) {
			Traject t = (Traject) modelRepository.loadObject(traject);
			DefaultQuery query = new DefaultQuery();
			List<ResourceIdentifier> segmentIds = new ArrayList<ResourceIdentifier>();
			segmentIds.add(traject);

			if (t instanceof Route) {
				query.setModelClassName(t.getModelClass().getName()
						.concat("Bord"));
				query.addFilter(FilterUtils.equal("route", traject));
			}

			if (t instanceof NetwerkSegment) {
				query.setModelClassName(t.getModelClass().getName()
						.replace("Segment", "Bord"));
				query.addFilter(FilterUtils.in("segmenten", segmentIds));
			}

			if (t instanceof NetwerkLus) {
				query.setModelClassName(t.getModelClass().getName()
						.replace("Lus", "Bord"));
				query.addFilter(FilterUtils.in("segmenten",
						((NetwerkLus) t).getSegmenten()));
			}

			// Query op buffer 3000m rond het AnderProbleem punt
			query.addFilter(FilterUtils.intersects(geom
					.buffer(BUFFER_BORD_METER)));

			List<Bord> borden = (List<Bord>) modelRepository.searchObjects(
					query, false, false);

			Map<ResourceIdentifier, Double> distances = new HashMap<ResourceIdentifier, Double>();

			for (Bord bord : borden) {
				double distance = DistanceOp.distance(geom, bord.getGeom());
				distances.put(modelRepository.getResourceIdentifier(bord),
						distance);
			}

			if (!borden.isEmpty()) {

				double minValueInMap = (Collections.min(distances.values()));

				for (Entry<ResourceIdentifier, Double> entry : distances
						.entrySet()) {
					if (entry.getValue() == minValueInMap) {

						ResourceIdentifier minBordId = entry.getKey();
						return (Bord) modelRepository.loadObject(minBordId);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Bepalen van de Regio waarin een segment zich bevindt. Indien een segment
	 * meerdere regios doorkruist, wordt de regio genomen waar de lijn
	 * intersectie het grootst is.
	 * 
	 * @param segment
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Regio getRegioForSegment(NetwerkSegment segment) {

		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");
			query.addFilter(FilterUtils.intersects("geom", segment.getGeom()));

			List<Regio> regios = (List<Regio>) modelRepository.searchObjects(
					query, true, true);

			Regio regio = null;

			// Indien 1 regio
			if (regios.size() == 1) {
				regio = (Regio) modelRepository.getUniqueResult(regios);
			}

			// Indien intersectie met meerdere regios
			else if (regios.size() > 1) {
				Map<Regio, Double> intersections = new HashMap<Regio, Double>();

				for (Regio r : regios) {

					Geometry g1 = r.getGeom().intersection(segment.getGeom());
					intersections.put(r, g1.getLength());
				}

				double maxValueInMap = (Collections.max(intersections.values()));
				for (Entry<Regio, Double> entry : intersections.entrySet()) {
					if (entry.getValue() == maxValueInMap) {

						regio = entry.getKey();
					}
				}
			}

			return regio;

		} catch (IOException e) {
			LOG.error("Can not search Regio", e);
		}

		return null;
	}

	/**
	 * Bepalen van de Regio waarin de segmenten van een lus zich bevinden.
	 * Indien een segment meerdere regios doorkruist, wordt de regio genomen
	 * waar de lijn intersectie het grootst is. De regio waar de lengte van de
	 * intersectie het grootste is zal teruggegeven worden.
	 * 
	 * @param segment
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Regio getRegioForSegmenten(List<Geometry> segmenten) {

		try {

			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");

			Map<Regio, Double> result = new HashMap<Regio, Double>();
			List<Regio> regios = new ArrayList<Regio>();

			for (Geometry segment : segmenten) {

				query.setFilters(null);
				query.addFilter(FilterUtils.intersects("geom", segment));

				regios = (List<Regio>) modelRepository.searchObjects(query,
						false, false);

				for (Regio regio : regios) {

					Geometry g1 = regio.getGeom().intersection(segment);

					if (result.containsKey(regio)) {
						Double updatedLength = result.get(regio)
								+ g1.getLength();
						result.put(regio, updatedLength);

					} else {
						result.put(regio, g1.getLength());
					}
				}
			}

			double maxValueInMap = (Collections.max(result.values()));
			for (Entry<Regio, Double> entry : result.entrySet()) {
				if (entry.getValue() == maxValueInMap) {

					return entry.getKey();
				}
			}
		} catch (IOException e) {
			LOG.error("Can not search Regio", e);
		}

		return null;
	}

	/**
	 * Bepalen van de Regio waarin een traject zich bevindt. Indien een traject
	 * meerdere regios doorkruist, wordt de regio genomen waar de lijn
	 * intersectie het grootst is.
	 * 
	 * @param segment
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Regio getRegioForTraject(Traject traject) {

		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");
			query.addFilter(FilterUtils.intersects("geom", traject.getGeom()));

			List<Regio> regios = (List<Regio>) modelRepository.searchObjects(
					query, false, false);

			Regio regio = null;

			// Indien 1 regio
			if (regios.size() == 1) {
				regio = (Regio) modelRepository.getUniqueResult(regios);
			}

			// Indien intersectie met meerdere regios
			else if (regios.size() > 1) {
				Map<Regio, Double> intersections = new HashMap<Regio, Double>();

				for (Regio r : regios) {

					Geometry g1 = r.getGeom().intersection(traject.getGeom());
					intersections.put(r, g1.getLength());
				}

				double maxValueInMap = (Collections.max(intersections.values()));
				for (Entry<Regio, Double> entry : intersections.entrySet()) {
					if (entry.getValue() == maxValueInMap) {

						regio = entry.getKey();
					}
				}
			}

			return regio;

		} catch (IOException e) {
			LOG.error("Can not search Regio", e);
		}

		return null;
	}

	/**
	 * Ophalen van de beschikbare jaren uit configuration.properties
	 * 
	 * @return
	 */
	public List<Object[]> getJaren() {

		String years = DefaultConfiguration.instance().getString(
				"osyris.controleOpdracht.jaar");

		List<String> yearList = Arrays.asList(years.split(","));
		List<Object[]> jaren = new ArrayList<Object[]>();

		for (String year : yearList) {
			Object[] code1 = { year, year };
			jaren.add(code1);
		}
		return jaren;
	}

	/**
	 * Zoeken van het dichtsbijzijnde NetwerkSegment bij een
	 * AnderNetwerkProbleem
	 * 
	 * @param geom
	 * @param traject
	 * @return
	 * @throws Exception
	 */
	public ResourceIdentifier getNearestSegment(Geometry geom, String type)
			throws IOException {

		if (geom != null) {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName(type);

			// Query op buffer 3000m rond het AnderProbleem punt
			query.addFilter(FilterUtils.intersects(geom
					.buffer(BUFFER_BORD_METER)));

			List<Traject> segmenten = (List<Traject>) modelRepository
					.searchObjects(query, false, false);

			Map<ResourceIdentifier, Double> distances = new HashMap<ResourceIdentifier, Double>();

			for (Traject segment : segmenten) {
				double distance = DistanceOp.distance(geom, segment.getGeom());
				distances.put(modelRepository.getResourceIdentifier(segment),
						distance);
			}

			if (segmenten != null && !segmenten.isEmpty()) {

				double minValueInMap = (Collections.min(distances.values()));

				for (Entry<ResourceIdentifier, Double> entry : distances
						.entrySet()) {
					if (entry.getValue() == minValueInMap) {

						ResourceIdentifier minSegmentId = entry.getKey();
						return minSegmentId;
					}
				}
			}

			else {
				messages.error("Het door U opgegeven punt bevindt zich niet in de buurt van het netwerk.");
			}
		}
		return null;
	}

	public List<String> getSegmentenInNetwerkLus() {
		try {
			QueryBuilder builder = new QueryBuilder("NetwerkLus");
			builder.results(FilterUtils.properties("segmenten"));
			builder.groupBy(FilterUtils.properties("segmenten"));

			List<ResourceIdentifier> segmentIds = (List<ResourceIdentifier>) modelRepository
					.searchObjects(builder.build(), false, false);

			List<String> ids = new ArrayList<String>();
			for (ResourceIdentifier resourceId : segmentIds) {
				ids.add(resourceId.getIdPart());
			}

			segmentIds.clear();
			return ids;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Traject getTrajectForWerkOpdracht(WerkOpdracht werkOpdracht) {
		try {
			Traject traject = (Traject) modelRepository.loadObject(werkOpdracht
					.getTraject());
			return traject;
		} catch (IOException e) {
			LOG.error("Can not find Traject for WerkOpdracht "
					+ werkOpdracht.getId().toString());
		}
		return null;
	}

	public boolean checkIsLusGesloten(List<ResourceIdentifier> ids)
			throws IOException {

		List<NetwerkSegment> segmenten = new ArrayList<NetwerkSegment>();

		for (ResourceIdentifier id : ids) {

			NetwerkSegment segment = (NetwerkSegment) modelRepository
					.loadObject(id);
			segmenten.add(segment);
		}

		NetwerkSegment first = segmenten.get(0);
		NetwerkSegment last = segmenten.get(segmenten.size() - 1);

		NetwerkSegment current = null;
		NetwerkSegment next = null;

		// Check eerst of de eerste en laatste segmenten een knooppunt
		// gemeeschappelijk hebben
		if (first.getNaarKnooppunt().equals(last.getNaarKnooppunt())
				|| first.getNaarKnooppunt().equals(last.getVanKnooppunt())
				|| first.getVanKnooppunt().equals(last.getNaarKnooppunt())
				|| first.getVanKnooppunt().equals(last.getVanKnooppunt())) {

			for (int i = 0; i < segmenten.size(); i++) {

				if (i == 0) {
					current = first;
				} else {
					current = segmenten.get(i);
				}

				int j = i + 1;

				if (j < segmenten.size()) {
					next = segmenten.get(j);
				} else {
					next = last;
				}

				if (current.getNaarKnooppunt().equals(next.getNaarKnooppunt())
						|| current.getNaarKnooppunt().equals(
								next.getVanKnooppunt())
						|| current.getVanKnooppunt().equals(
								next.getNaarKnooppunt())
						|| current.getVanKnooppunt().equals(
								next.getVanKnooppunt())) {
				} else {
					return false;
				}

			}
			return true;

		} else {
			return false;
		}
	}

	/**
	 * Zoeken uitvoeringsronde bij een werkopdracht.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Uitvoeringsronde getUitvoeringsronde(WerkOpdracht opdracht) {

		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Uitvoeringsronde");
			query.setFilter(FilterUtils.equal("opdrachten",
					modelRepository.getResourceIdentifier(opdracht)));
			List<Uitvoeringsronde> result = (List<Uitvoeringsronde>) modelRepository
					.searchObjects(query, true, true, true);

			return (Uitvoeringsronde) modelRepository.getUniqueResult(result);

		} catch (IOException e) {
			LOG.error("Can not search Uitvoeringsronde for WerkOpdracht.", e);
		}
		return null;
	}
}
