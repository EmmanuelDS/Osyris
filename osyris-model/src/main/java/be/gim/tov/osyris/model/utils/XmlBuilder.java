package be.gim.tov.osyris.model.utils;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import be.gim.tov.osyris.model.controle.ControleOpdracht;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * @author kristof
 * 
 */
public class XmlBuilder {

	/**
	 * Opbouwen van individuele bordfiches in XML.
	 * 
	 * @param object
	 * @param borden
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static Document buildBordFiches(ControleOpdracht object,
			List<Bord> borden) throws ParserConfigurationException,
			TransformerException {

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

		if (object.getTrajectType().contains("netwerk")) {
			rootElement.setAttribute("netwerkbord", Boolean.TRUE.toString());
		} else {
			rootElement.setAttribute("netwerkbord", Boolean.FALSE.toString());
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
				} else {
					knooppunt0.appendChild(doc
							.createTextNode(StringUtils.EMPTY));
				}
				bord.appendChild(knooppunt0);

				Element knooppunt1 = doc.createElement("knooppunt1");
				if (((NetwerkBord) b).getKpnr1() != null) {
					knooppunt1.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr1().toString()));
				} else {
					knooppunt1.appendChild(doc
							.createTextNode(StringUtils.EMPTY));
				}
				bord.appendChild(knooppunt1);

				Element knooppunt2 = doc.createElement("knooppunt2");
				if (((NetwerkBord) b).getKpnr2() != null) {
					knooppunt2.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr2().toString()));
				} else {
					knooppunt2.appendChild(doc
							.createTextNode(StringUtils.EMPTY));
				}
				bord.appendChild(knooppunt2);

				Element knooppunt3 = doc.createElement("knooppunt3");
				if (((NetwerkBord) b).getKpnr3() != null) {
					knooppunt3.appendChild(doc.createTextNode(((NetwerkBord) b)
							.getKpnr3().toString()));
				} else {
					knooppunt3.appendChild(doc
							.createTextNode(StringUtils.EMPTY));
				}
				bord.appendChild(knooppunt3);
			}

			Element constructieType = doc.createElement("constructietype");
			constructieType.appendChild(doc.createTextNode(b.getBordConst()));
			bord.appendChild(constructieType);

			Element paalType = doc.createElement("paaltype");
			paalType.appendChild(doc.createTextNode(b.getPaalConst()));
			bord.appendChild(paalType);

			Element paalDiameter = doc.createElement("paaldiameter");
			paalDiameter.appendChild(doc.createTextNode(b.getPaalDia()));
			bord.appendChild(paalDiameter);

			Element paalBeugel = doc.createElement("paalbeugel");
			paalBeugel.appendChild(doc.createTextNode(b.getPaalBeugel()));
			bord.appendChild(paalBeugel);

			Element paalOndergrond = doc.createElement("paalondergrond");
			paalOndergrond.appendChild(doc.createTextNode(b.getPaalGrond()));
			bord.appendChild(paalOndergrond);
		}

		// XML String output for testing purposes
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = tf.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		trans.transform(new DOMSource(doc), new StreamResult(writer));
		String output = writer.getBuffer().toString().replaceAll("\n|\r", "");

		return doc;
	}

	/**
	 * Ombouwen bewegwijzeringtabel in XML
	 * 
	 * @param object
	 * @param borden
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static Document buildBewegwijzeringTabel(Traject traject,
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

		if (object.getTrajectType().contains("netwerk")) {
			rootElement.setAttribute("netwerkCO", Boolean.TRUE.toString());
		} else {
			rootElement.setAttribute("netwerkCO", Boolean.FALSE.toString());
		}

		for (Bord b : borden) {
			// Bord elementen
			Element bord = doc.createElement("bord");
			rootElement.appendChild(bord);

			Element volgNr = doc.createElement("bordnr");
			if (b.getVolg() != null) {
				volgNr.appendChild(doc.createTextNode(b.getVolg()));
			} else {
				volgNr.appendChild(doc.createTextNode(StringUtils.EMPTY));
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
}
