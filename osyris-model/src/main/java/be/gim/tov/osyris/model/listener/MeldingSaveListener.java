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

		if (melding.get("status").equals(null)) {
			melding.setStatus(MeldingStatus.GEMELD);
			melding.set("datumGemeld", new Date());
			String type = melding.getProbleem().getModelClass().getName();
			melding.getProbleem().setType(type);

			// TODO: Automatisch toewijzen aan Medewerker TOV via
			// MedewerkerProfiel trajecttype property
		}

		if (melding.get("status")
				.equals(MeldingStatus.GEVALIDEERD_WERKOPDRACHT)) {
			melding.setDatumGevalideerd(new Date());
			// TODO: nieuwe WerkOpdracht maken met te herstellen bording,
			// uitvoerder,
			// toezichthoudend medewerker, status te controleren, datum creatie
		}
	}
}
