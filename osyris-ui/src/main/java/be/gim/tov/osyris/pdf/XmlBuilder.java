package be.gim.tov.osyris.pdf;

import java.awt.image.RenderedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xerces.impl.dv.util.Base64;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.service.interchange.InterchangeStore;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import be.gim.commons.bean.Beans;
import be.gim.commons.encoder.impl.raster.RasterEncoderFactory;
import be.gim.peritia.codec.EncodableContent;
import be.gim.specto.ui.component.MapViewer;
import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Route;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;
import be.gim.tov.osyris.model.werk.WerkHandeling;
import be.gim.tov.osyris.model.werk.WerkOpdracht;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author kristof
 * 
 */
public class XmlBuilder {

	@Inject
	protected ModelRepository modelRepository;

	protected Collection<String> imageKeys = new ArrayList<String>();

	/**
	 * Opbouwen van individuele bordfiches in XML.
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
	public Document buildBordFiches(ControleOpdracht object, List<Bord> borden,
			MapViewer viewer) throws ParserConfigurationException,
			TransformerException, DOMException, InstantiationException,
			IllegalAccessException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("verslag");
		doc.appendChild(rootElement);

		Element regio = doc.createElement("regio");
		regio.appendChild(doc.createTextNode(object.getRegio()));
		rootElement.appendChild(regio);

		Element trajectType = doc.createElement("trajecttype");
		trajectType.appendChild(doc.createTextNode(object.getTrajectType()));
		rootElement.appendChild(trajectType);

		rootElement.setAttribute("netwerkbord", Boolean.FALSE.toString());

		if (object.getTrajectType().contains("netwerk")) {
			rootElement.setAttribute("netwerkbord", Boolean.TRUE.toString());
		}

		for (Bord b : borden) {
			// Bord elementen
			Element bord = doc.createElement("bord");
			rootElement.appendChild(bord);

			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode(b.getId().toString()));
			bord.appendChild(id);

			Element gemeente = doc.createElement("gemeente");
			gemeente.appendChild(doc.createTextNode(b.getGemeente()));
			bord.appendChild(gemeente);

			Element straat = doc.createElement("straat");
			straat.appendChild(doc.createTextNode(b.getStraatnaam()));
			bord.appendChild(straat);

			Element x = doc.createElement("x");
			x.appendChild(doc.createTextNode(BigDecimal.valueOf(b.getX())
					.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
			bord.appendChild(x);

			Element y = doc.createElement("y");
			y.appendChild(doc.createTextNode(BigDecimal.valueOf(b.getY())
					.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
			bord.appendChild(y);

			Element wegBevoegd = doc.createElement("wegbevoegd");
			wegBevoegd.appendChild(doc.createTextNode(b.getWegBevoegd()));
			bord.appendChild(wegBevoegd);

			// Indien NetwerkBord knooppunten weergeven
			if (b instanceof NetwerkBord) {

				Element knooppunt0 = doc.createElement("knooppunt0");
				if (((NetwerkBord) b).getKpnr0() != null) {
					knooppunt0.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr0().toString()));
				}
				bord.appendChild(knooppunt0);

				Element knooppunt1 = doc.createElement("knooppunt1");
				if (((NetwerkBord) b).getKpnr1() != null) {
					knooppunt1.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr1().toString()));
				}
				bord.appendChild(knooppunt1);

				Element knooppunt2 = doc.createElement("knooppunt2");
				if (((NetwerkBord) b).getKpnr2() != null) {
					knooppunt2.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr2().toString()));
				}
				bord.appendChild(knooppunt2);

				Element knooppunt3 = doc.createElement("knooppunt3");
				if (((NetwerkBord) b).getKpnr3() != null) {
					knooppunt3.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr3().toString()));
				}
				bord.appendChild(knooppunt3);
			}

			Element constructieType = doc.createElement("constructietype");
			if (b.getBordConst() != null) {
				constructieType
						.appendChild(doc.createTextNode(b.getBordConst()));
			}
			bord.appendChild(constructieType);

			Element paalType = doc.createElement("paaltype");
			if (b.getPaalConst() != null) {
				paalType.appendChild(doc.createTextNode(b.getPaalConst()));
			}
			bord.appendChild(paalType);

			Element paalDiameter = doc.createElement("paaldiameter");
			if (b.getPaalDia() != null) {
				paalDiameter.appendChild(doc.createTextNode(b.getPaalDia()));
			}
			bord.appendChild(paalDiameter);

			Element paalBeugel = doc.createElement("paalbeugel");
			if (b.getPaalBeugel() != null) {
				paalBeugel.appendChild(doc.createTextNode(b.getPaalBeugel()));
			}
			bord.appendChild(paalBeugel);

			Element paalOndergrond = doc.createElement("paalondergrond");
			if (b.getPaalGrond() != null) {
				paalOndergrond
						.appendChild(doc.createTextNode(b.getPaalGrond()));
			}
			bord.appendChild(paalOndergrond);

			Element foto = doc.createElement("foto");
			foto.appendChild(doc.createTextNode(b.getFoto().toString()));
			bord.appendChild(foto);

			Element mapOrtho = doc.createElement("mapOrtho");
			// mapOrtho.appendChild(doc.createTextNode(getMapBord(viewer, b)));
			bord.appendChild(mapOrtho);

			Element mapTopo = doc.createElement("mapTopo");
			// mapTopo.appendChild(doc.createTextNode(getMapBord(viewer, b)));
			bord.appendChild(mapTopo);
		}

		// XML String output for debug purposes
		// TransformerFactory tf = TransformerFactory.newInstance();
		// Transformer trans = tf.newTransformer();
		// trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		// StringWriter writer = new StringWriter();
		// trans.transform(new DOMSource(doc), new StreamResult(writer));
		// String output = writer.getBuffer().toString().replaceAll("\n|\r",
		// "");

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
	 */
	public Document buildBewegwijzeringTabel(Traject traject,
			ControleOpdracht object, List<Bord> borden)
			throws ParserConfigurationException, TransformerException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("verslag");
		doc.appendChild(rootElement);

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

				Element bordType = doc.createElement("bordtype");
				bordType.appendChild(doc.createTextNode(((NetwerkBord) b)
						.getBordType()));
				bord.appendChild(bordType);

				Element pijlkp1 = doc.createElement("pijlkp1");
				Element pijlkp2 = doc.createElement("pijlkp2");
				Element pijlkp3 = doc.createElement("pijlkp3");

				if (((NetwerkBord) b).getKp1ImageCode() != null) {
					pijlkp1.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKp1ImageCode()));
					bord.appendChild(pijlkp1);
				}

				if (((NetwerkBord) b).getKp2ImageCode() != null) {
					pijlkp2.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKp2ImageCode()));
					bord.appendChild(pijlkp2);
				}

				if (((NetwerkBord) b).getKp3ImageCode() != null) {
					pijlkp3.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKp3ImageCode()));
					bord.appendChild(pijlkp3);
				}
			}

			if (b instanceof RouteBord) {

				if (((RouteBord) b).getImageCode() != null) {
					Element pijl = doc.createElement("pijl");
					pijl.appendChild(doc.createTextNode(((RouteBord) b)
							.getImageCode()));
					bord.appendChild(pijl);
				}
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
	 */
	public Document buildWerkOpdrachtFicheBordProbleem(Traject traject,
			WerkOpdracht object, Bord bord)
			throws ParserConfigurationException, TransformerException {

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

		rootElement.setAttribute("bordprobleem", Boolean.TRUE.toString());
		rootElement.setAttribute("segment", Boolean.FALSE.toString());
		rootElement.setAttribute("hasFoto", Boolean.FALSE.toString());

		if (traject instanceof NetwerkSegment) {
			rootElement.setAttribute("segment", Boolean.TRUE.toString());
		}

		if (object.getProbleem().getFoto() != null) {
			rootElement.setAttribute("hasFoto", Boolean.TRUE.toString());
		}

		Element gemeente = doc.createElement("gemeente");
		gemeente.appendChild(doc.createTextNode(object.getGemeente()));
		rootElement.appendChild(gemeente);

		Element straat = doc.createElement("straat");
		straat.appendChild(doc.createTextNode(bord.getStraatnaam()));
		rootElement.appendChild(straat);

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

		Element paalType = doc.createElement("paaltype");
		if (bord.getPaalConst() != null) {
			paalType.appendChild(doc.createTextNode(bord.getPaalConst()));
		}
		rootElement.appendChild(paalType);

		Element paalDiameter = doc.createElement("paaldiameter");
		if (bord.getPaalDia() != null) {
			paalDiameter.appendChild(doc.createTextNode(bord.getPaalDia()));
		}
		rootElement.appendChild(paalDiameter);

		Element paalBeugel = doc.createElement("paalbeugel");
		if (bord.getPaalBeugel() != null) {
			paalBeugel.appendChild(doc.createTextNode(bord.getPaalBeugel()));
		}
		rootElement.appendChild(paalBeugel);

		Element paalOndergrond = doc.createElement("paalondergrond");
		if (bord.getPaalGrond() != null) {
			paalOndergrond.appendChild(doc.createTextNode(bord.getPaalGrond()));
		}
		rootElement.appendChild(paalOndergrond);

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

		Element foto = doc.createElement("foto");
		foto.appendChild(doc.createTextNode(Base64.encode(object.getProbleem()
				.getFoto())));
		rootElement.appendChild(foto);

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
	 */
	public Document buildWerkOpdrachtFicheAnderProbleem(Traject traject,
			WerkOpdracht object) throws ParserConfigurationException,
			TransformerException {

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

		rootElement.setAttribute("bordprobleem", Boolean.FALSE.toString());
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

		Element foto = doc.createElement("foto");
		foto.appendChild(doc.createTextNode(Base64.encode(object.getProbleem()
				.getFoto())));
		rootElement.appendChild(foto);

		return doc;
	}

	/**
	 * 
	 * @param viewer
	 * @param bord
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private String getMapBord(MapViewer viewer, Bord bord)
			throws InstantiationException, IllegalAccessException {

		Envelope envelope = new Envelope(bord.getGeom().getCoordinate());

		RenderedImage mapImage = viewer.getMap(viewer.getContentExtent(), 900,
				600);

		return storeImage(mapImage);
	}

	/**
	 * 
	 * @param image
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected String storeImage(RenderedImage image)
			throws InstantiationException, IllegalAccessException {

		imageKeys.clear();
		String key = Beans.getReference(InterchangeStore.class).add(
				new EncodableContent<RenderedImage>(RasterEncoderFactory
						.instance().newInstance("image/png"), image));
		imageKeys.add(key);

		return Beans.getReference(InterchangeStore.class).getURL(key);
	}
}
