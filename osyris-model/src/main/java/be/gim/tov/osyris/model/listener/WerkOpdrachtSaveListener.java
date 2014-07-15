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

import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "WerkOpdracht") })
public class WerkOpdrachtSaveListener {

	private static final Log LOG = LogFactory
			.getLog(WerkOpdrachtSaveListener.class);

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
		WerkOpdracht werkOpdracht = (WerkOpdracht) event.getModelObject();

		// Nieuwe WerkOpdracht
		if (werkOpdracht.getStatus() == null) {

			werkOpdracht.setInRonde("0");
			werkOpdracht.setDatumTeControleren(new Date());
			werkOpdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);

			if (werkOpdracht.getMedewerker() == null) {
				werkOpdracht.setMedewerker(osyrisModelFunctions
						.zoekVerantwoordelijke(werkOpdracht.getTraject()));
			}

			werkOpdracht.setTrajectType(osyrisModelFunctions
					.getTrajectType(werkOpdracht.getTraject()));
			werkOpdracht.setRegioId(osyrisModelFunctions
					.getTrajectRegioId(werkOpdracht.getTraject()));
			werkOpdracht.setDatumLaatsteWijziging(new Date());
			werkOpdracht.setGemeente(osyrisModelFunctions
					.getWerkOpdrachtGemeente(werkOpdracht.getProbleem()));

			// Voor BordProbleem uitvoerder zoeken via RegioID van het
			// Bord
			if (werkOpdracht.getUitvoerder() == null) {
				if (werkOpdracht.getProbleem() instanceof BordProbleem) {
					Bord b = (Bord) modelRepository
							.loadObject(((BordProbleem) werkOpdracht
									.getProbleem()).getBord());
					werkOpdracht.setUitvoerder(osyrisModelFunctions
							.zoekUitvoerder(b.getRegio()));
				}

				// Voor AnderProbleem uitvoerder zoeken via intersect geometrie
				// met regio
				if (werkOpdracht.getProbleem() instanceof AnderProbleem) {
					werkOpdracht
							.setUitvoerder(osyrisModelFunctions.zoekUitvoerder(osyrisModelFunctions
									.searchRegioForProbleem(((AnderProbleem) werkOpdracht
											.getProbleem()).getGeom())));

				}
			}
		}
	}
}
