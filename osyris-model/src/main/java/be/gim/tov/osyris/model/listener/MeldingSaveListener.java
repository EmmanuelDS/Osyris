package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.Melding;
import be.gim.tov.osyris.model.controle.status.MeldingStatus;
import be.gim.tov.osyris.model.controle.status.ProbleemStatus;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.user.MedewerkerProfiel;
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
			melding.setMedewerker(zoekVerantwoordelijke(melding.getTraject()));
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
	 * 
	 * @param traject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ResourceName zoekVerantwoordelijke(ResourceIdentifier traject) {
		try {
			Traject t = (Traject) modelRepository.loadObject(traject);
			List<User> users = new ArrayList<User>();
			List<User> medewerkers = new ArrayList<User>();
			users = (List<User>) modelRepository.searchObjects(
					new DefaultQuery("User"), true, true);
			for (User user : users) {
				if (userRepository.listGroupnames(user).contains("Medewerker")) {
					medewerkers.add(user);
				}
			}

			for (User u : medewerkers) {

				MedewerkerProfiel profiel = (MedewerkerProfiel) u.getAspect(
						"MedewerkerProfiel", modelRepository, true);
				if (profiel != null) {
					for (String trajectType : profiel.getTrajectType()) {
						if (trajectType.equals(t.getModelClass().getName())) {
							return modelRepository.getResourceName(u);
						}
					}
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		} catch (InstantiationException e) {
			LOG.error("Can not instantiate object.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Can not access object.", e);
		}
		return null;
	}

	/**
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
			werkOpdracht.setDatumTeControleren(new Date());
			werkOpdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
			werkOpdracht.setMedewerker(melding.getMedewerker());
			werkOpdracht.setProbleem(melding.getProbleem());
			werkOpdracht.setUitvoerder(osyrisModelFunctions
					.zoekUitvoerder(melding.getTraject()));
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

	// /**
	// *
	// * @param traject
	// * @return
	// */
	// private ResourceName zoekUitvoerder(ResourceIdentifier traject) {
	// try {
	// Traject t = (Traject) modelRepository.loadObject(traject);
	// Regio regio = (Regio) modelRepository.loadObject(t.getRegio());
	// return (ResourceName) regio.getUitvoerder();
	// } catch (IOException e) {
	// LOG.error("Can not load Traject.", e);
	// }
	// return null;
	// }
}
