package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.jboss.seam.security.Identity;

import be.gim.tov.osyris.model.controle.ControleOpdracht;
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

		if (controleOpdracht.getStatus() == null) {
			controleOpdracht.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
		}

		if (controleOpdracht.getStatus().equals(
				ControleOpdrachtStatus.UITGESTELD)) {
			controleOpdracht.setDatumUitgesteld(new Date());
		}

		if (identity.inGroup("PeterMeter", "CUSTOM")
				&& controleOpdracht.getStatus().equals(
						ControleOpdrachtStatus.UIT_TE_VOEREN)) {
			controleOpdracht.setStatus(ControleOpdrachtStatus.GERAPPORTEERD);
			controleOpdracht.setDatumGerapporteerd(new Date());
		}

		if (controleOpdracht.getStatus().equals(
				ControleOpdrachtStatus.GEVALIDEERD)) {
			controleOpdracht.setDatumGevalideerd(new Date());
		}
	}
}
