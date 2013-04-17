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
import org.conscientia.core.functions.ModelFunctions;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class OsyrisModelFunctions {

	private static final Log log = LogFactory
			.getLog(OsyrisModelFunctions.class);

	@Inject
	private ModelRepository modelRepository;

	public List<Object[]> getStockMateriaalStates() {
		List<Object[]> stockMateriaalStates = new ArrayList<Object[]>();
		Object[] statusBesteld = { "1", "Besteld" };
		Object[] statusnietBesteld = { "0", "Niet besteld" };

		stockMateriaalStates.add(statusBesteld);
		stockMateriaalStates.add(statusnietBesteld);
		return stockMateriaalStates;
	}

	public List<Object[]> getEnkeleRichting() {
		List<Object[]> enkeleRichting = new ArrayList<Object[]>();
		Object[] statusTrue = { "1", "Ja" };
		Object[] statusFalse = { "0", "Nee" };

		enkeleRichting.add(statusTrue);
		enkeleRichting.add(statusFalse);
		return enkeleRichting;
	}

	public List<Object[]> getImageCodes() {
		List<Object[]> imageCodes = new ArrayList<Object[]>();
		Object[] code1 = { "1", "1" };
		Object[] code2 = { "2", "2" };
		Object[] code3 = { "3", "3" };

		imageCodes.add(code1);
		imageCodes.add(code2);
		imageCodes.add(code3);
		return imageCodes;
	}

	public List<Object[]> getPeriodeCodes() {
		List<Object[]> periodeCodes = new ArrayList<Object[]>();
		Object[] periode1 = { "1", "1" };
		Object[] periode2 = { "2", "2" };
		Object[] periode3 = { "3", "3" };

		periodeCodes.add(periode1);
		periodeCodes.add(periode2);
		periodeCodes.add(periode3);
		return periodeCodes;
	}

	public List<Object[]> getTrajectTypes() {
		List<Object[]> trajectTypes = new ArrayList<Object[]>();
		Collection<ModelClass> subClassesRoute = Collections.emptyList();
		subClassesRoute = modelRepository.getModelClass("Route")
				.getSubClasses();
		for (ModelClass modelClass : subClassesRoute) {
			Object[] object = { modelClass.getName(), modelClass.getName() };
			trajectTypes.add(object);
		}

		Collection<ModelClass> subClassesNetwerk = Collections.emptyList();
		subClassesNetwerk = modelRepository.getModelClass("NetwerkSegment")
				.getSubClasses();
		for (ModelClass modelClass : subClassesNetwerk) {
			Object[] object = { modelClass.getName(), modelClass.getName() };
			trajectTypes.add(object);
		}
		return trajectTypes;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<?> getCodeList(String modelClassName) {
		List<?> codeList = Collections.emptyList();

		try {
			codeList = ModelFunctions.searchEnumValues("OsyrisDataStore",
					modelClassName, "code", "label", null, null);

		} catch (IOException e) {
			log.error("Can not get list of codes for class " + modelClassName,
					e);
		}
		return codeList;
	}
}
