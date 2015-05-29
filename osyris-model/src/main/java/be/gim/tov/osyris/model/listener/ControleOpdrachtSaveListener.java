package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.jboss.seam.international.status.Messages;

import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "ControleOpdracht") })
public class ControleOpdrachtSaveListener {

	private static final Log LOG = LogFactory
			.getLog(ControleOpdrachtSaveListener.class);

	@Inject
	private ModelRepository modelRepository;
	@Inject
	private OsyrisModelFunctions osyrisModelFunctions;
	@Inject
	protected Messages messages;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		ControleOpdracht controleOpdracht = (ControleOpdracht) event
				.getModelObject();

		// Altijd medewerker herberekenen
		// if (null == controleOpdracht.getMedewerker()) {
		controleOpdracht.setMedewerker(osyrisModelFunctions
				.zoekVerantwoordelijke(controleOpdracht.getTraject()));
		// }

		if (controleOpdracht.getJaar() == null) {
			Calendar c = Calendar.getInstance();
			Integer jaar = c.get(Calendar.YEAR);
			c.getTime();
			controleOpdracht.setJaar(jaar.toString());
		}

		// Een nieuw aangemaakte ControleOpdracht krijgt status te controleren
		if (controleOpdracht.getStatus() == null) {

			controleOpdracht.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
			controleOpdracht.setTrajectType(osyrisModelFunctions
					.getTrajectType(controleOpdracht.getTraject()));
			controleOpdracht.setRegioId(osyrisModelFunctions
					.getTrajectRegioId(controleOpdracht.getTraject()));
			controleOpdracht.setDatumLaatsteWijziging(new Date());
		}
	}
}
