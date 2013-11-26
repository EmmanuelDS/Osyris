package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.Transformer;
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
import org.conscientia.core.functions.ModelFunctions;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.core.security.annotation.RunPrivileged;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Identity;

import be.gim.commons.filter.FilterUtils;
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
import be.gim.tov.osyris.model.traject.Gemeente;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.RichtingEnum;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.MedewerkerProfiel;
import be.gim.tov.osyris.model.user.UitvoerderBedrijf;
import be.gim.tov.osyris.model.user.UitvoerderProfiel;
import be.gim.tov.osyris.model.utils.AlphanumericSorting;
import be.gim.tov.osyris.model.utils.DropdownListSorting;
import be.gim.tov.osyris.model.werk.status.ValidatieStatus;

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

	/**
	 * Zoeken gebruikers in een bepaalde groep.
	 * 
	 * @param groupName
	 * @return
	 */
	@RunPrivileged
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
	 * Get suggestielijst voor een bepaalde groep.
	 * 
	 * @return
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

								Object[] object = {
										"user:" + user.getUsername(),
										profiel.getLastName() + " "
												+ profiel.getFirstName() };
								suggestions.add(object);

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

		QueryBuilder builder = new QueryBuilder("NetwerkKnooppunt");
		builder.filter(FilterUtils.notEqual("nummer", 0, true));
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

			return bord.getVolg();

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

			return bord.getStraatnaam();

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
	@RunPrivileged
	@SuppressWarnings("unchecked")
	public ResourceName zoekVerantwoordelijke(ResourceIdentifier traject) {

		try {
			Traject t = (Traject) modelRepository.loadObject(traject);

			// Group medewerkers opzoeken
			DefaultQuery query = new DefaultQuery("Group");
			query.addFilter(FilterUtils.equal("groupname", "Medewerker"));
			List<Group> groups = new ArrayList<Group>();
			groups = (List<Group>) modelRepository.searchObjects(query, false,
					false);
			Group group = (Group) ModelRepository.getUniqueResult(groups);

			// Users uit medewerker group halen
			for (ResourceName name : group.getMembers()) {
				DefaultQuery q = new DefaultQuery("User");
				q.addFilter(FilterUtils.equal("username", name.getNamePart()));
				User medewerker = (User) modelRepository.searchObjects(q,
						false, false).get(0);

				// Check welke Medewerker verantwoordelijk is voor het gekozen
				// trajectType
				MedewerkerProfiel profiel = (MedewerkerProfiel) medewerker
						.getAspect("MedewerkerProfiel", modelRepository, true);
				if (profiel != null) {
					for (String trajectType : profiel.getTrajectType()) {
						if (t.getModelClass().getName().contains("Lus")) {
							String className = t.getModelClass().getName()
									.replace("Lus", "Segment");
							if (className.equals(trajectType)) {
								return name;
							}
						} else if (trajectType.equals(t.getModelClass()
								.getName())) {
							return name;
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

			return modelRepository.searchObjects(builder.build(), true, true,
					true);
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

			return modelRepository.searchObjects(builder.build(), true, true,
					true);
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

			return modelRepository.searchObjects(builder.build(), false, false);

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

				QueryBuilder builder = new QueryBuilder("Regio");
				builder.addFilter(FilterUtils.equal("uitvoerder",
						profiel.getBedrijf()));
				builder.results(FilterUtils.properties("naam"));
				builder.groupBy(FilterUtils.properties("naam"));
				List<String> regios = (List<String>) modelRepository
						.searchObjects(builder.build(), true, true, true);

				for (String regio : regios) {
					Object[] object = { new ResourceKey("Regio", regio), regio };
					uitvoerderRegios.add(object);
				}
			}

			else {
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
	@SuppressWarnings("unchecked")
	public List<User> getUserObjectsInGroup(String groupName) {

		try {
			Group group = (Group) modelRepository.loadObject(new ResourceName(
					"group", groupName));

			List<String> nameParts = new ArrayList<String>();

			for (ResourceName name : group.getMembers()) {

				nameParts.add(name.getNamePart());
			}

			DefaultQuery query = new DefaultQuery("User");

			query.addFilter(FilterUtils.in("username", nameParts));

			List<User> result = (List<User>) modelRepository.searchObjects(
					query, true, true);

			return result;

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

		return (List<Bord>) cacheProducer.getCache("NetwerkBordVolgordeLus",
				new Transformer() {

					@Override
					public Object transform(Object lus) {

						try {

							if (lus != null) {
								List<Bord> result = new ArrayList<Bord>();

								// StartSegment in de lus
								NetwerkSegment start = (NetwerkSegment) modelRepository
										.loadObject(((NetwerkLus) lus)
												.getSegmenten().get(0));

								// ---------------
								for (int i = 0; i < ((NetwerkLus) lus)
										.getSegmenten().size(); i++) {

									int nextIndex = i + 1;

									NetwerkSegment current = (NetwerkSegment) modelRepository
											.loadObject(((NetwerkLus) lus)
													.getSegmenten().get(i));

									NetwerkSegment next = null;

									if (nextIndex < ((NetwerkLus) lus)
											.getSegmenten().size()) {

										next = (NetwerkSegment) modelRepository
												.loadObject(((NetwerkLus) lus)
														.getSegmenten().get(
																nextIndex));
									} else {
										next = start;
									}

									String richting = null;
									if (current.getNaarKpNr().equals(
											next.getVanKpNr())) {

										richting = RichtingEnum.FT.toString();

									}

									else if (current.getNaarKpNr().equals(
											next.getNaarKpNr())) {

										richting = RichtingEnum.FT.toString();
									}

									else {
										richting = RichtingEnum.TF.toString();
									}

									QueryBuilder builder = new QueryBuilder(
											"NetwerkBord");
									List<Bord> subset = new ArrayList<Bord>();

									List<ResourceIdentifier> ids = new ArrayList<ResourceIdentifier>(
											1);
									ids.add(modelRepository
											.getResourceIdentifier(current));

									builder.addFilter(FilterUtils.in(
											"segmenten", ids));

									builder.addFilter(FilterUtils.equal(
											"richting", richting));

									subset = (List<Bord>) modelRepository
											.searchObjects(builder.build(),
													false, false);

									if (subset != null && !subset.isEmpty()) {
										Collections.sort(subset,
												new AlphanumericSorting());
										result.addAll(subset);
									}
								}

								return result;
							}
							// ----------------

						} catch (IOException e) {
							LOG.error("Can not search segmenten.", e);
						}

						return Collections.emptyList();
					}
				}).get(lus);
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
			variables.put("problem", melding.getProbleem());

			// Mail naar melder
			mailSender.sendMail(preferences.getNoreplyEmail(),
					Collections.singleton(melding.getEmail()),
					"/META-INF/resources/core/mails/confirmMelding.fmt",
					variables);

			messages.info("Er is een bevestigingsmail gestuurd naar "
					+ melding.getEmail() + ".");

			// Ophalen emailadres Medewerker
			UserProfile profiel = (UserProfile) modelRepository.loadAspect(
					modelRepository.getModelClass("UserProfile"),
					modelRepository.loadObject(melding.getMedewerker()));
			String medewerkerEmail = profiel.getEmail();

			// DEBUG ONLY
			String testEmail = "kristof.spiessens@gim.be";

			// Mail naar Medewerker TOV
			mailSender.sendMail(preferences.getNoreplyEmail(),
					Collections.singleton(testEmail),
					"/META-INF/resources/core/mails/confirmMelding.fmt",
					variables);

			messages.info("Er is een bevestigingsmail gestuurd de verantwoordelijke TOV "
					+ medewerkerEmail + ".");

		} catch (IOException e) {

			LOG.error("Can not load object.", e);
		} catch (Exception e) {

			LOG.error("Can not send email.", e);
			messages.error("Fout bij het versturen van bevestigingsmail");
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
	 * Bepalen van de Regio waarin een bord zich bevindt.
	 * 
	 * @param geometry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Regio getRegioForBord(Bord bord) {

		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");
			query.addFilter(FilterUtils.intersects("geom", bord.getGeom()));

			List<Regio> regios = (List<Regio>) modelRepository.searchObjects(
					query, true, true);
			Regio regio = (Regio) modelRepository.getUniqueResult(regios);

			return regio;

		} catch (IOException e) {
			LOG.error("Can not search Regio", e);
		}

		return null;
	}
}
