package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;

import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.status.MeldingStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Melding") })
public class MeldingSaveListener {

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		Melding melding = (Melding) event.getModelObject();

		if (melding.getStatus() == null) {
			melding.setStatus(MeldingStatus.GEMELD);
			melding.set("datumGemeld", new Date());
			// TODO: Automatisch toewijzen aan Medewerker TOV
		}

		else if (melding.getStatus().equals(MeldingStatus.GEMELD)) {
			melding.setStatus(MeldingStatus.GEVALIDEERD);
			melding.setDatumGevalideerd(new Date());
		}
	}
}
