package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;

import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "ControleOpdracht") })
public class ControleOpdrachtSaveListener {

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		ControleOpdracht controleOpdracht = (ControleOpdracht) event
				.getModelObject();

		if (controleOpdracht.get("status").equals(
				ControleOpdrachtStatus.UITGESTELD)) {
			controleOpdracht.setDatumUitgesteld(new Date());
		}

		if (controleOpdracht.get("status").equals(
				ControleOpdrachtStatus.GERAPPORTEERD)) {
			controleOpdracht.setDatumGerapporteerd(new Date());
		}

		if (controleOpdracht.get("status").equals(
				ControleOpdrachtStatus.GEVALIDEERD)) {
			controleOpdracht.setDatumGevalideerd(new Date());
		}
	}
}
