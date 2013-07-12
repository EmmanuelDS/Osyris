package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.UserRepository;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.status.MeldingStatus;
import be.gim.tov.osyris.model.controle.status.ProbleemStatus;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Melding") })
public class MeldingSaveListener {

	private static final Log LOG = LogFactory.getLog(MeldingSaveListener.class);

	@Inject
	private ModelRepository modelRepository;
	@Inject
	private UserRepository userRepository;
	@Inject
	private OsyrisModelFunctions osyrisModelFunctions;
	@Inject
	protected Messages messages;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		Melding melding = (Melding) event.getModelObject();

		// New melding
		if (melding.getStatus() == null) {
			melding.setStatus(MeldingStatus.GEMELD);
			melding.setDatumGemeld(new Date());
			melding.setMedewerker(osyrisModelFunctions
					.zoekVerantwoordelijke(melding.getTraject()));
		}

		// If probleem has a status, Melding is validated
		if (melding.getProbleem().getStatus() != null) {
			melding.setStatus(MeldingStatus.GEVALIDEERD);
			melding.setDatumGevalideerd(new Date());

			// Create new werkopdracht if status probleem is werkopdracht
			if (melding.getProbleem().getStatus()
					.equals(ProbleemStatus.WERKOPDRACHT)) {
				createWerkOpdracht(melding);
			}
		}
	}

	/**
	 * Aanmaken nieuwe Melding.
	 * 
	 * @param melding
	 */
	private void createWerkOpdracht(Melding melding) {
		try {
			String modelClassName = "WerkOpdracht";
			WerkOpdracht werkOpdracht = (WerkOpdracht) modelRepository
					.createObject(
							modelRepository.getModelClass(modelClassName),
							(ResourceName) ResourceName
									.fromString(modelClassName));
			werkOpdracht.setInRonde("0");
			werkOpdracht.setDatumTeControleren(new Date());
			werkOpdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
			werkOpdracht.setMedewerker(melding.getMedewerker());
			werkOpdracht.setProbleem(melding.getProbleem());
			werkOpdracht.setTraject(melding.getTraject());

			Traject traject = (Traject) modelRepository.loadObject(melding
					.getTraject());

			werkOpdracht.setUitvoerder(osyrisModelFunctions
					.zoekUitvoerder(traject.getRegio()));

			modelRepository.saveObject(werkOpdracht);
			messages.info("Nieuwe werkopdracht aangemaakt.");
		} catch (IOException e) {
			messages.error("Fout bij het bewaren van een nieuwe werkopdracht.");
			LOG.error("Can not save WerkOpdracht", e);
		} catch (InstantiationException e) {
			messages.error("Fout bij het aanmaken van een nieuwe werkopdracht.");
			LOG.error("Can not create WerkOpdracht.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Can not access WerkOpdracht.", e);
		}
	}
}
