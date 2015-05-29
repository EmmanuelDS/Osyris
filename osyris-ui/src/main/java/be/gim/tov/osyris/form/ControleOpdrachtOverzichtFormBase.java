package be.gim.tov.osyris.form;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SortOrder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.conscientia.api.cache.CacheProducer;
import org.conscientia.api.mail.MailSender;
import org.conscientia.api.model.ModelClass;
import org.conscientia.api.model.ModelObjectList;
import org.conscientia.api.preferences.Preferences;
import org.conscientia.api.search.Query;
import org.conscientia.api.search.QueryOrderBy;
import org.conscientia.api.user.User;
import org.conscientia.api.user.UserProfile;
import org.conscientia.api.user.UserRepository;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.core.form.AbstractListForm;
import org.conscientia.core.model.DefaultModelObjectList;
import org.conscientia.core.resource.FileResource;
import org.conscientia.core.search.DefaultQuery;
import org.conscientia.core.search.DefaultQueryOrderBy;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.jsf.component.ComponentUtils;
import org.conscientia.jsf.event.ControllerEvent;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.w3c.dom.Document;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.api.Encoder;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.commons.resource.ResourceKey;
import be.gim.commons.resource.ResourceName;
import be.gim.peritia.codec.EncodableContent;
import be.gim.peritia.io.content.Content;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.api.context.RasterMapLayer;
import be.gim.specto.core.context.MapFactory;
import be.gim.specto.core.layer.feature.GeometryListFeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.NetwerkAnderProbleem;
import be.gim.tov.osyris.model.controle.NetwerkBordProbleem;
import be.gim.tov.osyris.model.controle.NetwerkControleOpdracht;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.controle.RouteControleOpdracht;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;
import be.gim.tov.osyris.model.controle.status.ProbleemStatus;
import be.gim.tov.osyris.model.encoder.ControleOpdrachtCSVModelEncoder;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Provincie;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.utils.AlphanumericSorting;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;
import be.gim.tov.osyris.pdf.XmlBuilder;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Named
@ViewScoped
public class ControleOpdrachtOverzichtFormBase extends
		AbstractListForm<ControleOpdracht> implements Serializable {
	private static final long serialVersionUID = -86881009141250710L;

	private static final Log LOG = LogFactory
			.getLog(ControleOpdrachtOverzichtFormBase.class);

	private static final String GEOMETRY_LAYER_NAME = "geometry";
	private static final String GEOMETRY_LAYER_LINE_NAME = "geometryLine";
	private static final String PERIODE_LENTE = "1";
	private static final String PERIODE_ZOMER = "2";
	private static final String PERIODE_HERFST = "3";

	private static final String OVERVIEW_MAP = "/META-INF/resources/osyris/xslts/overviewMap.xsl";
	private static final String CO_PDF = "/META-INF/resources/osyris/xslts/controleOpdrachtPdf.xsl";
	private static final String BORDFICHE_PDF = "/META-INF/resources/osyris/xslts/bordFichePdf.xsl";

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
	protected CacheProducer cacheProducer;

	protected String controleOpdrachtType;
	protected ResourceIdentifier regio;
	protected String regioCreate;
	protected String trajectType;
	protected String trajectNaam;
	protected String trajectTypeCreate;
	protected String trajectNaamCreate;
	protected Date vanDatum;
	protected Date totDatum;
	protected List<Bord> bewegwijzering;
	protected String probleemType;
	protected Probleem probleem;
	protected Probleem selectedProbleem;
	protected Envelope envelope = null;
	protected FeatureMapLayer bordLayer;
	protected ResourceIdentifier trajectId;
	protected Collection<String> imageKeys = new ArrayList<String>();
	protected String baseLayerName;
	protected String periode;
	protected String jaar;
	protected String editType;
	protected boolean hasOmleiding;
	protected boolean hasErrors;
	protected List<BordProbleem> bordProblemen;
	protected List<AnderProbleem> andereProblemen;
	protected List<Geometry> anderProbleemLineGeoms;
	protected List<String> bordSelection;
	protected List<Geometry> anderProbleemPointGeoms;

	public String getControleOpdrachtType() {
		return controleOpdrachtType;
	}

	public void setControleOpdrachtType(String controleOpdrachtType) {
		this.controleOpdrachtType = controleOpdrachtType;
	}

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

	public String getRegioCreate() {
		return regioCreate;
	}

	public void setRegioCreate(String regioCreate) {
		this.regioCreate = regioCreate;
	}

	public String getTrajectTypeCreate() {
		return trajectTypeCreate;
	}

	public void setTrajectTypeCreate(String trajectTypeCreate) {
		this.trajectTypeCreate = trajectTypeCreate;
	}

	public String getTrajectNaamCreate() {
		return trajectNaamCreate;
	}

	public void setTrajectNaamCreate(String trajectNaamCreate) {
		this.trajectNaamCreate = trajectNaamCreate;
	}

	public Date getVanDatum() {
		return vanDatum;
	}

	public void setVanDatum(Date vanDatum) {
		this.vanDatum = vanDatum;
	}

	public Date getTotDatum() {
		return totDatum;
	}

	public void setTotDatum(Date totDatum) {
		this.totDatum = totDatum;
	}

	public MapViewer getViewer() {
		return (MapViewer) ComponentUtils.findComponent("viewer");
	}

	public List<Bord> getBewegwijzering() {
		return bewegwijzering;
	}

	public void setBewegwijzering(List<Bord> bewegwijzering) {
		this.bewegwijzering = bewegwijzering;
	}

	public String getProbleemType() {
		return probleemType;
	}

	public void setProbleemType(String probleemType) {
		this.probleemType = probleemType;
	}

	public Probleem getProbleem() {
		return probleem;
	}

	public void setProbleem(Probleem probleem) {
		this.probleem = probleem;
	}

	public Probleem getSelectedProbleem() {
		return selectedProbleem;
	}

	public void setSelectedProbleem(Probleem selectedProbleem) {
		this.selectedProbleem = selectedProbleem;
	}

	public FeatureMapLayer getBordLayer() {
		return bordLayer;
	}

	public void setBordLayer(FeatureMapLayer bordLayer) {
		this.bordLayer = bordLayer;
	}

	public ResourceIdentifier getTrajectId() {
		return trajectId;
	}

	public void setTrajectId(ResourceIdentifier trajectId) {
		this.trajectId = trajectId;
	}

	public String getBaseLayerName() {
		return baseLayerName;
	}

	public void setBaseLayerName(String baseLayerName) {
		this.baseLayerName = baseLayerName;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public String getJaar() {
		return jaar;
	}

	public void setJaar(String jaar) {
		this.jaar = jaar;
	}

	public String getEditType() {
		if (editType == null) {
			editType = "drawPoint";
		}
		return editType;
	}

	public void setEditType(String editType) {
		this.editType = editType;
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	public void setBordProblemen(List<BordProbleem> bordProblemen) {
		this.bordProblemen = bordProblemen;
	}

	public void setAndereProblemen(List<AnderProbleem> andereProblemen) {
		this.andereProblemen = andereProblemen;
	}

	public List<Geometry> getAnderProbleemLineGeoms() {
		return anderProbleemLineGeoms;
	}

	public void setAnderProbleemLineGeoms(List<Geometry> anderProbleemLineGeoms) {
		this.anderProbleemLineGeoms = anderProbleemLineGeoms;
	}

	public List<String> getBordSelection() {
		return bordSelection;
	}

	public void setBordSelection(List<String> bordSelection) {
		this.bordSelection = bordSelection;
	}

	public List<Geometry> getAnderProbleemPointGeoms() {
		return anderProbleemPointGeoms;
	}

	public void setAnderProbleemPointGeoms(
			List<Geometry> anderProbleemPointGeoms) {
		this.anderProbleemPointGeoms = anderProbleemPointGeoms;
	}

	// METHODS
	@PostConstruct
	public void init() throws IOException {
		search();
	}

	@Override
	public String getName() {
		return "ControleOpdracht";
	}

	@Override
	public ModelClass getModelClass() {
		return modelRepository.getModelClass(getName());
	}

	@Override
	protected Query transformQuery(Query query) {

		query = new DefaultQuery(query);

		if (trajectType != null && trajectId != null) {
			query.addFilter(FilterUtils.equal("traject", trajectId));
		} else {
			if (trajectType != null) {
				query.addFilter(FilterUtils.equal("trajectType", trajectType));
			}

			if (regio != null) {
				query.addFilter(FilterUtils.equal("regioId", regio));
			}
		}

		if (vanDatum != null && totDatum != null) {
			query.addFilter(FilterUtils.and(FilterUtils.lessOrEqual(
					"datumLaatsteWijziging", totDatum), FilterUtils
					.greaterOrEqual("datumLaatsteWijziging", vanDatum)));
		}

		try {
			// Medewerker moet gevalideerd en geannuleerd status niet kunnen
			// zien
			if (identity.inGroup("Routedokter", "CUSTOM")) {
				return query;
			} else if (identity.inGroup("Medewerker", "CUSTOM")) {
				query.addFilter(FilterUtils.equal("medewerker", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));

				query.addFilter(FilterUtils.notEqual("status",
						ControleOpdrachtStatus.GEANNULEERD));

			} else if (identity.inGroup("PeterMeter", "CUSTOM")) {
				// PeterMeter kan enkel status uit te voeren en gevalideerd zien
				query.addFilter(FilterUtils.equal("peterMeter", modelRepository
						.getResourceName(userRepository.loadUser(identity
								.getUser().getId()))));

				query.addFilter(FilterUtils.and(FilterUtils.notEqual("status",
						ControleOpdrachtStatus.TE_CONTROLEREN), FilterUtils
						.notEqual("status", ControleOpdrachtStatus.GEANNULEERD)));
			}
		} catch (IOException e) {
			LOG.error("Can not load user.", e);
		}

		DefaultQueryOrderBy orderBy = new DefaultQueryOrderBy(
				FilterUtils.property("datumLaatsteWijziging"));
		orderBy.setSortOrder(SortOrder.DESCENDING);

		query.setOrderBy(Collections.singletonList((QueryOrderBy) orderBy));

		return query;
	}

	@Override
	public void create() {

		try {
			regioCreate = null;
			trajectTypeCreate = null;
			trajectNaamCreate = null;

			object = null;
			bewegwijzering = null;

			if (controleOpdrachtType.equals("route")) {
				object = (ControleOpdracht) modelRepository.createObject(
						modelRepository.getModelClass("RouteControleOpdracht"),
						null);
			} else if (controleOpdrachtType.equals("netwerk")) {
				object = (ControleOpdracht) modelRepository
						.createObject(modelRepository
								.getModelClass("NetwerkControleOpdracht"), null);
			}
		} catch (InstantiationException e) {
			messages.error("Fout bij het aanmaken van controleopdracht: "
					+ e.getMessage());
			LOG.error("Can not instantiate model object.", e);
		} catch (IllegalAccessException e) {
			messages.error("Fout bij het aanmaken van controleopdracht: "
					+ e.getMessage());
			LOG.error("Illegal access at creation model object.", e);
		}
	}

	@Override
	public void save() {

		setHasErrors(true);
		try {
			// Indien route achterhalen van de TrajectId via de routeNaam
			if (object.getTraject() == null) {
				messages.warn("Gelieve een traject voor deze controleopdracht te selecteren.");
			}

			if (object.getTraject() != null) {
				setHasErrors(false);
				probleem = null;
				modelRepository.saveObject(object);
				messages.info("Controleopdracht succesvol bewaard.");
				// clear();
				search();
			}
		} catch (IOException e) {
			messages.error("Fout bij het bewaren van controleopdracht: "
					+ e.getMessage());
			LOG.error("Can not save model object.", e);
		}
	}

	@Override
	public void clear() {
		probleem = null;
		selectedProbleem = null;
		object = null;
		controleOpdrachtType = null;
		bewegwijzering = null;
		trajectId = null;
		trajectType = null;
		trajectNaam = null;
	}

	/**
	 * Map configuratie bij het aanmaken van een ControleOpdracht.
	 * 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws FactoryException
	 * @throws NoSuchAuthorityCodeException
	 */
	@SuppressWarnings("static-access")
	public MapConfiguration getSearchConfiguration() throws Exception {

		MapContext context = (MapContext) modelRepository
				.loadObject(new ResourceKey("Form@12"));

		if (context != null) {
			MapConfiguration configuration = mapFactory
					.getConfiguration(context);

			// Retrieve context instance from configuration.
			context = configuration.getContext();

			// Reset layers
			resetLayers(context);

			// Startconfiguratie zoomt naar Provincie OVL
			FeatureMapLayer provincieLayer = (FeatureMapLayer) context
					.getLayer("provincie");
			provincieLayer.setHidden(false);
			Provincie provincie = (Provincie) modelRepository
					.getUniqueResult(modelRepository.searchObjects(
							new DefaultQuery("Provincie"), true, true));
			Envelope envelope = GeometryUtils.getEnvelope(provincie.getGeom());
			context.setBoundingBox(envelope);

			// Indien netwerk CO lussen zichtbaar maken
			if (controleOpdrachtType.equals("netwerk")) {

				// Alle lussen tonen
				FeatureMapLayer wandelLusLayer = (FeatureMapLayer) context
						.getLayer("wandelNetwerkLus");
				wandelLusLayer.setHidden(false);

				FeatureMapLayer fietsLusLayer = (FeatureMapLayer) context
						.getLayer("fietsNetwerkLus");
				fietsLusLayer.setHidden(false);
			}

			return configuration;
		}
		return null;
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

			// Retrieve context instance from configuration.
			context = configuration.getContext();

			// Reset layers
			resetLayers(context);

			// Setup layers
			setupLayers(configuration, context, true);

			return configuration;
		}
		return null;
	}

	/**
	 * Maakt het bewegwijzeringsverslag voor het gezochte traject.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void createBewegwijzeringVerslag() {

		try {
			// Routes filteren op trajectNaam
			if (trajectTypeCreate.contains("Route")) {
				QueryBuilder builder = new QueryBuilder("RouteBord");
				// builder.addFilter(FilterUtils.equal("naam", trajectNaam));
				builder.addFilter(FilterUtils.and(
						FilterUtils.equal("naam", trajectNaamCreate),
						FilterUtils.equal("actief", "1")));

				bewegwijzering = (List<Bord>) modelRepository.searchObjects(
						builder.build(), true, true);

				Collections.sort(bewegwijzering, new AlphanumericSorting());
			}

			else if (trajectTypeCreate.contains("Lus")) {

				// MapViewer viewer = getViewer();
				// MapContext context = viewer.getConfiguration().getContext();
				//
				// for (FeatureMapLayer layer : context.getFeatureLayers()) {
				// if (layer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
				// layer.setFilter(FilterUtils.equal("naam",
				// trajectNaamCreate));
				// searchTrajectId(layer);
				// }
				// }
				NetwerkLus lus = (NetwerkLus) modelRepository.loadObject(object
						.getTraject());

				bewegwijzering = Beans.getReference(OsyrisModelFunctions.class)
						.getNetwerkBordVolgordeLus(lus);

			}
		} catch (IOException e) {
			messages.error("Fout bij het aanmaken van bewegwijzeringsverslag: "
					+ e.getMessage());
			LOG.error("Can not search objects.", e);
			bewegwijzering = null;
		}
	}

	/**
	 * Zoekt het bewegwijzeringsverslag voor een opgegeven traject
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Bord> createBewegwijzering(ResourceIdentifier trajectId) {

		List<Bord> result = new ArrayList<Bord>();

		try {
			Traject t = (Traject) modelRepository.loadObject(trajectId);
			if (t == null) {
				result = Collections.emptyList();
			}
			if (t instanceof Route) {

				// Routes filteren op trajectNaam
				QueryBuilder builder = new QueryBuilder("RouteBord");
				// builder.addFilter(FilterUtils.equal("naam", t.getNaam()));
				builder.addFilter(FilterUtils.and(
						FilterUtils.equal("naam", t.getNaam()),
						FilterUtils.equal("actief", "1")));

				result = (List<Bord>) modelRepository.searchObjects(
						builder.build(), true, true);
				Collections.sort(result, new AlphanumericSorting());

			} else if (t instanceof NetwerkLus) {
				NetwerkLus lus = ((NetwerkLus) t);

				result = Beans.getReference(OsyrisModelFunctions.class)
						.getNetwerkBordVolgordeLus(lus);
			} else {
				result = Collections.emptyList();
			}

		} catch (IOException e) {
			messages.error("Fout bij het ophalen van bewegwijzeringsverslag voor traject: "
					+ e.getMessage());
			LOG.error("Can not search objects.", e);
			result = Collections.emptyList();
		}
		return result;
	}

	/**
	 * Zoekt het traject aan de hand van de ingegeven zoekparameters
	 * 
	 * @throws IOException
	 */
	public void searchTraject() throws IOException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		envelope = viewer.getContentExtent();
		bewegwijzering = Collections.emptyList();

		koppelTraject();

		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			layer.setFilter(null);
			layer.setHidden(true);
			layer.set("selectable", false);

			// Provincie altijd zichtbaar
			if (layer.getLayerId().equalsIgnoreCase("provincie")) {
				layer.setHidden(false);
			}

			// Traject
			else if (layer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
				// Route of Lussen
				searchRouteLayer(layer);
				envelope = viewer.getContentExtent(layer);
			}
			// RouteBord
			else if (layer.getLayerId().equalsIgnoreCase(
					trajectTypeCreate + "Bord")) {
				searchRouteLayer(layer);
			}
			// NetwerkBord
			else if (layer.getLayerId().equalsIgnoreCase(
					trajectTypeCreate.replace("Lus", "") + "Bord")) {
				searchNetwerkBordLayer(layer);
			}
			// Knooppunten
			else if (layer.getLayerId().equalsIgnoreCase(
					trajectTypeCreate.replace("Lus", "") + "Knooppunt")) {
				searchKnooppuntLayer2(layer);
			}
		}

		viewer.updateCurrentExtent(envelope);
		viewer.updateContext(null);
	}

	/**
	 * Zoekoperaties op de NetwerkBord lagen.
	 * 
	 * @param layer
	 */
	private void searchNetwerkBordLayer(FeatureMapLayer layer) {

		layer.setFilter(null);
		layer.setHidden(false);

		try {

			// Indien nieuwe ControleOpdracht koppelen van TrajectID aan
			// ControleOpdracht
			// MapViewer viewer = getViewer();
			// MapContext context = viewer.getConfiguration().getContext();
			//
			// for (FeatureMapLayer mapLayer : context.getFeatureLayers()) {
			// if (mapLayer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
			// mapLayer.setFilter(FilterUtils.equal("naam",
			// trajectNaamCreate));
			// searchTrajectId(mapLayer);
			// }
			// }

			NetwerkLus lus = (NetwerkLus) modelRepository.loadObject(object
					.getTraject());

			layer.setFilter(FilterUtils.in("segmenten", lus.getSegmenten()));

			List<String> bordIds = new ArrayList<String>();

			for (Bord b : createBewegwijzering(object.getTraject())) {
				bordIds.add(b.getId().toString());
			}
			layer.setFilter(FilterUtils.in("id", bordIds));

		} catch (IOException e) {
			LOG.error("Can not load NetwerkLus.", e);
		}
	}

	/**
	 * Zoekoperaties op de Route lagen.
	 * 
	 * @param layer
	 * @param viewer
	 */
	private void searchRouteLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		if (trajectNaamCreate != null) {
			layer.setFilter(FilterUtils.like("naam", trajectNaamCreate));
		}
	}

	private void searchKnooppuntLayer(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.setFilter(null);
		layer.setSelection(Collections.EMPTY_LIST);

		try {
			NetwerkLus lus = (NetwerkLus) modelRepository.loadObject(object
					.getTraject());

			Set<Long> knooppuntFilterIds = new HashSet<Long>();

			for (ResourceIdentifier segment : lus.getSegmenten()) {
				NetwerkSegment seg = (NetwerkSegment) modelRepository
						.loadObject(segment);

				NetwerkKnooppunt vanKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getVanKnooppunt());

				NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getNaarKnooppunt());

				if (vanKp != null) {
					knooppuntFilterIds.add(vanKp.getId());
				}

				if (naarKp != null) {
					knooppuntFilterIds.add(naarKp.getId());
				}
			}
			layer.setFilter(FilterUtils.in("id", knooppuntFilterIds));

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	private void searchKnooppuntLayer2(FeatureMapLayer layer) {

		layer.setHidden(false);
		layer.setFilter(null);
		layer.setSelection(Collections.EMPTY_LIST);

		try {

			DefaultQuery query = new DefaultQuery();
			query.setModelClassName(trajectTypeCreate);
			query.setFilter(FilterUtils.equal("naam", trajectNaamCreate));
			List<NetwerkLus> lussen = (List<NetwerkLus>) modelRepository
					.searchObjects(query, false, false);
			NetwerkLus lus = lussen.get(0);

			Set<Long> knooppuntFilterIds = new HashSet<Long>();

			for (ResourceIdentifier segment : lus.getSegmenten()) {
				NetwerkSegment seg = (NetwerkSegment) modelRepository
						.loadObject(segment);

				NetwerkKnooppunt vanKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getVanKnooppunt());

				NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getNaarKnooppunt());

				if (vanKp != null) {
					knooppuntFilterIds.add(vanKp.getId());
				}

				if (naarKp != null) {
					knooppuntFilterIds.add(naarKp.getId());
				}
			}
			layer.setFilter(FilterUtils.in("id", knooppuntFilterIds));

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * Filteren van de laag ahv trajectNaam en koppelen van de trajectID aan de
	 * controleOpdracht. Dit is enkel toepasbaar op routes en lussen
	 * 
	 * @param layer
	 */
	// public void searchTrajectId(FeatureMapLayer layer) {
	//
	// if (layer.getLayerId().equalsIgnoreCase(trajectTypeCreate)) {
	// FeatureCollection<SimpleFeatureType, SimpleFeature> features =
	// getViewer()
	// .getFeature(layer, getViewer().getContext().getSrsName(),
	// getViewer().getContext().getBoundingBox(), null,
	// FilterUtils.equal("naam", trajectNaamCreate), null,
	// 1);
	//
	// FeatureIterator<SimpleFeature> iterator = features.features();
	//
	// try {
	// if (features.size() == 1) {
	// while (iterator.hasNext()) {
	// SimpleFeature feature = iterator.next();
	// object.setTraject(new ResourceKey("Traject", feature
	// .getAttribute("id").toString()));
	// }
	// }
	// } finally {
	// iterator.close();
	// }
	// }
	// }

	/**
	 * Toont het traject met bijbehorende borden voor controleopdrachten in
	 * raadpleeg modus.
	 * 
	 * @param controleOpdracht
	 */
	public void showOpdrachtMapData(ResourceIdentifier trajectId) {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		FeatureMapLayer layer = null;
		String layerId = null;

		try {
			// Get traject
			Traject t = (Traject) modelRepository.loadObject(trajectId);

			// Routes
			layerId = Character.toLowerCase(t.getModelClass().getName()
					.charAt(0))
					+ t.getModelClass().getName().substring(1);
			layer = (FeatureMapLayer) context.getLayer(layerId);
			layer.setHidden(false);
			layer.setFilter(FilterUtils.equal("naam", t.getNaam()));
			context.setBoundingBox(viewer.getContentExtent(layer));

			// Routeborden
			layerId = layerId + "Bord";
			layer = (FeatureMapLayer) context.getLayer(layerId);
			layer.setHidden(false);
			layer.setFilter(FilterUtils.equal("naam", t.getNaam()));

			viewer.updateContext(null);

		} catch (IOException e) {
			LOG.error("Can load object", e);
		}
	}

	/**
	 * Annuleren van een controleopdracht
	 * 
	 */
	public void annuleerControleOpdracht() {

		if (object != null) {
			object.setStatus(ControleOpdrachtStatus.GEANNULEERD);

			try {
				modelRepository.saveObject(object);
				messages.info("Controleopdracht succesvol geannuleerd.");
				clear();
				search();

			} catch (IOException e) {
				messages.error("Fout bij het annuleren van controleopdracht: "
						+ e.getMessage());
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Heropenen geannuleerde controleOpdracht
	 * 
	 */
	public void reopenControleOpdracht() {

		if (object != null) {

			object.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
			object.setDatumTeControleren(new Date());

			try {
				modelRepository.saveObject(object);
				messages.info("Controleopdracht succesvol heropend. De controleopdracht staat opnieuw in status 'Te controleren'.");
				clear();
				search();

			} catch (IOException e) {
				messages.error("Fout bij het heropenen van controleopdracht: "
						+ e.getMessage());
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Verzenden van controleOpdracht naar de betrokken peterMeter.
	 * 
	 */
	public void verzendenControleOpdracht() {

		setHasErrors(true);

		if (object != null) {

			if (checkControleOpdracht(object)) {

				setHasErrors(false);
				object.setStatus(ControleOpdrachtStatus.UIT_TE_VOEREN);
				object.setDatumUitTeVoeren(new Date());
				object.setDatumLaatsteWijziging(new Date());

				try {
					modelRepository.saveObject(object);

					// Send confirmatie mail naar peterMeter
					String mailServiceStatus = DefaultConfiguration.instance()
							.getString("service.mail.controleOpdracht");

					if (mailServiceStatus.equalsIgnoreCase("on")) {
						sendConfirmationMail();
					}

					// clear();
					object = null;
					controleOpdrachtType = null;
					bewegwijzering = null;
					trajectId = null;

					search();
					messages.info("Controleopdracht succesvol verzonden.");

				} catch (IOException e) {

					messages.error("Fout bij het verzenden van controleopdracht: "
							+ e.getMessage());
					LOG.error("Can not save object.", e);
				} catch (Exception e) {

					messages.error("Fout bij het versturen van bevestigingsmail naar de betrokken Peter/Meter: "
							+ e.getMessage());
					LOG.error("Can not send email", e);
				}
			}
		}
	}

	/**
	 * Rapporteren van controleOpdracht naar TOV
	 * 
	 */
	public void rapporterenControleOpdracht() {

		if (object != null) {

			object.setStatus(ControleOpdrachtStatus.GERAPPORTEERD);
			object.setDatumGerapporteerd(new Date());
			object.setDatumLaatsteWijziging(new Date());

			try {
				modelRepository.saveObject(object);

				clear();
				search();

				messages.info("Controleopdracht succesvol gerapporteerd.");

			} catch (IOException e) {
				messages.error("Fout bij het rapporteren van controleopdracht: "
						+ e.getMessage());
				LOG.error("Can not save object.", e);
			}
		}
	}

	/**
	 * Stuurt een mail naar de betrokken peterMeter dat er een nieuwe
	 * controleOpdracht op status uit te voeren beschikbaar is
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	private void sendConfirmationMail() throws IOException, Exception {

		User medewerker = (User) modelRepository.loadObject(object
				.getMedewerker());

		UserProfile profiel = (UserProfile) medewerker.getAspect("UserProfile",
				modelRepository, true);

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("preferences", preferences);

		if (object.getPeriode().equals(PERIODE_LENTE)) {
			variables.put("periode", "Lente");
		} else if (object.getPeriode().equals(PERIODE_ZOMER)) {
			variables.put("periode", "Zomer");
		} else if (object.getPeriode().equals(PERIODE_HERFST)) {
			variables.put("periode", "Herfst");
		}

		variables.put("jaar", object.getJaar());
		variables.put("id", object.get("id"));
		variables.put("trajectType", object.getTrajectType());
		variables.put("traject",
				modelRepository.getObjectLabel(object.getTraject()));
		variables.put("regio", Beans.getReference(OsyrisModelFunctions.class)
				.getTrajectRegio(object.getTraject()));
		variables.put("medewerker",
				profiel.getLastName() + " " + profiel.getFirstName());

		User peterMeter = (User) modelRepository.loadObject(object
				.getPeterMeter());
		UserProfile pmProfiel = (UserProfile) peterMeter.getAspect(
				"UserProfile", modelRepository, true);

		mailSender.sendMail(preferences.getNoreplyEmail(),
				Collections.singleton(pmProfiel.getEmail()),
				"/META-INF/resources/core/mails/confirmControleOpdracht.fmt",
				variables);

		messages.info("Er is een email verzonden naar de betrokken PeterMeter.");
	}

	/**
	 * Zoomt naar een bepaald bord op de kaart
	 * 
	 * @param bord
	 */
	public void zoomToBord(Bord bord) {

		MapViewer viewer = getViewer();
		Envelope envelope = new Envelope(bord.getGeom().getCoordinate());

		// Niet te ver inzoomen op Bord
		GeometryUtils.expandEnvelope(envelope, 0.02, viewer.getConfiguration()
				.getContext().getMaxBoundingBox());

		viewer.updateCurrentExtent(envelope);
	}

	/**
	 * Oplijsten van problemen van het type BordProbleem
	 * 
	 * @return
	 */
	public List<BordProbleem> getBordProblemen() {

		bordProblemen = new ArrayList<BordProbleem>();

		if (object != null) {
			for (Probleem probleem : object.getProblemen()) {
				if (probleem instanceof BordProbleem) {
					bordProblemen.add((BordProbleem) probleem);
				}
			}
		}
		return bordProblemen;
	}

	/**
	 * Oplijsten van problemen van het type AnderProbleem
	 * 
	 * @return
	 */
	public List<AnderProbleem> getAndereProblemen() {

		// andereProblemen = new ArrayList<AnderProbleem>();
		andereProblemen = new ArrayList<AnderProbleem>();

		if (object != null) {
			for (Probleem probleem : object.getProblemen()) {
				if (probleem instanceof AnderProbleem) {
					andereProblemen.add((AnderProbleem) probleem);
				}
			}
		}
		return andereProblemen;
	}

	/**
	 * Aanmaken van een probleem bij een controleopdracht
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void createProbleem() throws InstantiationException,
			IllegalAccessException {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();
		setProbleem(null);

		// Bordprobleem
		if ("bord".equals(probleemType)) {

			viewer.getContext().setShowFeatureInfoControl(true);
			viewer.getContext().setShowDrawPointControl(false);
			viewer.getContext().setShowDrawLineStringControl(false);

			if (object.getTrajectType().contains("route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteBordProbleem", null);
			} else if (object.getTrajectType().contains("lus")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkBordProbleem", null);
			}
			// Itereren over de lagen en de correcte lagen selecteerbaar zetten
			for (FeatureMapLayer layer : context.getFeatureLayers()) {

				if (layer.getLayerId().equalsIgnoreCase(
						object.getTrajectType() + "Bord")
						|| layer.getLayerId().equalsIgnoreCase(
								object.getTrajectType().replace("lus", "")
										+ "Bord")) {
					layer.set("selectable", true);
					layer.setSelection(new ArrayList<String>(1));

				} else if (layer.getLayerId().equalsIgnoreCase(
						GEOMETRY_LAYER_NAME)
						|| layer.getLayerId().equalsIgnoreCase(
								GEOMETRY_LAYER_LINE_NAME)) {
					layer.setHidden(true);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(new ArrayList<Geometry>(1));
				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		}
		// Ander probleem
		else if ("ander".equals(probleemType)) {

			viewer.getContext().setShowFeatureInfoControl(false);
			viewer.getContext().setShowDrawPointControl(true);
			viewer.getContext().setShowDrawLineStringControl(false);

			setEditType(null);

			if (object.getTrajectType().contains("route")) {
				probleem = (Probleem) modelRepository.createObject(
						"RouteAnderProbleem", null);
			} else if (object.getTrajectType().contains("lus")) {
				probleem = (Probleem) modelRepository.createObject(
						"NetwerkAnderProbleem", null);
			}

			for (FeatureMapLayer layer : context.getFeatureLayers()) {
				if (layer.getLayerId().equalsIgnoreCase(GEOMETRY_LAYER_NAME)
						|| layer.getLayerId().equalsIgnoreCase(
								GEOMETRY_LAYER_LINE_NAME)) {
					layer.setHidden(false);
					((GeometryListFeatureMapLayer) layer)
							.setGeometries(new ArrayList<Geometry>(1));
				} else {
					layer.set("selectable", false);
					layer.setSelection(Collections.EMPTY_LIST);
				}
			}
		}
		viewer.updateContext(null);
	}

	/**
	 * Toevoegen van een probleem aan de controleOpdracht
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * 
	 */
	public void addProbleem() throws InstantiationException,
			IllegalAccessException {

		try {

			// Toevoegen probleem aan controleOpdracht
			Probleem p = getProbleem();

			if (checkProbleem(p)) {

				object.getProblemen().add(p);

				// Reset probleem
				probleemType = null;
				setProbleem(null);

				modelRepository.saveObject(object);
				messages.info("Probleem succesvol toegevoegd.");

				// Deselect all features
				MapViewer viewer = getViewer();
				MapConfiguration configuration = viewer.getConfiguration();

				MapContext context = viewer.getConfiguration().getContext();
				for (FeatureMapLayer layer : context.getFeatureLayers()) {
					layer.setSelection(new ArrayList<String>(1));
				}

				setupLayers(configuration, context, false);
				context.setShowFeatureInfoControl(false);
				context.setShowDrawPointControl(false);
				context.setShowDrawLineStringControl(false);
				viewer.updateContext(null);
			}
		} catch (IOException e) {

			messages.error("Fout bij het toevoegen van probleem: "
					+ e.getMessage());
			LOG.error("Can not save object.", e);
		}
	}

	/**
	 * Event bij het selecteren van features op de kaart
	 * 
	 * @param event
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void onSelectFeatures(ControllerEvent event) throws IOException {

		// Selecteren bord
		if (getProbleem() != null && getProbleem() instanceof BordProbleem) {

			List<String> ids = (List<String>) event.getParams().get(
					"featureIds");
			String layerId = (String) event.getParams().get("layerId");
			FeatureMapLayer layer = (FeatureMapLayer) getViewer().getContext()
					.getLayer(layerId);

			// Workaround om te vermijden dat niet zichtbare borden geselecteerd
			// worden
			// Enkel de Borden behorende tot de bewegwijzering van het Traject
			// mogen geselecteerd
			// worden.
			List<String> idsFiltered = new ArrayList<String>();
			for (Bord b : createBewegwijzering(object.getTraject())) {
				if (ids.contains(b.getId().toString())) {
					idsFiltered.add(b.getId().toString());
				}
			}

			// Limiteren selectie Borden tot 1
			if (idsFiltered.size() == 1) {
				String id = idsFiltered.iterator().next();
				layer.setSelection(idsFiltered);
				((BordProbleem) getProbleem()).setBord(new ResourceKey("Bord",
						id));
			} else {
				messages.error("Er zijn meerdere borden geselecteerd. Mogelijk bevinden deze borden zich op dezelfde locatie. Gelieve in dit geval de 'Tonen attributen' knop te gebruiken om uw selectie te verfijnen.");
				layer.setSelection(new ArrayList<String>(1));
			}
		}
	}

	/**
	 * Event bij het updaten van features op de kaart
	 * 
	 * @param event
	 */
	public void onUpdateFeatures(ControllerEvent event) {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		GeometryListFeatureMapLayer pointLayer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_NAME);

		GeometryListFeatureMapLayer lineLayer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_LINE_NAME);

		// Debug
		String layerId = (String) event.getParams().get("layerId");
		FeatureMapLayer layer = (FeatureMapLayer) getViewer().getContext()
				.getLayer(layerId);

		if (viewer.getEditLayerId().equals(GEOMETRY_LAYER_NAME)) {
			// Slechts 1 punt mag ingegeven worden
			if (pointLayer.getGeometries().size() > 1) {
				pointLayer.getGeometries().remove(0);
			}

			if (pointLayer.getGeometries().size() == 1) {
				if (probleem instanceof AnderProbleem) {
					((AnderProbleem) probleem).setGeom(pointLayer
							.getGeometries().iterator().next());
				}
			}
		}

		if (viewer.getEditLayerId().equals(GEOMETRY_LAYER_LINE_NAME)) {
			// Slechts 1 lijn mag ingegeven worden
			if (lineLayer.getGeometries().size() > 1) {
				lineLayer.getGeometries().remove(0);
			}

			if (lineLayer.getGeometries().size() == 1) {
				if (probleem instanceof AnderProbleem) {
					((AnderProbleem) probleem).setGeomOmleiding(lineLayer
							.getGeometries().iterator().next());
				}
			}
		}
	}

	/**
	 * Zoomen naar bordprobleem op de kaart
	 * 
	 * @param bordProbleem
	 */
	public void zoomToBordProbleem(BordProbleem bordProbleem) {

		MapViewer viewer = getViewer();
		Envelope envelope;

		try {
			envelope = new Envelope(
					((Bord) modelRepository.loadObject(bordProbleem.getBord()))
							.getGeom().getCoordinate());

			// Niet te ver inzoomen op Bord
			GeometryUtils.expandEnvelope(envelope, 0.02, viewer
					.getConfiguration().getContext().getMaxBoundingBox());
			viewer.updateCurrentExtent(envelope);

		} catch (IOException e) {
			LOG.error("Can not load object.", e);
		}
	}

	/**
	 * Zoomen naar een anderProbleem op de kaart
	 * 
	 * @param bordProbleem
	 */
	public void zoomToAnderProbleem(AnderProbleem anderProbleem) {

		MapViewer viewer = getViewer();
		Envelope envelope;

		GeometryListFeatureMapLayer layer = (GeometryListFeatureMapLayer) viewer
				.getConfiguration().getContext().getLayer(GEOMETRY_LAYER_NAME);
		layer.setHidden(false);

		if (anderProbleem.getGeom() instanceof Point) {

			envelope = new Envelope(anderProbleem.getGeom().getCoordinate());

			// Niet te ver inzoomen op Probleempunt
			GeometryUtils.expandEnvelope(envelope, 0.02, viewer
					.getConfiguration().getContext().getMaxBoundingBox());

			viewer.updateCurrentExtent(envelope);
		}

		if (anderProbleem.getGeomOmleiding() != null
				&& anderProbleem.getGeomOmleiding() instanceof LineString) {

			envelope = new Envelope(anderProbleem.getGeomOmleiding()
					.getEnvelopeInternal());
			viewer.updateCurrentExtent(envelope);
		}
	}

	/**
	 * Valideren ven een probleem
	 * 
	 */
	public void validateProbleem() {

		try {
			modelRepository.saveObject(selectedProbleem);
		} catch (IOException e) {
			LOG.error("Can not save object.", e);
		}
	}

	/**
	 * Automatisch aanmaken van controleOpdrachten voor een gekozen peridoe voor
	 * de toegewezen PetersMeters aan een traject.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void createControleOpdrachten() {

		try {
			int counter = 0;

			// Filter trajecten waar een PeterMeter is aan toegekend voor de
			// gekozen periode.
			DefaultQuery routeQuery = new DefaultQuery();
			routeQuery.setModelClassName("Route");

			DefaultQuery lusQuery = new DefaultQuery();
			lusQuery.setModelClassName("NetwerkLus");

			if (periode.equals(PERIODE_LENTE.toString())) {
				routeQuery.addFilter(FilterUtils.notEqual("peterMeter1", null));
				lusQuery.addFilter(FilterUtils.notEqual("peterMeter1", null));
			}

			else if (periode.equals(PERIODE_ZOMER.toString())) {
				routeQuery.addFilter(FilterUtils.notEqual("peterMeter2", null));
				lusQuery.addFilter(FilterUtils.notEqual("peterMeter2", null));
			}

			else if (periode.equals(PERIODE_HERFST.toString())) {
				routeQuery.addFilter(FilterUtils.notEqual("peterMeter3", null));
				lusQuery.addFilter(FilterUtils.notEqual("peterMeter3", null));
			}

			List<Traject> routes = (List<Traject>) modelRepository
					.searchObjects(routeQuery, false, false);

			List<Traject> lussen = (List<Traject>) modelRepository
					.searchObjects(lusQuery, false, false);

			List<Traject> trajecten = new ArrayList<Traject>();
			trajecten.addAll(routes);
			trajecten.addAll(lussen);

			// Voor Routes en NetwerkLussen trajecttypes wordt voor de gekozen
			// periode
			// waar een peterMeter is
			// toegekend een controleOpdracht aangemaakt
			for (Traject traject : trajecten) {

				if (traject instanceof Route || traject instanceof NetwerkLus) {

					if (periode.equals(PERIODE_LENTE.toString())
							&& traject.getPeterMeter1() != null) {
						if (!isExistingCO(traject, PERIODE_LENTE.toString(),
								jaar)) {
							ControleOpdracht opdrachtlente = buildControleOpdracht(
									traject, PERIODE_LENTE, jaar);
							modelRepository.saveObject(opdrachtlente);
							counter++;
						}

					} else if (periode.equals(PERIODE_ZOMER.toString())
							&& traject.getPeterMeter2() != null) {
						if (!isExistingCO(traject, PERIODE_ZOMER.toString(),
								jaar)) {
							ControleOpdracht opdrachtZomer = buildControleOpdracht(
									traject, PERIODE_ZOMER, jaar);
							modelRepository.saveObject(opdrachtZomer);
							counter++;
						}
					} else if (periode.equals(PERIODE_HERFST.toString())
							&& traject.getPeterMeter3() != null) {
						if (!isExistingCO(traject, PERIODE_HERFST.toString(),
								jaar)) {
							ControleOpdracht opdrachtHerfst = buildControleOpdracht(
									traject, PERIODE_HERFST, jaar);
							modelRepository.saveObject(opdrachtHerfst);
							counter++;
						}
					}
				}
			}
			search();
			messages.info("Automatisch aanmaken van controleopdrachten succesvol uitgevoerd.");
			messages.info("Er zijn " + counter
					+ " nieuwe controleopdrachten aangemaakt.");

		} catch (IOException e) {
			messages.info("Automatisch aanmaken controleopdrachten niet gelukt: "
					+ e.getMessage());
			LOG.error("Can not search Trajecten.", e);
		}
	}

	/**
	 * Opbouwen ControleOpdracht aan de hand van een Traject periode en jaar.
	 * 
	 * @param traject
	 * @param periode
	 * @param jaar
	 * @return
	 */
	public ControleOpdracht buildControleOpdracht(Traject traject,
			String periode, String jaar) {

		try {
			ControleOpdracht opdracht = null;

			// Aanmaken type controleOpdracht aan de hand van het type Traject
			if (traject instanceof Route) {
				opdracht = (RouteControleOpdracht) modelRepository
						.createObject("RouteControleOpdracht", null);
			} else if (traject instanceof NetwerkLus) {
				opdracht = (NetwerkControleOpdracht) modelRepository
						.createObject("NetwerkControleOpdracht", null);
			}
			// Set properties
			opdracht.setDatumTeControleren(new Date());
			opdracht.setDatumLaatsteWijziging(new Date());
			opdracht.setTrajectType(Beans.getReference(
					OsyrisModelFunctions.class).getTrajectType(
					modelRepository.getResourceIdentifier(traject)));
			opdracht.setPeriode(periode);
			opdracht.setMedewerker(Beans.getReference(
					OsyrisModelFunctions.class).zoekVerantwoordelijke(
					modelRepository.getResourceIdentifier(traject)));
			opdracht.setJaar(jaar);

			if (periode.equals(PERIODE_LENTE)) {
				opdracht.setPeterMeter(traject.getPeterMeter1());
			} else if (periode.equals(PERIODE_ZOMER)) {
				opdracht.setPeterMeter(traject.getPeterMeter2());
			} else if (periode.equals(PERIODE_HERFST)) {
				opdracht.setPeterMeter(traject.getPeterMeter3());
			}

			opdracht.setRegioId(traject.getRegio());
			opdracht.setStatus(ControleOpdrachtStatus.TE_CONTROLEREN);
			opdracht.setTraject(modelRepository.getResourceIdentifier(traject));

			return opdracht;

		} catch (InstantiationException e) {
			LOG.error("Can not instantiate ControleOpdracht.", e);
		} catch (IllegalAccessException e) {
			LOG.error("Illegal access at object.", e);
		}
		return null;
	}

	/**
	 * Printen van een PDF bestand met een overzichtskaart van het te
	 * controleren traject.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Content printOverviewMap() throws Exception {

		FileOutputStream out = null;
		File file = null;
		InputStream in = null;

		try {
			// Opbouwen XML
			List<Bord> borden = createBewegwijzering(object.getTraject());
			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			XmlBuilder xmlBuilder = new XmlBuilder();
			Document doc = xmlBuilder.buildOverviewMap(bordLayer, getViewer(),
					traject, object, borden);

			Random randomGenerator = new Random();
			Integer randomInt = randomGenerator.nextInt(1000000000);
			String fileName = "traject" + traject.getId().toString() + "_"
					+ randomInt.toString() + ".pdf";

			String location = DefaultConfiguration.instance().getString(
					"osyris.location.temp.pdf.co.kaart");

			file = new File(location + fileName);
			out = new FileOutputStream(file);

			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF,
					FopFactory.newInstance().newFOUserAgent(), out);

			// xslt
			Source xslt = new StreamSource(getClass().getResourceAsStream(
					OVERVIEW_MAP));
			in = getClass().getResourceAsStream(OVERVIEW_MAP);

			// Transform source
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(xslt);
			Result res = new SAXResult(fop.getDefaultHandler());

			transformer.transform(new DOMSource(doc), res);

			String contentStr = IOUtils.toString(in, "UTF-8");

			// File aanmaken indien niet bestaand
			if (!file.exists()) {
				file.createNewFile();
			}
			out.write(contentStr.getBytes());
			contentStr = null;

			return new FileResource(file);
			// return new ByteArrayContent("application/pdf",
			// out.toByteArray());
		} finally {
			// in.close();
			// out.close();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Printen van een PDF bestand met overzichtskaart en bewegwijzergsverslag
	 * in tabelvorm met betrekking tot het Traject uit de gekozen
	 * ControleOpdracht.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Content printControleOpdracht() throws Exception {

		FileOutputStream out = null;
		File file = null;
		InputStream in = null;

		try {
			// Opbouwen XML
			List<Bord> borden = createBewegwijzering(object.getTraject());
			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			XmlBuilder xmlBuilder = new XmlBuilder();
			Document doc = xmlBuilder.buildBewegwijzeringTabel(getViewer(),
					traject, object, borden);

			Random randomGenerator = new Random();
			Integer randomInt = randomGenerator.nextInt(1000000000);
			String fileName = "traject" + traject.getId().toString() + "_"
					+ randomInt.toString() + ".pdf";

			String location = DefaultConfiguration.instance().getString(
					"osyris.location.temp.pdf.co.verslag");

			file = new File(location + fileName);
			out = new FileOutputStream(file);

			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF,
					FopFactory.newInstance().newFOUserAgent(), out);

			// xslt
			Source xslt = new StreamSource(getClass().getResourceAsStream(
					CO_PDF));
			in = getClass().getResourceAsStream(CO_PDF);

			// Transform source
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(xslt);
			Result res = new SAXResult(fop.getDefaultHandler());

			transformer.transform(new DOMSource(doc), res);

			String contentStr = IOUtils.toString(in, "UTF-8");

			// File aanmaken indien niet bestaand
			if (!file.exists()) {
				file.createNewFile();
			}
			out.write(contentStr.getBytes());
			contentStr = null;

			return new FileResource(file);
			// return new ByteArrayContent("application/pdf",
			// out.toByteArray());
		} finally {
			// in.close();
			// out.close();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Printen van een PDF bestand met de individuele bordfiches uit het
	 * bewegwijzeringsverslag met betrekking tot het Traject in de gekozen
	 * ControleOpdracht.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Content printControleOpdrachtBordFiches() throws Exception {

		FileOutputStream out = null;
		File file = null;
		InputStream in = null;

		try {
			// Opbouwen XML
			List<Bord> borden = createBewegwijzering(object.getTraject());
			XmlBuilder xmlBuilder = new XmlBuilder();

			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());
			Document doc = xmlBuilder.buildBordFiches(traject, object, borden,
					getViewer());

			Random randomGenerator = new Random();
			Integer randomInt = randomGenerator.nextInt(1000000000);
			String fileName = "traject" + traject.getId().toString() + "_"
					+ randomInt.toString() + ".pdf";

			String location = DefaultConfiguration.instance().getString(
					"osyris.location.temp.pdf.co.fiche");

			file = new File(location + fileName);
			out = new FileOutputStream(file);

			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF,
					FopFactory.newInstance().newFOUserAgent(), out);

			// xslt
			Source xslt = new StreamSource(getClass().getResourceAsStream(
					BORDFICHE_PDF));
			in = getClass().getResourceAsStream(BORDFICHE_PDF);

			// Transform source
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(xslt);
			Result res = new SAXResult(fop.getDefaultHandler());

			transformer.transform(new DOMSource(doc), res);

			String contentStr = IOUtils.toString(in, "UTF-8");

			// File aanmaken indien niet bestaand
			if (!file.exists()) {
				file.createNewFile();
			}
			out.write(contentStr.getBytes());
			contentStr = null;

			return new FileResource(file);
			// return new ByteArrayContent("application/pdf",
			// out.toByteArray());
		} finally {
			// in.close();
			// out.close();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Checken of het opgegeven Probleem een BordProbleem is.
	 * 
	 * @param probleem
	 * @return
	 */
	public boolean isBordProbleem(Probleem probleem) {

		if (probleem != null) {
			if (probleem instanceof BordProbleem) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Checken of het opgegeven Bord een RouteBord is.
	 * 
	 * @param bord
	 * @return
	 */
	public boolean isRouteBord(Bord bord) {

		if (bord instanceof RouteBord) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Verwijderen van een Probleem.
	 * 
	 * @param probleem
	 */
	public void deleteProbleem(Probleem probleem) {

		try {
			object.getProblemen().remove(probleem);
			modelRepository.saveObject(object);
			modelRepository.deleteObject(probleem);

			// Updaten kaart
			MapViewer viewer = getViewer();
			MapContext context = viewer.getConfiguration().getContext();

			GeometryListFeatureMapLayer pointLayer = (GeometryListFeatureMapLayer) context
					.getLayer(GEOMETRY_LAYER_NAME);

			GeometryListFeatureMapLayer lineLayer = (GeometryListFeatureMapLayer) context
					.getLayer(GEOMETRY_LAYER_LINE_NAME);

			// Updaten bordselectie
			if (probleem instanceof BordProbleem) {

				// ArrayList<String> bordSelection = (ArrayList<String>)
				// bordLayer
				// .getSelection();
				bordSelection = bordLayer.getSelection();
				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) probleem).getBord());

				bordSelection.remove(bord.getId().toString());
				bordLayer.setSelection(bordSelection);
			}

			// Verwijderen punt of lijn van kaart
			if (probleem instanceof AnderProbleem) {

				if (((AnderProbleem) probleem).getGeom() instanceof Point) {
					pointLayer.getGeometries().remove(
							((AnderProbleem) probleem).getGeom());
				}

				else if (((AnderProbleem) probleem).getGeomOmleiding() instanceof LineString) {
					lineLayer.getGeometries().remove(
							((AnderProbleem) probleem).getGeomOmleiding());
				}
			}

			viewer.updateContext(null);
			messages.info("Probleem succesvol verwijderd.");

		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van probleem: "
					+ e.getMessage());
			LOG.error("Can not delete Probleem from ControleOpdracht.", e);
		}
	}

	@Override
	public void delete() {

		try {

			modelRepository.deleteObject(object);
			messages.info("Controleopdracht succesvol verwijderd.");
			// clear();
			search();

		} catch (IOException e) {
			messages.error("Fout bij het verwijderen van de controleopdracht: "
					+ e.getMessage());
			LOG.error("Can not delete model object.", e);
		}
	}

	/**
	 * Checken of de nodige Probleeminfo is aangeduid op de kaart.
	 * 
	 * @param probleem
	 */
	public boolean checkProbleem(Probleem probleem) {

		// Indien BordProbleem check of bord geselecteerd is
		if (probleem instanceof BordProbleem) {
			BordProbleem b = (BordProbleem) probleem;

			if (b.getType().equals(null) || b.getType().isEmpty()) {
				messages.warn("Probleem niet toegevoegd: gelieve een type op te geven.");
				return false;
			}
			if (b.getBord() == null) {
				messages.warn("Probleem niet toegevoegd: gelieve eerst een bord op de kaart te selecteren.");
				return false;
			}
		}

		// Indien AnderProbleem check of probleempunt is aangeduid
		if (probleem instanceof AnderProbleem) {
			AnderProbleem a = (AnderProbleem) probleem;

			if (a.getCategorie().equals(null) || a.getCategorie().isEmpty()) {
				messages.warn("Probleem niet toegevoegd: gelieve een categorie op te geven.");
				return false;
			}
			if (a.getGeom() == null) {
				messages.warn("Probleem niet toegevoegd: gelieve eerst een probleem(punt) aan te duiden op de kaart.");
				return false;
			}
		}
		return true;
	}

	/**
	 * Reset probleem en probleemType
	 * 
	 */
	public void resetProbleem() {
		probleem = null;
		probleemType = null;
	}

	/**
	 * Reset alle lagen in de mapConfiguratie en toon enkel de provinciegrens.
	 * 
	 * @param context
	 */
	private void resetLayers(MapContext context) {

		for (FeatureMapLayer layer : context.getFeatureLayers()) {
			layer.setFilter(null);
			layer.setHidden(true);
			layer.setSelection(Collections.EMPTY_LIST);
			layer.set("selectable", false);

			// Provincie altijd zichtbaar
			if (layer.getLayerId().equalsIgnoreCase("provincie")) {
				layer.setHidden(false);
			}
		}

		// Ortho TMS als default achtergrondlaag
		for (RasterMapLayer baseLayer : context.getBaseRasterLayers()) {

			baseLayer.setHidden(true);

			if (baseLayer.getLayerId().equalsIgnoreCase("tms")) {
				baseLayer.setHidden(false);
				baseLayerName = baseLayer.getLayerId();
			}
		}
	}

	/**
	 * Test switch naar PointGeomLayer bij intekenen punt
	 */
	public void switchToPointGeomLayer() {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		// Lijn laag
		GeometryListFeatureMapLayer lineLayer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_LINE_NAME);
		// lineLayer.setEditable(false);
		lineLayer.setSelectable(false);
		viewer.getContext().setShowDrawLineStringControl(false);

		// Punten laag
		GeometryListFeatureMapLayer pointLayer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_NAME);
		// pointLayer.setEditable(true);
		// pointLayer.setGeometries(new ArrayList<Geometry>(1));
		pointLayer.setSelectable(false);
		viewer.setEditLayerId(GEOMETRY_LAYER_NAME);
		viewer.getContext().setShowDrawPointControl(true);

		viewer.updateContext(null);
	}

	/**
	 * Test switch naar lineGeomLayer bij intekenen omleiding
	 * 
	 */
	public void switchToLineGeomLayer() {

		MapViewer viewer = getViewer();
		MapContext context = viewer.getConfiguration().getContext();

		// Punten laag
		GeometryListFeatureMapLayer pointLayer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_NAME);
		// pointLayer.setEditable(false);
		pointLayer.setSelectable(false);
		viewer.getContext().setShowDrawPointControl(false);

		// Lijn laag
		GeometryListFeatureMapLayer lineLayer = (GeometryListFeatureMapLayer) context
				.getLayer(GEOMETRY_LAYER_LINE_NAME);
		// lineLayer.setGeometries(new ArrayList<Geometry>(1));
		// lineLayer.setEditable(true);
		lineLayer.setSelectable(false);
		viewer.setEditLayerId(GEOMETRY_LAYER_LINE_NAME);
		viewer.getContext().setShowDrawLineStringControl(true);

		viewer.updateContext(null);
	}

	/**
	 * Checken of BordId een RouteBord is.
	 * 
	 * @param bord
	 * @return
	 */
	public boolean isRouteBord(ResourceIdentifier bord) {

		try {
			Bord b = (Bord) modelRepository.loadObject(bord);
			if (b instanceof RouteBord) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			LOG.error("Can not load Bord.", e);
		}

		return false;
	}

	/**
	 * Opzetten van de lagen bij het visualiseren van de mini kaart.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 * 
	 */
	private void setupLayers(MapConfiguration configuration,
			MapContext context, boolean reset) throws InstantiationException,
			IllegalAccessException, IOException {

		bordSelection = new ArrayList<String>();
		anderProbleemPointGeoms = new ArrayList<Geometry>();
		anderProbleemLineGeoms = new ArrayList<Geometry>();

		GeometryListFeatureMapLayer geomLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_NAME, null, Point.class, null, true,
						"single", null, null);

		GeometryListFeatureMapLayer geomLineLayer = (GeometryListFeatureMapLayer) mapFactory
				.createGeometryLayer(configuration.getContext(),
						GEOMETRY_LAYER_LINE_NAME, null, LineString.class, null,
						true, "single", null, null);

		// Reset the layers to default view if specified
		if (reset) {
			// Get traject
			Traject traject = (Traject) modelRepository.loadObject(object
					.getTraject());

			// Laden laag op basis van trajectType
			FeatureMapLayer trajectLayer = (FeatureMapLayer) context
					.getLayer(LabelUtils.lowerCamelCase(traject.getModelClass()
							.getName()));

			if (trajectLayer != null) {
				trajectLayer.setHidden(false);
				trajectLayer.setFilter(FilterUtils.equal("naam",
						traject.getNaam()));

				Envelope envelope = GeometryUtils
						.getEnvelope(traject.getGeom());
				GeometryUtils.expandEnvelope(envelope, 0.1,
						context.getMaxBoundingBox());
				context.setBoundingBox(envelope);
			}

			// BordLayer
			bordLayer = null;
			// ROUTES
			if (traject instanceof Route) {
				bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
						.lowerCamelCase(LabelUtils.lowerCamelCase(traject
								.getModelClass().getName() + "Bord")));

				// Filteren Routeborden op BordNaam
				if (bordLayer != null) {
					bordLayer.setFilter(FilterUtils.equal("naam",
							traject.getNaam()));
					bordLayer.set("selectable", true);
					bordLayer.setHidden(false);
				}

			}
			// LUSSEN
			else if (traject instanceof NetwerkLus) {
				bordLayer = (FeatureMapLayer) context.getLayer(LabelUtils
						.lowerCamelCase(LabelUtils.lowerCamelCase(traject
								.getModelClass().getName()
								.replace("Lus", "Bord"))));

				FeatureMapLayer knooppuntLayer = (FeatureMapLayer) context
						.getLayer(LabelUtils.lowerCamelCase(LabelUtils
								.lowerCamelCase(traject.getModelClass()
										.getName().replace("Lus", "Knooppunt"))));

				// Filteren NetwerkBorden op segmenten van de Lus
				if (bordLayer != null) {

					bordLayer.setFilter(FilterUtils.in("segmenten",
							((NetwerkLus) traject).getSegmenten()));

					List<String> bordIds = new ArrayList<String>();

					for (Bord b : createBewegwijzering(object.getTraject())) {

						bordIds.add(b.getId().toString());
					}

					bordLayer.setFilter(FilterUtils.in("id", bordIds));

					bordLayer.setHidden(false);
					bordLayer.set("selectable", true);
				}

				if (knooppuntLayer != null) {
					searchKnooppuntLayer(knooppuntLayer);
				}
			}
		}

		// Problemen
		for (Probleem probleem : object.getProblemen()) {
			if (probleem instanceof BordProbleem) {
				// Bord Probleem
				Bord bord = (Bord) modelRepository
						.loadObject(((BordProbleem) probleem).getBord());

				if (bord != null) {
					bordSelection.add(bord.getId().toString());
				}

			} else if (probleem instanceof AnderProbleem) {
				// Ander Probleem
				AnderProbleem anderProbleem = (AnderProbleem) probleem;

				if (anderProbleem.getGeom() instanceof Point) {
					anderProbleemPointGeoms.add(anderProbleem.getGeom());
				}

				if (anderProbleem.getGeomOmleiding() instanceof LineString) {
					anderProbleemLineGeoms
							.add(anderProbleem.getGeomOmleiding());
				}
			}
		}

		bordLayer.setSelection(bordSelection);
		geomLayer.setGeometries(anderProbleemPointGeoms);
		geomLineLayer.setGeometries(anderProbleemLineGeoms);

		if (!anderProbleemPointGeoms.isEmpty()) {
			geomLayer.setHidden(false);
		}

		if (!anderProbleemLineGeoms.isEmpty()) {
			geomLineLayer.setHidden(false);
		}
	}

	/**
	 * Switchen tussen basislagen
	 * 
	 */
	public void switchBaseLayers() {
		getViewer().setBaseLayerId(baseLayerName);
	}

	/**
	 * Zoeken of een ControleOpdracht voor een bepaald traject en periode reeds
	 * bestaat.
	 * 
	 * @param traject
	 * @param periode
	 * @return
	 * @throws IOException
	 */
	public boolean isExistingCO(Traject traject, String periode, String jaar)
			throws IOException {

		DefaultQuery query = new DefaultQuery("ControleOpdracht");
		query.addFilter(FilterUtils.and(
				FilterUtils.equal("traject",
						modelRepository.getResourceIdentifier(traject)),
				FilterUtils.equal("periode", periode),
				FilterUtils.equal("jaar", jaar)));

		List<Traject> existingLenteCO = (List<Traject>) modelRepository
				.searchObjects(query, false, false);

		if (existingLenteCO.isEmpty()) {
			return false;
		}
		return true;
	}

	public void validerenControleOpdracht() {

		try {
			if (object != null) {

				// Indien alle problemen in een ControleOpdracht een status
				// hebben is die niet IN BEHANDELING is, mag de ControleOpdracht
				// gevalideerd worden en mogen de WO
				// aangemaakt worden in batch
				int aantalOpenProblemen = checkOpenstaandeProblemen(object);

				if (aantalOpenProblemen != 0) {
					messages.warn("Opgelet: Sommige problemen zijn nog in behandeling of hebben nog geen status. Om deze controleopdracht te kunnen valideren, moeten alle problemen een status hebben en niet meer in behandeling zijn.");
				}
				if (aantalOpenProblemen == 0) {

					object.setStatus(ControleOpdrachtStatus.GEVALIDEERD);
					object.setDatumGevalideerd(new Date());
					object.setDatumLaatsteWijziging(new Date());

					// WerkOpdrachten aanmaken
					createWerkOpdrachten(object);

					modelRepository.saveObject(object);
					messages.info("Controleopdracht succesvol gevalideerd.");

				}
			}
		} catch (IOException e) {
			LOG.error("Can not validate ControleOpdracht.", e);
		}
	}

	/**
	 * Check of problemen bij een controleOpdracht een status hebben en niet in
	 * status IN_BEHANDELING zijn.
	 * 
	 * @param controleOpdracht
	 * @return
	 */
	private int checkOpenstaandeProblemen(ControleOpdracht controleOpdracht) {

		int openstaandeProblemen = 0;

		if (controleOpdracht.getProblemen() != null) {
			for (Probleem p : controleOpdracht.getProblemen()) {
				if (p.getStatus() == null || p.getStatus().toString().isEmpty()
						|| p.getStatus().equals(ProbleemStatus.IN_BEHANDELING)) {
					openstaandeProblemen = +1;
				}
			}
		}
		return openstaandeProblemen;
	}

	/**
	 * Aanmaken van werkopdrachten bij bepaalde problemen in een
	 * controleOpdracht
	 * 
	 * @param controleOpdracht
	 */
	private void createWerkOpdrachten(ControleOpdracht controleOpdracht) {

		if (controleOpdracht.getProblemen() != null) {

			int counter = 0;
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
						werkOpdracht
								.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
						werkOpdracht.setMedewerker(controleOpdracht
								.getMedewerker());
						werkOpdracht.setProbleem(probleem);
						werkOpdracht.setTraject(controleOpdracht.getTraject());
						werkOpdracht.setTrajectType(Beans.getReference(
								OsyrisModelFunctions.class).getTrajectType(
								werkOpdracht.getTraject()));
						werkOpdracht.setRegioId(Beans.getReference(
								OsyrisModelFunctions.class).getTrajectRegioId(
								werkOpdracht.getTraject()));
						werkOpdracht.setDatumLaatsteWijziging(new Date());
						werkOpdracht.setGemeente(Beans.getReference(
								OsyrisModelFunctions.class)
								.getWerkOpdrachtGemeente(probleem));

						// Voor routes uitvoerder zoeken via RegioID van de
						// route
						Traject traject = (Traject) modelRepository
								.loadObject(controleOpdracht.getTraject());
						if (traject instanceof Route) {
							werkOpdracht.setUitvoerder(Beans.getReference(
									OsyrisModelFunctions.class).zoekUitvoerder(
									traject.getRegio()));
						}

						// Voor netwerk uitvoerder zoeken via RegioID van het
						// NetwerkBord indien bordprobleem
						if (traject instanceof NetwerkLus
								&& probleem instanceof NetwerkBordProbleem) {
							Bord b = (Bord) modelRepository
									.loadObject(((NetwerkBordProbleem) probleem)
											.getBord());
							werkOpdracht.setUitvoerder(Beans.getReference(
									OsyrisModelFunctions.class).zoekUitvoerder(
									b.getRegio()));
						}

						// Voor netwerk uitvoerder zoeken via geom anderprobleem
						// en
						// zoeken naar intersect met een bepaalde regio?
						if (traject instanceof NetwerkLus
								&& probleem instanceof NetwerkAnderProbleem) {
							werkOpdracht
									.setUitvoerder(Beans
											.getReference(
													OsyrisModelFunctions.class)
											.zoekUitvoerder(
													Beans.getReference(
															OsyrisModelFunctions.class)
															.searchRegioForProbleem(
																	((NetwerkAnderProbleem) probleem)
																			.getGeom())));

						}
						modelRepository.saveObject(werkOpdracht);
						counter += 1;

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
			messages.info(counter + " werkopdracht(en) succesvol aangemaakt.");
		}
	}

	public void setupEditLayers() {
		if (editType.equals("drawPoint")) {
			switchToPointGeomLayer();

		} else if (editType.equals("drawLineString")) {
			switchToLineGeomLayer();
		}
	}

	/**
	 * Checken of een ControleOpdracht beschikt over een periode en een
	 * peter/meter.
	 * 
	 * @param controleOpdracht
	 * @return
	 */
	public boolean checkControleOpdracht(ControleOpdracht controleOpdracht) {
		if (controleOpdracht.getPeriode() == null
				|| controleOpdracht.getPeriode().isEmpty()) {
			messages.warn("Gelieve een periode voor deze controleopdracht te selecteren.");
			return false;
		} else if (controleOpdracht.getPeterMeter() == null) {
			messages.warn("Gelieve een peter/meter voor deze controleopdracht te selecteren.");
			return false;
		}
		return true;
	}

	/**
	 * Koppelen van de Route of Lus aan de nieuwe ControleOpdracht.
	 * 
	 */
	public void koppelTraject() {
		// Koppelen traject aan ControleOpdracht
		if (trajectNaamCreate != null) {
			try {
				DefaultQuery query = new DefaultQuery("Traject");
				query.addFilter(FilterUtils.equal("naam", trajectNaamCreate));
				List<Traject> trajecten = (List<Traject>) modelRepository
						.searchObjects(query, false, false);

				if (trajecten.size() == 1) {
					Traject selectedTraject = trajecten.get(0);

					// Indien precies 1 traject gevonden, koppelen aan de
					// ControleOpdracht
					if (selectedTraject != null) {

						object.setTraject(modelRepository
								.getResourceIdentifier(selectedTraject));
					}
				}
			} catch (IOException e) {
				LOG.error("Can not search Trajecten.");
			}
		}
	}

	/**
	 * Custom ControleOpdracht CSV export
	 * 
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Content<?> report() {

		File file = null;

		DefaultModelObjectList objectList = new DefaultModelObjectList<ControleOpdracht>(
				getModelClass(), getResults());

		// Write CSV to disk
		Content content = new EncodableContent<ModelObjectList>(
				(Encoder) new ControleOpdrachtCSVModelEncoder(), objectList);

		Random randomGenerator = new Random();
		Integer randomInt = randomGenerator.nextInt(1000000000);
		String fileName = "ControleOpdrachtOverzicht_" + randomInt.toString()
				+ ".csv";

		String location = DefaultConfiguration.instance().getString(
				"osyris.location.temp.csv");

		file = new File(location + fileName);

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			content.encode(file);
		} catch (IOException e) {
			LOG.error("Can not encode CSV file.");
		} catch (Exception e) {
			LOG.error("Can not create new CSV file.");
		}
		return new FileResource(file);

		// return new EncodableContent<ModelObjectList>(
		// (Encoder) new ControleOpdrachtCSVModelEncoder(), objectList);
	}

	/**
	 * 
	 */
	public void saveAllLussen() {

		try {

			DefaultQuery query = new DefaultQuery("NetwerkLus");
			List<NetwerkLus> lussen = (List<NetwerkLus>) modelRepository
					.searchObjects(query, false, false);

			for (NetwerkLus lus : lussen) {
				modelRepository.saveObject(lus);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean checkDebugMode() {
		String mode = DefaultConfiguration.instance().getString(
				"core.mode.debug");
		if (mode.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	/**
	 * Reset zoekvelden trajectId bij het wijzigen van het trajectType
	 * 
	 */
	public void resetSearchParameters() {
		trajectId = null;
	}
}
