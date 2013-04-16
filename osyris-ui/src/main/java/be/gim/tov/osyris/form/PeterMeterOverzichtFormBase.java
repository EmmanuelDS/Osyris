package be.gim.tov.osyris.form;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.UserRepository;

public class PeterMeterOverzichtFormBase {

	private static final Log log = LogFactory
			.getLog(PeterMeterOverzichtFormBase.class);

	// VARIABLES
	@Inject
	private ModelRepository modelRepository;

	@Inject
	private UserRepository userRepository;

	@Inject
	private Preferences preferences;

}
