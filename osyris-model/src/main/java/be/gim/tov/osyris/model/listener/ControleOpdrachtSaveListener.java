package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.DefaultQuery;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.Identity;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceName;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.NetwerkAnderProbleem;
import be.gim.tov.osyris.model.controle.NetwerkBordProbleem;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;
import be.gim.tov.osyris.model.controle.status.ProbleemStatus;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

import com.vividsolutions.jts.geom.Geometry;

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
	private Identity identity;
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

		// Een nieuw aangemaakte ControleOpdracht krit status te controleren
		if (controleOpdracht.getStatus() == null) {
			controleOpdracht.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
		}

		// Indien alle problemen in een ControleOpdracht een status hebben is de
		// ControleOpdracht gevalideerd
		if (checkOpenstaandeProblemen(controleOpdracht) == 0
				&& !controleOpdracht.getProblemen().isEmpty()) {
			controleOpdracht.setStatus(ControleOpdrachtStatus.GEVALIDEERD);
			controleOpdracht.setDatumGevalideerd(new Date());
			// WerkOpdrachten aanmaken
			createWerkOpdrachten(controleOpdracht);
		}
	}

	/**
	 * Check of problemen bij een controleOpdracht een status hebben
	 * 
	 * @param controleOpdracht
	 * @return
	 */
	private int checkOpenstaandeProblemen(ControleOpdracht controleOpdracht) {
		int probleemNotChecked = 0;
		for (Probleem p : controleOpdracht.getProblemen()) {
			if (p.getStatus() == null || p.getStatus().toString().isEmpty()) {
				probleemNotChecked = +1;
			}
		}
		return probleemNotChecked;
	}

	/**
	 * Aanmaken van werkopdrachten bij bepaalde problemen in een
	 * controleOpdracht
	 * 
	 * @param controleOpdracht
	 */
	private void createWerkOpdrachten(ControleOpdracht controleOpdracht) {
		for (Probleem probleem : controleOpdracht.getProblemen()) {
			if (probleem.getStatus().equals(ProbleemStatus.WERKOPDRACHT)) {
				try {
					String modelClassName = "WerkOpdracht";
					WerkOpdracht werkOpdracht = (WerkOpdracht) modelRepository
							.createObject(modelRepository
									.getModelClass(modelClassName),
									(ResourceName) ResourceName
											.fromString(modelClassName));
					werkOpdracht.setDatumTeControleren(new Date());
					werkOpdracht.setInRonde("0");
					werkOpdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
					werkOpdracht
							.setMedewerker(controleOpdracht.getMedewerker());
					werkOpdracht.setProbleem(probleem);
					werkOpdracht.setTraject(controleOpdracht.getTraject());

					// Voor routes uitvoerder zoeken via RegioID van de route
					Traject traject = (Traject) modelRepository
							.loadObject(controleOpdracht.getTraject());
					if (traject instanceof Route) {
						werkOpdracht.setUitvoerder(osyrisModelFunctions
								.zoekUitvoerder(traject.getRegio()));
					}

					// Voor netwerk uitvoerder zoeken via RegioID van het
					// NetwerkBord indien bordprobleem
					if (traject instanceof NetwerkLus
							&& probleem instanceof NetwerkBordProbleem) {
						Bord b = (Bord) modelRepository
								.loadObject(((NetwerkBordProbleem) probleem)
										.getBord());
						werkOpdracht.setUitvoerder(osyrisModelFunctions
								.zoekUitvoerder(b.getRegio()));
					}

					// Voor netwerk uitvoerder zoeken via geom anderprobleem en
					// zoeken naar intersect met een bepaalde regio?
					if (traject instanceof NetwerkLus
							&& probleem instanceof NetwerkAnderProbleem) {
						werkOpdracht
								.setUitvoerder(osyrisModelFunctions
										.zoekUitvoerder(searchRegioForProbleem(((NetwerkAnderProbleem) probleem)
												.getGeom())));

					}
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
	}

	/**
	 * Zoekt de regio waarmee het aangeduide probleem een intersectie heeft.
	 * 
	 * @param geometry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ResourceIdentifier searchRegioForProbleem(Geometry geometry) {
		try {
			DefaultQuery query = new DefaultQuery();
			query.setModelClassName("Regio");
			query.addFilter(FilterUtils.intersects("geom", geometry));
			List<Regio> regios = (List<Regio>) modelRepository.searchObjects(
					query, true, true);
			Regio regio = (Regio) modelRepository.getUniqueResult(regios);
			return modelRepository.getResourceIdentifier(regio);

		} catch (IOException e) {
			LOG.error("Can not search regios.", e);
		}
		return null;
	}
}
