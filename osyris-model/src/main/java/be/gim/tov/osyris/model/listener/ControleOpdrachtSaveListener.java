package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.jboss.seam.security.Identity;

import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "ControleOpdracht") })
public class ControleOpdrachtSaveListener {

	@Inject
	private Identity identity;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		ControleOpdracht controleOpdracht = (ControleOpdracht) event
				.getModelObject();

		// Een nieuw aangemaakte ControleOpdracht krit status te controleren
		if (controleOpdracht.getStatus() == null) {
			controleOpdracht.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
		}

		// Indien alle problemen in een ControleOpdracht een status hebben is de
		// ControleOpdracht gevalideerd
		if (checkOpenstaandeProblemen(controleOpdracht) == 0
				&& !controleOpdracht.getProblemen().isEmpty()) {
			controleOpdracht.setStatus(ControleOpdrachtStatus.GEVALIDEERD);
			controleOpdracht.setDatumGevalideerd(new Date());
		}
	}

	/**
	 * Check of problemen bij een controleOpdracht een status hebben
	 * 
	 * @param controleOpdracht
	 * @return
	 */
	private int checkOpenstaandeProblemen(ControleOpdracht controleOpdracht) {
		int probleemNotChecked = 0;
		for (Probleem p : controleOpdracht.getProblemen()) {
			if (p.getStatus() == null || p.getStatus().toString().isEmpty()) {
				probleemNotChecked = +1;
			}
		}
		return probleemNotChecked;
	}
}
