package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.functions.ModelFunctions;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class OsyrisModelFunctions {
	private static final Log LOG = LogFactory
			.getLog(OsyrisModelFunctions.class);

	private static final String GEEN_PETER_METER = "Geen PeterMeter toegewezen";

	@Inject
	private ModelRepository modelRepository;
	@Inject
	private UserRepository userRepository;

	/**
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
	 * 
	 * @return
	 */
	public List<Object[]> getEnkeleRichting() {
		List<Object[]> enkeleRichting = new ArrayList<Object[]>();
		Object[] statusTrue = { "1", "Ja" };
		Object[] statusFalse = { "0", "Nee" };

		enkeleRichting.add(statusTrue);
		enkeleRichting.add(statusFalse);
		return enkeleRichting;
	}

	/**
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
	 * Retrieve all users in a certain group.
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersInGroup(String groupName) {

		List<User> users = new ArrayList<User>();
		List<User> medewerkers = new ArrayList<User>();
		try {
			users = (List<User>) modelRepository.searchObjects(
					new DefaultQuery("User"), true, true);
			for (User user : users) {
				if (userRepository.listGroupnames(user).contains(groupName)) {
					medewerkers.add(user);
				}
			}
		} catch (IOException e) {
			LOG.error("Can not search objects.", e);
		}
		return medewerkers;
	}

	/**
	 * Get the suggestions for users in a certain group.
	 * 
	 * @param groupName
	 * @return
	 */
	public List<? extends ResourceIdentifier> getSuggestions(String groupName) {

		List<User> users = getUsersInGroup(groupName);

		List<ResourceIdentifier> suggestions = new ArrayList<ResourceIdentifier>();
		for (User u : users) {
			suggestions.add(modelRepository.getResourceIdentifier(u));
		}
		if (groupName.equals("PeterMeter")) {
			suggestions.add(new ResourceName(GEEN_PETER_METER));
		}

		return suggestions;
	}

	/**
	 * Gets regios OVL
	 * 
	 * @return
	 */
	public List<Object[]> getRegiosOostVlaanderen() {

		List<Object[]> regios = new ArrayList<Object[]>();
		Object[] code1 = { "Regio@1", "Leiestreek" };
		Object[] code2 = { "Regio@2", "Meetjesland" };
		Object[] code3 = { "Regio@3", "Vlaamse Ardennen" };
		Object[] code4 = { "Regio@4", "Waasland" };
		Object[] code5 = { "Regio@5", "Scheldeland" };
		Object[] code6 = { "Regio@9", "Zeeuws-Vlaanderen" };

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
}
