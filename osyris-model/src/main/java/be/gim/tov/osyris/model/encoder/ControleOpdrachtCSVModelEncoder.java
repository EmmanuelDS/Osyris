package be.gim.tov.osyris.model.encoder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.model.ModelProperty;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.core.encoder.CSVModelEncoder;
import org.conscientia.core.model.property.EnumModelProperty;
import org.conscientia.core.model.property.ObjectModelProperty;
import org.conscientia.core.model.property.ResourceIdentifierModelProperty;
import org.conscientia.core.util.ModelUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.api.EncoderException;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.user.OsyrisUserProfile;

public class ControleOpdrachtCSVModelEncoder extends CSVModelEncoder {

	private static final Log LOG = LogFactory
			.getLog(ControleOpdrachtCSVModelEncoder.class);

	@Override
	public String getContentType() {
		return "text/csv; charset=UTF-8";
	}

	@Override
	public String getExtension() {
		return "csv";
	}

	@Override
	public void encode(Writer writer,
			ModelObjectList<? extends ModelObject> list)
			throws EncoderException, IOException {

		CsvListWriter csvWriter = getCsvWriter(writer);

		ModelClass modelClass = list.getItemModelClass();

		csvWriter.write(getLabels(modelClass));

		for (ModelObject object : list) {
			csvWriter.write(getStrings(modelClass, object));
		}

		csvWriter.close();
	}

	/**
	 * Use a ; as delimiter in CSV output file. Belgian Regional settings define
	 * a ; as the default delimiter. This value is used by MS Excel.
	 */
	@Override
	protected CsvListWriter getCsvWriter(Writer writer) {
		return new CsvListWriter(writer, new CsvPreference('\"', ';', "\n"));
	}

	@Override
	protected boolean isExportable(ModelProperty property) {
		return property.isViewable();
	}

	@Override
	protected List<String> getLabels(ModelClass modelClass) {

		List<String> labels = new ArrayList<String>();

		for (ModelProperty property : modelClass.getProperties()) {
			if (isExportable(property)) {
				if (property instanceof ObjectModelProperty
						&& !property.isMultiple()) {
					labels.addAll(getLabels(modelClass.getModelClassLoader()
							.getModelClass(
									((ObjectModelProperty) property)
											.getObjectClassName())));
				} else {
					labels.add(property.getLabel().toString());
				}
			}
		}

		// Add extra OsyrisUserProfile property labels for ControleOpdracht CSV
		// export
		ModelClass userProfile = Beans.getReference(OsyrisUserProfile.class)
				.getModelClass();

		ModelProperty propFirstName = userProfile.getProperty("firstName");
		labels.add(propFirstName.getLabel().toString());

		ModelProperty propLastName = userProfile.getProperty("lastName");
		labels.add(propLastName.getLabel().toString());

		ModelProperty propEmail = userProfile.getProperty("email");
		labels.add(propEmail.getLabel().toString());

		return labels;
	}

	@Override
	protected List<String> getStrings(ModelClass modelClass, ModelObject object) {

		List<String> values = new ArrayList<String>();

		for (ModelProperty property : modelClass.getProperties()) {
			if (isExportable(property)) {
				Object value = object.get(property.getName());

				if (property.isMultiple()) {
					values.add(StringUtils.join(
							getStrings(property, object, (List<?>) value), ','));
				} else {
					values.add(getString(property, object, value));
				}
			}
		}

		try {

			ControleOpdracht opdracht = (ControleOpdracht) object;

			// Leave fields blank if no PM is assigned
			if (opdracht.getPeterMeter() != null) {
				User user = (User) Beans.getReference(ModelRepository.class)
						.loadObject(opdracht.getPeterMeter());

				// Add UserProfile info to ControleOpdracht CSV export
				OsyrisUserProfile profile = (OsyrisUserProfile) user.getAspect(
						"UserProfile",
						Beans.getReference(ModelRepository.class), true);

				if (profile != null) {

					String firstName = profile.getFirstName();
					ModelProperty propFirstName = profile.getModelClass()
							.getProperty("firstName");
					values.add(getString(propFirstName, profile, firstName));

					String lastName = profile.getLastName();
					ModelProperty propLastName = profile.getModelClass()
							.getProperty("lastName");
					values.add(getString(propLastName, profile, lastName));

					String email = profile.getEmail();
					ModelProperty propEmail = profile.getModelClass()
							.getProperty("email");
					values.add(getString(propEmail, profile, email));
				}
			}
		} catch (IOException e) {
			LOG.error("Can not open aspect of ControleOpdracht", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate aspect of ControleOpdracht", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at aspect of ControleOpdracht", e);
		}

		return values;
	}

	@Override
	protected List<String> getStrings(ModelProperty property,
			ModelObject object, List<?> values) {

		List<String> list = new ArrayList<String>(values.size());

		for (Object item : values) {
			list.add(getString(property, object, item));
		}

		return list;
	}

	@Override
	protected String getString(ModelProperty property, ModelObject object,
			Object value) {

		if (value == null) {
			return "";
		}

		if (property instanceof ObjectModelProperty) {
			return '{' + StringUtils.join(
					getStrings(
							((ObjectModelProperty) property).getObjectClass(),
							(ModelObject) value), ',') + '}';
		} else if (property instanceof EnumModelProperty) {
			List<String> parents = ((EnumModelProperty) property).getParents();
			return ((EnumModelProperty) property).getValueLabel(object,
					parents.size() != 0 ? object.get(parents) : null, value);
		} else if (property instanceof ResourceIdentifierModelProperty) {
			return ModelUtils.toLabel(value);
		} else {
			return property.getAsString(value);
		}
	}
}
