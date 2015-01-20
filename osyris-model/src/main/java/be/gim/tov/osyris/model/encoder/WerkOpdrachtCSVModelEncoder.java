package be.gim.tov.osyris.model.encoder;

import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelProperty;
import org.conscientia.core.encoder.CSVModelEncoder;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class WerkOpdrachtCSVModelEncoder extends CSVModelEncoder {

	private static final Log LOG = LogFactory
			.getLog(WerkOpdrachtCSVModelEncoder.class);

	@Override
	public String getContentType() {
		return "text/csv; charset=UTF-8";
	}

	@Override
	public String getExtension() {
		return "csv";
	}

	/**
	 * Use a ; as delimiter in CSV output file. Belgian Regional settings define
	 * a ; as the default delimiter. This value is used by MS Excel.
	 */
	@Override
	protected CsvListWriter getCsvWriter(Writer writer) {
		return new CsvListWriter(writer, new CsvPreference('\"', ';', "\n"));
	}

	// Only make the property exportable if the view level is SHORT.
	// Byte array properties like images of a WerkOpdracht do not need to be
	// exportable.
	// Their view level must set to LONG
	@Override
	protected boolean isExportable(ModelProperty property) {

		if (property.getViewLevel().equals("short")) {
			return property.isViewable();
		}
		return false;
	}
}
