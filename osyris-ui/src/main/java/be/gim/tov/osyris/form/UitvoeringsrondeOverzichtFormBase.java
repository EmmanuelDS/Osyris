package be.gim.tov.osyris.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.jsf.component.ComponentUtils;
import org.jboss.seam.international.status.Messages;

import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.Uitvoeringsronde;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.UitvoeringsrondeStatus;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class UitvoeringsrondeOverzichtFormBase extends
		AbstractListForm<Uitvoeringsronde> {

	private static final long serialVersionUID = 3771393152252852618L;

	private static final Log LOG = LogFactory
			.getLog(UitvoeringsrondeOverzichtFormBase.class);

	private static final String GEOMETRY_LAYER_NAME = "geometry";

	// VARIABLES
	@Inject
	protected UserRepository userRepository;
	@Inject
	protected MapFactory mapFactory;
	@Inject
	protected Preferences preferences;
	@Inject
	protected MailSender mailSender;
	@Inject
	protected Messages messages;

	protected ResourceIdentifier regio;
	protected String trajectType;
	protected String trajectNaam;
	protected ResourceIdentifier werkOpdracht;
	protected ResourceIdentifier uitvoerder;
	protected ResourceIdentifier medewerker;
	protected WerkOpdracht selectedWerkOpdracht;

	// GETTERS AND SETTERS
	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public String getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(String trajectType) {
		this.trajectType = trajectType;
	}

	public String getTrajectNaam() {
		return trajectNaam;
	}

	public void setTrajectNaam(String trajectNaam) {
		this.trajectNaam = trajectNaam;
	}

	public ResourceIdentifier getWerkOpdracht() {
		return werkOpdracht;
	}

	public void setWerkOpdracht(ResourceIdentifier werkOpdracht) {
		this.werkOpdracht = werkOpdracht;
	}

	public ResourceIdentifier getUitvoerder() {
		return uitvoerder;
	}

	public void setUitvoerder(ResourceIdentifier uitvoerder) {
		this.uitvoerder = uitvoerder;
	}

	public ResourceIdentifier getMedewerker() {
		return medewerker;
	}

	public void setMedewerker(ResourceIdentifier medewerker) {
		this.medewerker = medewerker;
	}

	public WerkOpdracht getSelectedWerkOpdracht() {
		return selectedWerkOpdracht;
	}

	public void setSelectedWerkOpdracht(WerkOpdracht selectedWerkOpdracht) {
		this.selectedWerkOpdracht = selectedWerkOpdracht;
	}

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "Uitvoeringsronde";
	}

	@Override
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(getName());
	}

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
	}

	@Override
	public Query getQuery() {
		if (query == null) {
			query = getDefaultQuery();
		}
		try {
			if (identity.inGroup("Uitvoerder", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("uitvoerder", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));
			}

			if (werkOpdracht != null) {
				query.addFilter(FilterUtils.equal("opdrachten", werkOpdracht));
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void search() {
		try {
			List<Uitvoeringsronde> list = (List<Uitvoeringsronde>) modelRepository
					.searchObjects(getQuery(), true, true, true);
			List<Uitvoeringsronde> filteredList = new ArrayList<Uitvoeringsronde>();

			if (uitvoerder != null) {
				results = searchUitvoeringronde(uitvoerder, list);
				filteredList = results;
			}

			if (medewerker != null) {
				if (filteredList.isEmpty()) {
					results = searchUitvoeringronde(medewerker, list);
				} else {
					results = searchUitvoeringronde(medewerker, filteredList);
				}
			}

			if (uitvoerder == null && medewerker == null) {
				results = list;
			}
		} catch (IOException e) {
			LOG.error("Can not search Uitvoeringsronde.", e);
		}
	}

	/**
	 * Temporary Experimental search helper method
	 * 
	 * @param id
	 * @param list
	 * @return
	 */
	public List<Uitvoeringsronde> searchUitvoeringronde(ResourceIdentifier id,
			List<Uitvoeringsronde> list) {
		List<Uitvoeringsronde> result = new ArrayList<Uitvoeringsronde>();
		try {
			boolean flag = false;
			for (Uitvoeringsronde ronde : list) {
				for (ResourceIdentifier opdrachtId : ronde.getOpdrachten()) {
					WerkOpdracht opdracht = (WerkOpdracht) modelRepository
							.loadObject(opdrachtId);
					if (uitvoerder != null) {
						if (opdracht.getUitvoerder().equals(uitvoerder)) {
							flag = true;
						}
					} else if (medewerker != null) {
						if (opdracht.getMedewerker().equals(medewerker)) {
							flag = true;
						}
					}
				}
				if (flag) {
					result.add(ronde);
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht.", e);
		}
		return result;
	}

	/**
	 * Zoekt alle werkopdrachten in een uitvoeringsronde.
	 * 
	 * @return
	 */
	public List<WerkOpdracht> getWerkOpdrachtenInUitvoeringsronde(
			Uitvoeringsronde ronde) {
		try {
			List<WerkOpdracht> opdrachten = new ArrayList<WerkOpdracht>();
			for (ResourceIdentifier id : ronde.getOpdrachten()) {
				WerkOpdracht opdracht = (WerkOpdracht) modelRepository
						.loadObject(id);
				opdrachten.add(opdracht);
			}
			return opdrachten;
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht", e);
		}
		return Collections.emptyList();
	}

	@Override
	public void delete() {

		try {
			// Set WerkOpdrachten inRonde flag to false
			for (WerkOpdracht opdracht : getWerkOpdrachtenInUitvoeringsronde(object)) {
				opdracht.setInRonde("0");
				// Save WerkOpdracht
				modelRepository.saveObject(opdracht);
			}
			// Delete Uitvoeringsronde
			modelRepository.deleteObject(object);
			clear();
			search();
		} catch (IOException e) {
			LOG.error("Can not delete model object.", e);
		}
	}

	/**
	 * Map Configuratie
	 * 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public MapConfiguration getConfiguration() throws IOException,
			InstantiationException, IllegalAccessException {

		MapContext context = (MapContext) modelRepository
				.loadObject(new ResourceKey("Form@12"));

		if (context != null) {
			MapConfiguration configuration = mapFactory
					.getConfiguration(context);

			mapFactory.createGeometryLayer(configuration.getContext(),
					GEOMETRY_LAYER_NAME, null, Point.class, null, true,
					"single", null, null);

			// Reset context
			configuration.setContext(context);

			// Reset layers
			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				layer.setFilter(null);
				layer.setHidden(true);
				layer.setSelection(Collections.EMPTY_LIST);
			}

			// Toon en zoom naar bordprobleem of ander probleem van de
			// geselecteerde WerkOpdracht
			if (selectedWerkOpdracht != null) {

				List<String> bordSelection = new ArrayList<String>();
				List<Geometry> anderProbleemGeoms = new ArrayList<Geometry>();

				// Get traject
				Traject traject = (Traject) modelRepository
						.loadObject(selectedWerkOpdracht.getTraject());

				// Load layer depending on traject type
				FeatureMapLayer trajectLayer = (FeatureMapLayer) context
						.getLayer(LabelUtils.lowerCamelCase(traject
								.getModelClass().getName()));

				if (trajectLayer != null) {
					trajectLayer.setHidden(false);
					trajectLayer.setFilter(FilterUtils.equal("naam",
							traject.getNaam()));
				}

				// BordLayer
				FeatureMapLayer bordLayer = (FeatureMapLayer) context
						.getLayer(LabelUtils.lowerCamelCase(LabelUtils
								.lowerCamelCase(traject.getModelClass()
										.getName() + "Bord")));

				if (bordLayer != null) {
					bordLayer.setHidden(false);
					bordLayer.setFilter(FilterUtils.equal("naam",
							traject.getNaam()));
				}
				// Probleem
				if (selectedWerkOpdracht.getProbleem() instanceof BordProbleem) {
					// Bord Probleem
					Bord bord = (Bord) modelRepository
							.loadObject(((BordProbleem) selectedWerkOpdracht
									.getProbleem()).getBord());
					bordSelection.add(bord.getId().toString());
					bordLayer.setSelection(bordSelection);
					Envelope envelope = GeometryUtils.getEnvelope(bord
							.getGeom());
					GeometryUtils.expandEnvelope(envelope, 0.1,
							context.getMaxBoundingBox());
					context.setBoundingBox(envelope);

				} else if (selectedWerkOpdracht.getProbleem() instanceof AnderProbleem) {
					// Ander Probleem
					GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) mapFactory
							.createGeometryLayer(configuration.getContext(),
									"geometry", null, Point.class, null, true,
									"single", null, null);
					AnderProbleem anderProbleem = (AnderProbleem) selectedWerkOpdracht
							.getProbleem();
					anderProbleemGeoms.add(anderProbleem.getGeom());
					geomLayer.setGeometries(anderProbleemGeoms);
					Envelope envelope = GeometryUtils.getEnvelope(anderProbleem
							.getGeom());
					GeometryUtils.expandEnvelope(envelope, 0.1,
							context.getMaxBoundingBox());
					context.setBoundingBox(envelope);
				}
			}
			return configuration;
		}
		return null;
	}

	/**
	 * Verwijderen van een werkopdracht uit een uitvoeringsronde.
	 * 
	 * @param ronde
	 * @param opdracht
	 */
	public void removeFromUitvoeringsronde() {
		// Verwijder werkopdracht uit ronde
		object.getOpdrachten().remove(
				modelRepository.getResourceIdentifier(selectedWerkOpdracht));
		// Set flag inRonde false
		selectedWerkOpdracht.setInRonde("0");
		try {
			// Save opdracht en ronde
			modelRepository.saveObject(object);
			modelRepository.saveObject(selectedWerkOpdracht);
		} catch (IOException e) {
			LOG.error("Can not save object.", e);
		}
	}

	/**
	 * Rapporteren van een WerkOpdracht in een Uitvoeringsronde.
	 * 
	 */
	public void rapporteerWerkOpdrachtInRonde() {
		try {
			selectedWerkOpdracht.setStatus(WerkopdrachtStatus.GERAPPORTEERD);
			selectedWerkOpdracht.setDatumGerapporteerd(new Date());
			modelRepository.saveObject(selectedWerkOpdracht);
		} catch (IOException e) {
			LOG.error("Can not save werkopdracht.", e);
		}
	}

	/**
	 * Rapporteren van een Uitvoeringsronde waarin alle WerkOpdrachten
	 * gerapporteerd zijn.
	 * 
	 */
	public void rapporteerUitvoeringsRonde() {
		try {

			if (checkWerkOpdrachtenGerapporteerd()) {
				object.setStatus(UitvoeringsrondeStatus.UITGEVOERD);
				modelRepository.saveObject(object);
				messages.info("Uitvoeringsronde succesvol bewaard.");
			} else {
				messages.error("Uitvoeringsronde niet bewaard: De uitvoeringsronde bevat nog niet-gerapporteerde werkopdrachten.");
			}
		} catch (IOException e) {
			LOG.error("Can not save Uitvoeringsronde.", e);
		}
	}

	/**
	 * Check of alle Werkopdrachten de status gerapporteerd hebben.
	 * 
	 * @return
	 */
	private boolean checkWerkOpdrachtenGerapporteerd() {
		boolean isGerapporteerd = true;
		try {
			for (ResourceIdentifier id : object.getOpdrachten()) {
				WerkOpdracht opdracht = (WerkOpdracht) modelRepository
						.loadObject(id);
				if (!opdracht.getStatus().equals(
						WerkopdrachtStatus.GERAPPORTEERD)) {
					isGerapporteerd = false;
				}
			}
		} catch (IOException e) {
			LOG.error("Can not load WerkOpdracht.", e);
		}
		return isGerapporteerd;
	}
}
