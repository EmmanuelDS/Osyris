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
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.status.MeldingStatus;
import be.gim.tov.osyris.model.controle.status.ProbleemStatus;
import be.gim.tov.osyris.model.traject.Bord;
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

		// Nieuwe melding
		if (melding.getStatus() == null) {
			melding.setStatus(MeldingStatus.GEMELD);
			melding.setDatumGemeld(new Date());
			melding.setMedewerker(osyrisModelFunctions
					.zoekVerantwoordelijke(melding.getTraject()));
			melding.setTrajectType(osyrisModelFunctions.getTrajectType(melding
					.getTraject()));
			melding.setRegioId(osyrisModelFunctions.getTrajectRegioId(melding
					.getTraject()));
			melding.setDatumLaatsteWijziging(new Date());
		}

		// Indien probleem met status Melding gevalideerd

		if (melding.getProbleem() != null) {
			if (melding.getProbleem().getStatus() != null) {

				// Indien Probleem reeds gemeld
				if (melding.getProbleem().getStatus()
						.equals(ProbleemStatus.REEDS_GEMELD)) {
					melding.setStatus(MeldingStatus.REEDS_GEMELD);
					melding.setDatumGevalideerd(new Date());
					melding.setDatumLaatsteWijziging(new Date());
					melding.setTrajectType(osyrisModelFunctions
							.getTrajectType(melding.getTraject()));
				}

				// Indien Probleem in behandeling
				if (melding.getProbleem().getStatus()
						.equals(ProbleemStatus.IN_BEHANDELING)) {
					melding.setStatus(MeldingStatus.IN_BEHANDELING);
					melding.setDatumGevalideerd(new Date());
					melding.setDatumLaatsteWijziging(new Date());
					melding.setTrajectType(osyrisModelFunctions
							.getTrajectType(melding.getTraject()));
				}

				// Indien Probleem geen WerkOpdracht moet worden
				if (melding.getProbleem().getStatus()
						.equals(ProbleemStatus.GEEN_WERKOPDRACHT)) {
					melding.setStatus(MeldingStatus.GEEN_WERKOPDRACHT);
					melding.setDatumGevalideerd(new Date());
					melding.setDatumLaatsteWijziging(new Date());
					melding.setTrajectType(osyrisModelFunctions
							.getTrajectType(melding.getTraject()));
				}

				// Aanmaken werkopdracht indien status probleem werkopdracht
				if (melding.getProbleem().getStatus()
						.equals(ProbleemStatus.WERKOPDRACHT)) {
					melding.setStatus(MeldingStatus.GEVALIDEERD);
					melding.setDatumGevalideerd(new Date());
					melding.setDatumLaatsteWijziging(new Date());
					melding.setTrajectType(osyrisModelFunctions
							.getTrajectType(melding.getTraject()));
					createWerkOpdracht(melding);
				}
			}
		}
	}

	/**
	 * Aanmaken nieuwe WerkOpdracht
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
			werkOpdracht.setCommentaarMedewerker(melding.getCommentaar());
			werkOpdracht.setMedewerker(melding.getMedewerker());
			werkOpdracht.setProbleem(melding.getProbleem());
			werkOpdracht.setTraject(melding.getTraject());
			werkOpdracht.setTrajectType(osyrisModelFunctions
					.getTrajectType(melding.getTraject()));
			werkOpdracht.setRegioId(osyrisModelFunctions
					.getTrajectRegioId(melding.getTraject()));
			werkOpdracht.setDatumLaatsteWijziging(new Date());
			werkOpdracht.setGemeente(osyrisModelFunctions
					.getWerkOpdrachtGemeente(melding.getProbleem()));

			Traject traject = (Traject) modelRepository.loadObject(melding
					.getTraject());

			// Voor BordProbleem uitvoerder zoeken via RegioID van het
			// Bord
			if (melding.getProbleem() instanceof BordProbleem) {
				Bord b = (Bord) modelRepository
						.loadObject(((BordProbleem) melding.getProbleem())
								.getBord());
				werkOpdracht.setUitvoerder(osyrisModelFunctions
						.zoekUitvoerder(b.getRegio()));
			}

			// Voor AnderProbleem uitvoerder zoeken via intersect geometrie
			// met regio
			if (melding.getProbleem() instanceof AnderProbleem) {
				werkOpdracht
						.setUitvoerder(osyrisModelFunctions.zoekUitvoerder(osyrisModelFunctions
								.searchRegioForProbleem(((AnderProbleem) melding
										.getProbleem()).getGeom())));

			}

			modelRepository.saveObject(werkOpdracht);
			messages.info("Nieuwe werkopdracht succesvol aangemaakt.");

		} catch (IOException e) {
			messages.error("Fout bij het bewaren van een nieuwe werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not save WerkOpdracht", e);
		} catch (InstantiationException e) {
			messages.error("Fout bij het aanmaken van een nieuwe werkopdracht: "
					+ e.getMessage());
			LOG.error("Can not create WerkOpdracht.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Can not access WerkOpdracht.", e);
		}
	}
}
