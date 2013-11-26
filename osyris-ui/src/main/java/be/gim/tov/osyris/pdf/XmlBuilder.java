package be.gim.tov.osyris.pdf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.conscientia.core.configuration.DefaultConfiguration;
import org.conscientia.service.interchange.InterchangeStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.impl.raster.RasterEncoderFactory;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.peritia.codec.EncodableContent;
import be.gim.specto.api.context.FeatureMapLayer;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.AnderProbleem;
import be.gim.tov.osyris.model.controle.BordProbleem;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.traject.AutoRoute;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.FietsNetwerkBord;
import be.gim.tov.osyris.model.traject.FietsRoute;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.RuiterRoute;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.traject.WandelNetwerkBord;
import be.gim.tov.osyris.model.traject.WandelRoute;
import be.gim.tov.osyris.model.werk.WerkHandeling;
import be.gim.tov.osyris.model.werk.WerkOpdracht;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author kristof
 * 
 */
public class XmlBuilder {

	public static final int DPI_FACTOR = 2;
	public static final double DPI = 72 * DPI_FACTOR;

	private static double SCALE_ORTHO = 1889.7648;
	private static double SCALE_TOPO = 7231;

	private static String PIJL_LINKS = "1";
	private static String PIJL_RECHTS = "2";
	private static String PIJL_RECHTDOOR = "3";

	private static String POSITIE_PIJL_LINKS = "L";
	private static String POSITIE_PIJL_RECHTS = "R";

	private static final Log LOG = LogFactory.getLog(XmlBuilder.class);

	protected Collection<String> imageKeys = new ArrayList<String>();

	/**
	 * Opbouwen van bordfiches in XML. Voor elk Bord wordt een individuele fiche
	 * opgemaakt.
	 * 
	 * @param object
	 * @param borden
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 */
	public Document buildBordFiches(Traject traject, ControleOpdracht object,
			List<Bord> borden, MapViewer viewer)
			throws ParserConfigurationException, TransformerException,
			DOMException, InstantiationException, IllegalAccessException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// VERSLAG
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("verslag");
		doc.appendChild(rootElement);

		// REGIO
		Element regio = doc.createElement("regio");
		regio.appendChild(doc.createTextNode(Beans.getReference(
				OsyrisModelFunctions.class)
				.getTrajectRegio(object.getTraject())));
		rootElement.appendChild(regio);

		// TYPE TRAJECT
		Element trajectType = doc.createElement("trajecttype");
		trajectType.appendChild(doc.createTextNode(object.getTrajectType()));
		rootElement.appendChild(trajectType);

		rootElement.setAttribute("netwerkbord", Boolean.FALSE.toString());

		if (object.getTrajectType().contains("netwerk")) {
			rootElement.setAttribute("netwerkbord", Boolean.TRUE.toString());
		}

		// VOORBEELDFOTO ROUTE
		Element voorbeeldFoto = doc.createElement("voorbeeldFoto");
		if (traject instanceof Route) {

			voorbeeldFoto.appendChild(doc
					.createTextNode(getVoorbeeldRouteBord(traject)));
		}
		rootElement.appendChild(voorbeeldFoto);

		// NAAM TRAJECT
		Element trajectNaam = doc.createElement("trajectnaam");
		trajectNaam
				.appendChild(doc.createTextNode(Beans.getReference(
						OsyrisModelFunctions.class).getTrajectNaam(
						object.getTraject())));
		rootElement.appendChild(trajectNaam);

		for (Bord b : borden) {

			Element bord = doc.createElement("bord");
			rootElement.appendChild(bord);

			// BORD ID
			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode(b.getId().toString()));
			bord.appendChild(id);

			// BORD GEMEENTE
			Element gemeente = doc.createElement("gemeente");
			gemeente.appendChild(doc.createTextNode(b.getGemeente()));
			bord.appendChild(gemeente);

			// BORD STRAAT
			Element straat = doc.createElement("straat");
			straat.appendChild(doc.createTextNode(b.getStraatnaam()));
			bord.appendChild(straat);

			// X COORDINAAT
			Element x = doc.createElement("x");
			x.appendChild(doc.createTextNode(BigDecimal.valueOf(b.getX())
					.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
			bord.appendChild(x);

			// Y COORDINAAT
			Element y = doc.createElement("y");
			y.appendChild(doc.createTextNode(BigDecimal.valueOf(b.getY())
					.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
			bord.appendChild(y);

			// WEGBEVOEGD
			Element wegBevoegd = doc.createElement("wegbevoegd");
			wegBevoegd.appendChild(doc.createTextNode(b.getWegBevoegd()));
			bord.appendChild(wegBevoegd);

			// PIJL ROUTEBORD
			Element pijl = doc.createElement("pijl");
			if (b instanceof RouteBord) {

				RouteBord rb = (RouteBord) b;
				pijl.appendChild(doc.createTextNode(getPijlBord(rb
						.getImageCode())));

			}
			bord.appendChild(pijl);

			if (b instanceof NetwerkBord) {

				NetwerkBord nwb = (NetwerkBord) b;

				// VOORBEELDFOTO
				Element voorbeeldKpBord = doc
						.createElement("voorbeeld_kp_bord");
				voorbeeldKpBord.appendChild(doc
						.createTextNode(getVoorbeeldNetwerkBord(nwb)));
				bord.appendChild(voorbeeldKpBord);

				// KP 0
				Element knooppunt0 = doc.createElement("knooppunt0");
				if (nwb.getKpnr0() != null) {
					knooppunt0.appendChild(doc.createTextNode(nwb.getKpnr0()
							.toString()));
				}
				bord.appendChild(knooppunt0);

				// KP 1
				Element knooppunt1 = doc.createElement("knooppunt1");
				Element knooppunt1Pijl = doc.createElement("kp1_pijl");

				if (nwb.getKpnr1() != null) {
					knooppunt1.appendChild(doc.createTextNode(nwb.getKpnr1()
							.toString()));

					knooppunt1Pijl
							.appendChild(doc.createTextNode(getPijlBord(nwb
									.getKp1ImageCode())));
				}
				bord.appendChild(knooppunt1);
				bord.appendChild(knooppunt1Pijl);

				// KP 2
				Element knooppunt2 = doc.createElement("knooppunt2");
				Element knooppunt2Pijl = doc.createElement("kp2_pijl");

				if (nwb.getKpnr2() != null) {
					knooppunt2.appendChild(doc.createTextNode(nwb.getKpnr2()
							.toString()));

					knooppunt2Pijl
							.appendChild(doc.createTextNode(getPijlBord(nwb
									.getKp2ImageCode())));
				}
				bord.appendChild(knooppunt2);
				bord.appendChild(knooppunt2Pijl);

				// KP 3
				Element knooppunt3 = doc.createElement("knooppunt3");
				Element knooppunt3Pijl = doc.createElement("kp3_pijl");
				if (nwb.getKpnr3() != null) {
					knooppunt3.appendChild(doc.createTextNode(nwb.getKpnr3()
							.toString()));

					knooppunt3Pijl
							.appendChild(doc.createTextNode(getPijlBord(nwb
									.getKp3ImageCode())));
				}
				bord.appendChild(knooppunt3);
				bord.appendChild(knooppunt3Pijl);
			}

			// CONSTRUCTIE TYPE
			Element constructieType = doc.createElement("constructieType");
			if (b.getBordConst() != null) {
				constructieType
						.appendChild(doc.createTextNode(b.getBordConst()));
			}
			bord.appendChild(constructieType);

			// PAALTYPE
			Element paalType = doc.createElement("paaltype");
			if (b.getPaalConst() != null) {
				paalType.appendChild(doc.createTextNode(b.getPaalConst()));
			}
			bord.appendChild(paalType);

			// PAALDIAMETER
			Element paalDiameter = doc.createElement("paaldiameter");
			if (b.getPaalDia() != null) {
				paalDiameter.appendChild(doc.createTextNode(b.getPaalDia()));
			}
			bord.appendChild(paalDiameter);

			// PAALBEUGEL
			Element paalBeugel = doc.createElement("paalbeugel");
			if (b.getPaalBeugel() != null) {
				paalBeugel.appendChild(doc.createTextNode(b.getPaalBeugel()));
			}
			bord.appendChild(paalBeugel);

			// PAALONDERGROND
			Element paalOndergrond = doc.createElement("paalondergrond");
			if (b.getPaalGrond() != null) {
				paalOndergrond
						.appendChild(doc.createTextNode(b.getPaalGrond()));
			}
			bord.appendChild(paalOndergrond);

			// URL BORDFOTO
			Element bordFoto = doc.createElement("bordfoto");
			bordFoto.appendChild(doc.createTextNode(b.getFoto().toString()));
			bord.appendChild(bordFoto);

			// ORTHOKAART
			Element mapOrtho = doc.createElement("mapOrtho");
			mapOrtho.appendChild(doc
					.createTextNode(getMapBord(viewer, b, true)));
			bord.appendChild(mapOrtho);

			// TOPOKAART
			Element mapTopo = doc.createElement("mapTopo");
			mapTopo.appendChild(doc
					.createTextNode(getMapBord(viewer, b, false)));
			bord.appendChild(mapTopo);
		}

		return doc;
	}

	/**
	 * Opbouwen bewegwijzeringtabel in XML
	 * 
	 * @param object
	 * @param borden
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 */
	public Document buildBewegwijzeringTabel(MapViewer viewer, Traject traject,
			ControleOpdracht object, List<Bord> borden)
			throws ParserConfigurationException, TransformerException,
			DOMException, InstantiationException, IllegalAccessException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("verslag");
		doc.appendChild(rootElement);

		Element overviewMap = doc.createElement("overviewMap");
		overviewMap.appendChild(doc.createTextNode(getOverviewMap(viewer,
				traject)));
		rootElement.appendChild(overviewMap);

		Element trajectNaam = doc.createElement("trajectnaam");
		trajectNaam.appendChild(doc.createTextNode(traject.getNaam()));
		rootElement.appendChild(trajectNaam);

		rootElement.setAttribute("netwerkCO", Boolean.FALSE.toString());

		if (object.getTrajectType().contains("netwerk")) {
			rootElement.setAttribute("netwerkCO", Boolean.TRUE.toString());
		}

		for (Bord b : borden) {
			// Bord elementen
			Element bord = doc.createElement("bord");
			rootElement.appendChild(bord);

			Element volgNr = doc.createElement("bordnr");
			if (b.getVolg() != null) {
				volgNr.appendChild(doc.createTextNode(b.getVolg()));
			}
			bord.appendChild(volgNr);

			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode(b.getId().toString()));
			bord.appendChild(id);

			if (b instanceof NetwerkBord) {

				NetwerkBord nwb = (NetwerkBord) b;

				Element bordType = doc.createElement("bordtype");
				bordType.appendChild(doc.createTextNode(nwb.getBordType()));
				bord.appendChild(bordType);

				Element pijlkp1 = doc.createElement("pijlkp1");
				Element pijlkp2 = doc.createElement("pijlkp2");
				Element pijlkp3 = doc.createElement("pijlkp3");

				if (nwb.getKp1ImageCode() != null) {
					pijlkp1.appendChild(doc.createTextNode(getPijlBord(nwb
							.getKp1ImageCode())));
				}
				bord.appendChild(pijlkp1);

				if (nwb.getKp2ImageCode() != null) {
					pijlkp2.appendChild(doc.createTextNode(getPijlBord(nwb
							.getKp2ImageCode())));
				}
				bord.appendChild(pijlkp2);

				if (nwb.getKp3ImageCode() != null) {
					pijlkp3.appendChild(doc.createTextNode(getPijlBord(nwb
							.getKp3ImageCode())));
				}
				bord.appendChild(pijlkp3);
			}

			if (b instanceof RouteBord) {

				RouteBord rb = (RouteBord) b;

				Element pijl = doc.createElement("pijl");
				if (rb.getImageCode() != null) {
					pijl.appendChild(doc.createTextNode(getPijlBord(rb
							.getImageCode())));
				}
				bord.appendChild(pijl);
			}

			Element gemeente = doc.createElement("gemeente");
			gemeente.appendChild(doc.createTextNode(b.getGemeente()));
			bord.appendChild(gemeente);

			Element straat = doc.createElement("straatnaam");
			straat.appendChild(doc.createTextNode(b.getStraatnaam()));
			bord.appendChild(straat);

			Element paalType = doc.createElement("paaltype");
			paalType.appendChild(doc.createTextNode(b.getPaalConst()));
			bord.appendChild(paalType);
		}

		return doc;
	}

	/**
	 * Opbouwen WerkOpdracht fiche met BordProbleem in XML.
	 * 
	 * @param object
	 * @param borden
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 */
	public Document buildWerkOpdrachtFicheBordProbleem(MapViewer viewer,
			Traject traject, WerkOpdracht object, Bord bord)
			throws ParserConfigurationException, TransformerException,
			DOMException, InstantiationException, IllegalAccessException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		int counter = 1;

		// ROOT ELEMENT OPDRACHT
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("opdracht");
		doc.appendChild(rootElement);

		// ELEMENT BORD
		Element bordElement = doc.createElement("bord");
		rootElement.appendChild(bordElement);

		// OPDRACHT ID
		Element id = doc.createElement("id");
		id.appendChild(doc.createTextNode(object.getId().toString()));
		rootElement.appendChild(id);

		// VOORBEELD FOTO
		Element voorbeeldFoto = doc.createElement("voorbeeldFoto");
		if (bord instanceof RouteBord) {

			voorbeeldFoto.appendChild(doc
					.createTextNode(getVoorbeeldRouteBord(traject)));
		}
		rootElement.appendChild(voorbeeldFoto);

		// Attribute checks
		rootElement.setAttribute("segment", Boolean.FALSE.toString());
		rootElement.setAttribute("hasFoto", Boolean.FALSE.toString());
		rootElement.setAttribute("bordProbleem", Boolean.FALSE.toString());
		rootElement.setAttribute("netwerkbord", Boolean.FALSE.toString());

		if (object.getTrajectType().contains("netwerk")) {
			rootElement.setAttribute("netwerkbord", Boolean.TRUE.toString());
		}

		if (traject instanceof NetwerkSegment) {
			rootElement.setAttribute("segment", Boolean.TRUE.toString());
		}

		if (object.getProbleem().getFoto() != null) {
			rootElement.setAttribute("hasFoto", Boolean.TRUE.toString());
		}

		// GEMEENTE OPDRACHT
		Element gemeente = doc.createElement("gemeente");
		gemeente.appendChild(doc.createTextNode(object.getGemeente()));
		rootElement.appendChild(gemeente);

		// NAAM TRAJECT
		if (traject instanceof Route || traject instanceof NetwerkLus) {

			Element trajectNaam = doc.createElement("trajectnaam");
			trajectNaam.appendChild(doc.createTextNode(traject.getNaam()));
			rootElement.appendChild(trajectNaam);
		}

		else if (traject instanceof NetwerkSegment) {

			NetwerkSegment seg = (NetwerkSegment) traject;

			Element vanKp = doc.createElement("vankp");
			vanKp.appendChild(doc.createTextNode(seg.getVanKpNr().toString()));
			rootElement.appendChild(vanKp);

			Element naarKp = doc.createElement("naarkp");
			naarKp.appendChild(doc.createTextNode(seg.getNaarKpNr().toString()));
			rootElement.appendChild(naarKp);
		}

		// TYPE PAAL
		Element paalType = doc.createElement("paaltype");
		if (bord.getPaalConst() != null) {
			paalType.appendChild(doc.createTextNode(bord.getPaalConst()));
		}
		bordElement.appendChild(paalType);

		// DIAMETER PAAL
		Element paalDiameter = doc.createElement("paaldiameter");
		if (bord.getPaalDia() != null) {
			paalDiameter.appendChild(doc.createTextNode(bord.getPaalDia()));
		}
		bordElement.appendChild(paalDiameter);

		// PAALBEUGEL
		Element paalBeugel = doc.createElement("paalbeugel");
		if (bord.getPaalBeugel() != null) {
			paalBeugel.appendChild(doc.createTextNode(bord.getPaalBeugel()));
		}
		bordElement.appendChild(paalBeugel);

		// PAALONDERGROND
		Element paalOndergrond = doc.createElement("paalondergrond");
		if (bord.getPaalGrond() != null) {
			paalOndergrond.appendChild(doc.createTextNode(bord.getPaalGrond()));
		}
		bordElement.appendChild(paalOndergrond);

		// BORD
		if (object.getProbleem() instanceof BordProbleem) {
			rootElement.setAttribute("bordProbleem", Boolean.TRUE.toString());
		}
		// WERKHANDELINGEN
		for (WerkHandeling h : object.getHandelingen()) {

			Element handeling = doc.createElement("handeling");
			rootElement.appendChild(handeling);

			Element nummer = doc.createElement("nummer");
			nummer.appendChild(doc.createTextNode(String.valueOf(counter)));
			handeling.appendChild(nummer);
			counter++;

			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(h.getType()));
			handeling.appendChild(type);
		}

		// COMMENTAAR TOV
		Element commentaar = doc.createElement("commentaar");
		commentaar.appendChild(doc.createTextNode(object
				.getCommentaarMedewerker()));
		rootElement.appendChild(commentaar);

		// FOTO PROBLEEM
		Element probleemFoto = doc.createElement("probleemfoto");
		if (object.getProbleem().getFoto() != null) {
			probleemFoto.appendChild(doc.createTextNode(Base64.encode(object
					.getProbleem().getFoto())));
		} else {
			probleemFoto.appendChild(doc.createTextNode(getUrlGeenFoto()));
		}
		rootElement.appendChild(probleemFoto);

		// REGIO
		Element regio = doc.createElement("regio");
		regio.appendChild(doc.createTextNode(Beans.getReference(
				OsyrisModelFunctions.class)
				.getTrajectRegio(object.getTraject())));
		rootElement.appendChild(regio);

		// TYPE TRAJECT
		Element trajectType = doc.createElement("trajecttype");
		trajectType.appendChild(doc.createTextNode(object.getTrajectType()));
		rootElement.appendChild(trajectType);

		// BORD ID
		Element bordId = doc.createElement("bordId");
		bordId.appendChild(doc.createTextNode(bord.getId().toString()));
		bordElement.appendChild(bordId);

		// GEMEENTE BORD
		Element bordGemeente = doc.createElement("bordGemeente");
		bordGemeente.appendChild(doc.createTextNode(bord.getGemeente()));
		bordElement.appendChild(bordGemeente);

		// STRAAT BORD
		Element bordStraat = doc.createElement("bordStraat");
		bordStraat.appendChild(doc.createTextNode(bord.getStraatnaam()));
		bordElement.appendChild(bordStraat);

		// X
		Element x = doc.createElement("x");
		x.appendChild(doc.createTextNode(BigDecimal.valueOf(bord.getX())
				.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
		bordElement.appendChild(x);

		// Y
		Element y = doc.createElement("y");
		y.appendChild(doc.createTextNode(BigDecimal.valueOf(bord.getY())
				.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
		bordElement.appendChild(y);

		// WEGBEVOEGD
		Element wegBevoegd = doc.createElement("wegbevoegd");
		wegBevoegd.appendChild(doc.createTextNode(bord.getWegBevoegd()));
		bordElement.appendChild(wegBevoegd);

		// PIJL ROUTEBORD
		Element pijl = doc.createElement("pijl");
		if (bord instanceof RouteBord) {

			RouteBord rb = (RouteBord) bord;
			pijl.appendChild(doc.createTextNode(getPijlBord(rb.getImageCode())));
		}
		bordElement.appendChild(pijl);

		if (bord instanceof NetwerkBord) {

			NetwerkBord nwb = (NetwerkBord) bord;

			// KP 0
			Element knooppunt0 = doc.createElement("knooppunt0");

			if (nwb.getKpnr0() != null) {
				knooppunt0.appendChild(doc.createTextNode(nwb.getKpnr0()
						.toString()));
			}
			bordElement.appendChild(knooppunt0);

			// KP 1
			Element knooppunt1 = doc.createElement("knooppunt1");
			Element knooppunt1Pijl = doc.createElement("kp1_pijl");

			if (nwb.getKpnr1() != null) {
				knooppunt1.appendChild(doc.createTextNode(nwb.getKpnr1()
						.toString()));

				knooppunt1Pijl.appendChild(doc.createTextNode(getPijlBord(nwb
						.getKp1ImageCode())));
			}
			bordElement.appendChild(knooppunt1);
			bordElement.appendChild(knooppunt1Pijl);

			// KP 2
			Element knooppunt2 = doc.createElement("knooppunt2");
			Element knooppunt2Pijl = doc.createElement("kp2_pijl");

			if (nwb.getKpnr2() != null) {
				knooppunt2.appendChild(doc.createTextNode(nwb.getKpnr2()
						.toString()));

				knooppunt2Pijl.appendChild(doc.createTextNode(getPijlBord(nwb
						.getKp2ImageCode())));
			}
			bordElement.appendChild(knooppunt2);
			bordElement.appendChild(knooppunt2Pijl);

			// KP 3
			Element knooppunt3 = doc.createElement("knooppunt3");
			Element knooppunt3Pijl = doc.createElement("kp3_pijl");

			if (nwb.getKpnr3() != null) {
				knooppunt3.appendChild(doc.createTextNode(nwb.getKpnr3()
						.toString()));

				knooppunt3Pijl.appendChild(doc.createTextNode(getPijlBord(nwb
						.getKp3ImageCode())));
			}
			bordElement.appendChild(knooppunt3);
			bordElement.appendChild(knooppunt3Pijl);
		}

		// TYPE CONSTRUCTIE
		Element constructieType = doc.createElement("constructieType");
		if (bord.getBordConst() != null) {
			constructieType
					.appendChild(doc.createTextNode(bord.getBordConst()));
		}
		bordElement.appendChild(constructieType);

		// ORTHO KAART
		Element mapOrtho = doc.createElement("mapOrtho");
		mapOrtho.appendChild(doc.createTextNode(getMapBord(viewer, bord, true)));
		bordElement.appendChild(mapOrtho);

		// TOPO KAART
		Element mapTopo = doc.createElement("mapTopo");
		mapTopo.appendChild(doc.createTextNode(getMapBord(viewer, bord, false)));
		bordElement.appendChild(mapTopo);

		return doc;
	}

	/**
	 * Opbouwen WerkOpdracht fiche met AnderProbleem in XML.
	 * 
	 * @param traject
	 * @param object
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws DOMException
	 */
	public Document buildWerkOpdrachtFicheAnderProbleem(MapViewer viewer,
			Traject traject, WerkOpdracht object)
			throws ParserConfigurationException, TransformerException,
			DOMException, InstantiationException, IllegalAccessException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		int counter = 1;

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("opdracht");
		doc.appendChild(rootElement);

		Element id = doc.createElement("id");
		id.appendChild(doc.createTextNode(object.getId().toString()));
		rootElement.appendChild(id);

		rootElement.setAttribute("bordProbleem", Boolean.FALSE.toString());
		rootElement.setAttribute("segment", Boolean.FALSE.toString());
		rootElement.setAttribute("hasFoto", Boolean.FALSE.toString());

		if (traject instanceof NetwerkSegment) {
			rootElement.setAttribute("segment", Boolean.TRUE.toString());
		}

		if (object.getProbleem().getFoto() != null) {
			rootElement.setAttribute("hasFoto", Boolean.TRUE.toString());
		}

		if (traject instanceof Route || traject instanceof NetwerkLus) {

			Element trajectNaam = doc.createElement("trajectnaam");
			trajectNaam.appendChild(doc.createTextNode(traject.getNaam()));
			rootElement.appendChild(trajectNaam);
		}

		else if (traject instanceof NetwerkSegment) {

			NetwerkSegment seg = (NetwerkSegment) traject;

			Element vanKp = doc.createElement("vankp");
			vanKp.appendChild(doc.createTextNode(seg.getVanKpNr().toString()));
			rootElement.appendChild(vanKp);

			Element naarKp = doc.createElement("naarkp");
			naarKp.appendChild(doc.createTextNode(seg.getNaarKpNr().toString()));
			rootElement.appendChild(naarKp);
		}

		for (WerkHandeling h : object.getHandelingen()) {

			Element handeling = doc.createElement("handeling");
			rootElement.appendChild(handeling);

			Element nummer = doc.createElement("nummer");
			nummer.appendChild(doc.createTextNode(String.valueOf(counter)));
			handeling.appendChild(nummer);
			counter++;

			Element type = doc.createElement("type");
			type.appendChild(doc.createTextNode(h.getType()));
			handeling.appendChild(type);
		}

		// COMMENTAAR TOV
		Element commentaar = doc.createElement("commentaar");
		if (object.getCommentaarMedewerker() != null) {
			commentaar.appendChild(doc.createTextNode(object
					.getCommentaarMedewerker()));
		}
		rootElement.appendChild(commentaar);

		// REGIO
		Element regio = doc.createElement("regio");
		regio.appendChild(doc.createTextNode(Beans.getReference(
				OsyrisModelFunctions.class)
				.getTrajectRegio(object.getTraject())));
		rootElement.appendChild(regio);

		// GEMEENTE
		Element gemeente = doc.createElement("gemeente");
		gemeente.appendChild(doc.createTextNode(Beans.getReference(
				OsyrisModelFunctions.class).getWerkOpdrachtGemeente(
				object.getProbleem())));
		rootElement.appendChild(gemeente);

		// TYPE TRAJECT
		Element trajectType = doc.createElement("trajecttype");
		trajectType.appendChild(doc.createTextNode(object.getTrajectType()));
		rootElement.appendChild(trajectType);

		// FOTO PROBLEEM
		Element foto = doc.createElement("probleemfoto");
		foto.appendChild(doc.createTextNode(Base64.encode(object.getProbleem()
				.getFoto())));
		rootElement.appendChild(foto);

		// ORTHO KAART
		Element mapOrthoAnderProbleem = doc
				.createElement("mapOrthoAnderProbleem");
		mapOrthoAnderProbleem.appendChild(doc
				.createTextNode(getMapAnderProbleem(viewer,
						object.getProbleem(), true)));
		rootElement.appendChild(mapOrthoAnderProbleem);

		// TOPO KAART
		Element mapTopoAnderProbleem = doc
				.createElement("mapTopoAnderProbleem");
		mapTopoAnderProbleem.appendChild(doc
				.createTextNode(getMapAnderProbleem(viewer,
						object.getProbleem(), false)));
		rootElement.appendChild(mapTopoAnderProbleem);

		return doc;
	}

	/**
	 * Aanmaken afbeelding en teruggeven url van de kaartafbeelding ingezoomd op
	 * het Bord voor een opdracht met een BordProbleem.
	 * 
	 * @param viewer
	 * @param bord
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private String getMapBord(MapViewer viewer, Bord bord,
			boolean hasOrthoBasemap) throws InstantiationException,
			IllegalAccessException {

		double scale = SCALE_ORTHO;

		if (!hasOrthoBasemap) {
			viewer.setBaseLayerId("topo");
			scale = SCALE_TOPO;
		}

		// Selecteer bord
		List<String> ids = new ArrayList<String>(1);
		ids.add(bord.getId().toString());

		FeatureMapLayer layer = (FeatureMapLayer) viewer.getContext().getLayer(
				LabelUtils.lowerCamelCase(bord.getModelClass().getName()));

		viewer.setSelection(layer, ids);

		Coordinate centerPoint = bord.getGeom().getCoordinate();
		int width = 450;
		int height = 300;

		double worldWidth = GeometryUtils.pixelsToWorld(width, scale, DPI);
		double worldHeight = GeometryUtils.pixelsToWorld(height, scale, DPI);

		CoordinateReferenceSystem crs = viewer.getContext().getCrs();

		Envelope boundingBox = new ReferencedEnvelope(centerPoint.x
				- worldWidth / 2, centerPoint.x + worldWidth / 2, centerPoint.y
				- worldHeight / 2, centerPoint.y + worldHeight / 2, crs);

		RenderedImage mapImage = viewer.getMap(
				viewer.getContext().getSrsName(), boundingBox, null, width,
				height, scale);

		// Terugzetten naar tms baseLayer
		viewer.unselectFeatures(layer, ids);
		viewer.setBaseLayerId("tms");

		return storeImage(mapImage);
	}

	/**
	 * Aanmaken afbeelding en teruggeven url van de kaartafbeelding ingezoomd op
	 * de aangeduide locatie van het Probleem voor een opdracht met een
	 * AnderProbleem.
	 * 
	 * @param viewer
	 * @param probleem
	 * @param hasOrthoBasemap
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private String getMapAnderProbleem(MapViewer viewer, Probleem probleem,
			boolean hasOrthoBasemap) throws InstantiationException,
			IllegalAccessException {

		double scale = SCALE_ORTHO;

		if (!hasOrthoBasemap) {
			viewer.setBaseLayerId("navstreet");
			scale = SCALE_TOPO;
		}

		AnderProbleem anderProbleem = null;

		if (probleem instanceof AnderProbleem) {

			anderProbleem = (AnderProbleem) probleem;
		}

		Coordinate centerPoint = anderProbleem.getGeom().getCoordinate();
		int width = 450;
		int height = 300;

		double worldWidth = GeometryUtils.pixelsToWorld(width, scale, DPI);
		double worldHeight = GeometryUtils.pixelsToWorld(height, scale, DPI);

		CoordinateReferenceSystem crs = viewer.getContext().getCrs();

		Envelope boundingBox = new ReferencedEnvelope(centerPoint.x
				- worldWidth / 2, centerPoint.x + worldWidth / 2, centerPoint.y
				- worldHeight / 2, centerPoint.y + worldHeight / 2, crs);

		RenderedImage mapImage = viewer.getMap(
				viewer.getContext().getSrsName(), boundingBox, null, width,
				height, scale);

		// Terugzetten naar tms baseLayer
		viewer.setBaseLayerId("tms");

		return storeImage(mapImage);
	}

	/**
	 * Ophalen van de overzichtskaart bij het bewegwijzeringsverslag.
	 * 
	 * @param viewer
	 * @param traject
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private String getOverviewMap(MapViewer viewer, Traject traject)
			throws InstantiationException, IllegalAccessException {

		viewer.setBaseLayerId("navstreet");

		int width = 1024;
		int height = 780;

		FeatureMapLayer layer = (FeatureMapLayer) viewer.getContext().getLayer(
				LabelUtils.lowerCamelCase(traject.getModelClass().getName()));

		RenderedImage mapImage = viewer.getMap(
				viewer.getContext().getSrsName(),
				viewer.getContentExtent(layer), null, width, height,
				viewer.getCurrentScale());

		Graphics2D g2d = ((BufferedImage) mapImage).createGraphics();

		addNorthArrow(viewer, g2d, width, height);
		// addScaleLine(viewer, g2d, width, height, viewer.getCurrentScale(),
		// DPI);

		viewer.setBaseLayerId("tms");

		return storeImage(mapImage);
	}

	/**
	 * Opslaan en aanbieden van de url van de kaartafbeelding.
	 * 
	 * @param image
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected String storeImage(RenderedImage image)
			throws InstantiationException, IllegalAccessException {

		String key = Beans.getReference(InterchangeStore.class).add(
				new EncodableContent<RenderedImage>(RasterEncoderFactory
						.instance().newInstance("image/png"), image));
		imageKeys.add(key);

		return Beans.getReference(InterchangeStore.class).getURL(key);
	}

	/**
	 * Toevoegen NoordPijl
	 * 
	 * @param viewer
	 * @param g2d
	 * @param width
	 * @param height
	 */
	protected void addNorthArrow(MapViewer viewer, Graphics2D g2d, int width,
			int height) {

		try {
			Resource resource = FacesContext.getCurrentInstance()
					.getApplication().getResourceHandler()
					.createResource("map/images/graphics/northArrow.png");
			InputStream in = resource.getInputStream();
			try {
				BufferedImage image = ImageIO.read(in);
				g2d.drawImage(image, width - 30, 5, 25, 50, null);
			} finally {
				in.close();
			}
		} catch (Exception e) {
			LOG.error("Can not add north arrow graphic.", e);
		}
	}

	/**
	 * Toevoegen SchaalLijn.
	 * 
	 * @param viewer
	 * @param g2d
	 * @param width
	 * @param height
	 * @param scale
	 * @param dpi
	 */
	protected void addScaleLine(MapViewer viewer, Graphics2D g2d, int width,
			int height, double scale, double dpi) {

		try {
			BufferedImage image = (BufferedImage) viewer.getScaleGraphic(100,
					20, scale, viewer.getContext().getSrsName(), dpi);
			g2d.drawImage(image, width - 100, height - 20 - 25, 100, 20, null);
		} catch (Exception e) {
			LOG.error("Can not add scale line graphic.", e);
		}
	}

	/**
	 * Ophalen van de pijltjes op de shared drive ahv de imageCode bij een bord.
	 * 
	 * @param imageCode
	 * @return
	 */
	private String getPijlBord(String imageCode) {

		String url = "";
		String host = DefaultConfiguration.instance().getString(
				"core.server.url");

		if (imageCode.equals(PIJL_LINKS)) {
			url = host + "/fotos/icons/pijl_links.png";
		}
		if (imageCode.equals(PIJL_RECHTS)) {
			url = host + "/fotos/icons/pijl_rechts.png";
		}
		if (imageCode.equals(PIJL_RECHTDOOR)) {
			url = host + "/fotos/icons/pijl_rechtdoor.png";
		}
		return url;
	}

	/**
	 * Ohalen van de voorbeeldborden op de shared drive ahv het type Route.
	 * 
	 * @param traject
	 * @return
	 */
	private String getVoorbeeldRouteBord(Traject traject) {

		Route route = (Route) traject;
		String url = "";
		String host = DefaultConfiguration.instance().getString(
				"core.server.url");

		if (traject instanceof AutoRoute) {
			url = host + "/fotos/icons/bordfiches/autoroute.jpg";
		}

		if (traject instanceof FietsRoute) {
			url = host + "/fotos/icons/bordfiches/fietsroute.jpg";
		}

		if (traject instanceof RuiterRoute) {

			if (route.getRouteType().toLowerCase().contains("ruiter")
					&& route.getRouteType().toLowerCase().contains("men")) {
				url = host + "/fotos/icons/bordfiches/ruitermenroute.jpg";
			}

			else if (route.getRouteType().toLowerCase().contains("ruiter")) {
				url = host + "/fotos/icons/bordfiches/ruiterroute.jpg";
			}

			else {
				url = host + "/fotos/icons/bordfiches/menroute.jpg";
			}
		}

		if (traject instanceof WandelRoute) {

			String routeNaam = traject.getNaam();

			if (routeNaam.startsWith("T")
					&& Character.isDigit(routeNaam.charAt(1))) {

				String taalGrensRoute = routeNaam.substring(0,
						routeNaam.indexOf(" "));
				return host + "/fotos/icons/bordfiches/wandelroute_"
						+ taalGrensRoute + ".jpg";
			}

			if (traject.getNaam().toLowerCase().contains("groen")) {
				url = host + "/fotos/icons/bordfiches/wandelroute_groen.jpg";
			}

			else if (traject.getNaam().toLowerCase().contains("rood")) {
				url = host + "/fotos/icons/bordfiches/wandelroute_rood.jpg";
			}

			else if (traject.getNaam().toLowerCase().contains("oranje")) {
				url = host + "/fotos/icons/bordfiches/wandelroute_oranje.jpg";
			}

			else {
				url = host + "/fotos/icons/bordfiches/wandelroute_groen.jpg";
			}
		}
		return url;
	}

	/**
	 * Opzoeken van het NetwerkBordVoorbeeld op de shared drive.
	 * 
	 * @param bord
	 * @return
	 */
	private String getVoorbeeldNetwerkBord(NetwerkBord bord) {

		if (bord instanceof WandelNetwerkBord) {
			return getUrlWandelNetwerkBord(bord);
		}

		else if (bord instanceof FietsNetwerkBord) {
			return getUrlFietsNetwerkBord(bord);
		}

		return "";
	}

	/**
	 * Opzoeken van het aantal effectief gekoppelde knooppunten aan het bord.
	 * 
	 * @param bord
	 * @return
	 */
	private int getAantalKnooppunten(NetwerkBord bord) {

		int counter = 0;

		if (bord.getKpid1() != null) {
			counter++;
		}
		if (bord.getKpid2() != null) {
			counter++;
		}
		if (bord.getKpid3() != null) {
			counter++;
		}
		return counter;
	}

	/**
	 * Opzoeken van het voorbeeld WandelNetwerkBord. Aan de hand van het aantal
	 * gekoppelde knooppunten en de imageCode bij deze knoopunten kan het juiste
	 * bord gevonden worden.
	 * 
	 * @param bord
	 * @return
	 */
	private String getUrlWandelNetwerkBord(NetwerkBord bord) {

		String url = "";
		String host = DefaultConfiguration.instance().getString(
				"core.server.url");

		String positiePijlKp1 = getPositiePijl(bord.getKp1ImageCode());
		String positiePijlKp2 = getPositiePijl(bord.getKp2ImageCode());
		String positiePijlKp3 = getPositiePijl(bord.getKp3ImageCode());

		if (bord.getBordType().toLowerCase().contains("doorverwijs")) {

			url = host + "/fotos/icons/bordfiches/wnw_doorverwijs.jpg";
		}

		else if (bord.getBordType().toLowerCase().contains("knooppunt")) {

			int aantalKnooppunten = getAantalKnooppunten(bord);

			if (aantalKnooppunten == 3) {

				url = host + "/fotos/icons/bordfiches/wnw_" + positiePijlKp1
						+ positiePijlKp2 + positiePijlKp3 + ".jpg";
			}

			else if (aantalKnooppunten == 2) {

				url = host + "/fotos/icons/bordfiches/wnw_" + positiePijlKp1
						+ positiePijlKp2 + ".jpg";
			}
		}

		return url;
	}

	/**
	 * Opzoeken van het voorbeeld FietsNetwerkBord. Aan de hand van het aantal
	 * gekoppelde knooppunten en de imageCode bij deze knoopunten kan het juiste
	 * bord gevonden worden.
	 * 
	 * @param bord
	 * @return
	 */
	private String getUrlFietsNetwerkBord(NetwerkBord bord) {

		String url = "";
		String host = DefaultConfiguration.instance().getString(
				"core.server.url");

		String positiePijlKp1 = getPositiePijl(bord.getKp1ImageCode());
		String positiePijlKp2 = getPositiePijl(bord.getKp2ImageCode());
		String positiePijlKp3 = getPositiePijl(bord.getKp3ImageCode());

		if (bord.getBordType().toLowerCase().contains("doorverwijs")) {

			url = host + "/fotos/icons/bordfiches/fnw_"
					+ bord.getBordBase().toLowerCase().replace(" ", "_")
					+ "_doorverwijs.jpg";
		}

		else if (bord.getBordType().toLowerCase().contains("knooppunt")) {

			int aantalKnooppunten = getAantalKnooppunten(bord);

			if (aantalKnooppunten == 2) {

				url = host + "/fotos/icons/bordfiches/fnw_" + positiePijlKp1
						+ positiePijlKp2 + ".jpg";
			}

			else if (aantalKnooppunten == 3) {

				url = host + "/fotos/icons/bordfiches/fnw_" + positiePijlKp1
						+ positiePijlKp2 + positiePijlKp3 + ".jpg";
			}
		}
		return url;
	}

	/**
	 * Ophalen template indien geen foto beschikbaar.
	 * 
	 * @return
	 */
	private String getUrlGeenFoto() {

		String host = DefaultConfiguration.instance().getString(
				"core.server.url");

		return host + "/fotos/geen%20foto.png";
	}

	/**
	 * Afleiden van de pijlposistie op het bord ahv de imageCode. Een pijl naar
	 * rechts staat steeds rechts van het knooppuntnummer. De pijlen links en
	 * rechtdoor staan steeds aan de linkerzijde van het knooppuntnummer.
	 * 
	 * @param imageCode
	 * @return
	 */
	private String getPositiePijl(String imageCode) {

		String pijlPositie = "";

		if (imageCode != null && imageCode.equals(PIJL_RECHTS)) {

			pijlPositie = POSITIE_PIJL_RECHTS;
		}

		else {
			pijlPositie = POSITIE_PIJL_LINKS;
		}

		return pijlPositie;
	}
}
