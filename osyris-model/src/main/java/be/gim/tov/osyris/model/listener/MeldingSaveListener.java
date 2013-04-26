package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;

import be.gim.commons.resource.ResourceKey;
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

		if (melding.get("status") == null) {
			melding.setStatus(MeldingStatus.GEMELD);
			String type = melding.getProbleem().getModelClass().getName();
			melding.getProbleem().setType(type);
			// TODO: Automatisch toewijzen aan Medewerker TOV via
			// MedewerkerProfiel trajecttype property
			melding.setMedewerker(new ResourceKey("User", "4"));

		}

		if (melding.get("status").equals(MeldingStatus.GEMELD)) {
			melding.set("datumGemeld", new Date());
		}

		else {

			melding.setDatumGevalideerd(new Date());

			if (melding.get("status").equals(
					MeldingStatus.GEVALIDEERD_WERKOPDRACHT)) {
				// TODO: nieuwe WerkOpdracht maken met te herstellen bording,
				// uitvoerder,
				// toezichthoudend medewerker, status te controleren, datum
				// creatie
			}
		}
	}
}
