package be.gim.tov.osyris.form;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;

import be.gim.tov.osyris.model.werk.Uitvoeringsronde;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class UitvoeringsrondeOverzichtFormBase extends
		AbstractListForm<Uitvoeringsronde> {
	private static final long serialVersionUID = 3771393152252852618L;

	// VARIABLES
	@Inject
	protected UserRepository userRepository;

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "Uitvoeringsronde";
	}
}
