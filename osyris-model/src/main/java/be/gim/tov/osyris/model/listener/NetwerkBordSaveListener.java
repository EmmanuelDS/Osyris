package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;

import be.gim.commons.bean.Beans;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.FietsNetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.RichtingEnum;
import be.gim.tov.osyris.model.traject.WandelNetwerkBord;

import com.ocpsoft.pretty.faces.util.StringUtils;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "NetwerkBord") })
public class NetwerkBordSaveListener {

	private static final Log log = LogFactory
			.getLog(NetwerkBordSaveListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkBord netwerkBord = (NetwerkBord) event.getModelObject();

		// Automatically set Gemeente
		if (netwerkBord.getGemeente() == null) {

			String gemeente = Beans.getReference(OsyrisModelFunctions.class)
					.getGemeenteForBord(netwerkBord);
			netwerkBord.setGemeente(gemeente);
		}

		// Koppelen dichtste bijgelegen segment, indien afstand gelijk is worden
		// meerdere segmenten gekoppeld
		if (netwerkBord.getSegmenten() == null
				|| netwerkBord.getSegmenten().isEmpty()) {

			List<ResourceIdentifier> segmentIds = Beans.getReference(
					OsyrisModelFunctions.class).getSegmentenForNetwerkBord(
					netwerkBord);

			netwerkBord.getSegmenten().addAll(segmentIds);
		}

		// Automatically set Regio
		if (netwerkBord.getBordType() != null) {
			if (netwerkBord.getBordType().equals("doorverwijs")
					&& !netwerkBord.getSegmenten().isEmpty()) {

				// Doorverwijsbord set regio van bijhorend segment
				NetwerkSegment seg = (NetwerkSegment) modelRepository
						.loadObject(netwerkBord.getSegmenten().get(0));
				netwerkBord.setRegio(seg.getRegio());
			}
		}

		// KnooppuntNrs
		if (netwerkBord.getKpid0() != null) {

			if (StringUtils.isNotBlank(netwerkBord.getKpid0().toString())) {
				NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
						.loadObject(netwerkBord.getKpid0());
				netwerkBord.setKpnr0(knooppunt.getNummer());

				if (netwerkBord.getBordType() != null
						&& netwerkBord.getBordType().equals("knooppunt")) {

					// Knooppuntbord set regio van bijhorend KP0
					netwerkBord.setRegio(knooppunt.getRegio());

					// Bepalen richting knooppuntbord op basis van KPnr0
					NetwerkSegment seg = (NetwerkSegment) modelRepository
							.loadObject(netwerkBord.getSegmenten().get(0));

					NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
							.loadObject(seg.getNaarKnooppunt());

					if (naarKp.getNummer().equals(knooppunt.getNummer())) {
						netwerkBord.setRichting(RichtingEnum.FT);
					}

					else {
						netwerkBord.setRichting(RichtingEnum.TF);
					}
				}

			} else {
				netwerkBord.setKpnr0(null);
				netwerkBord.setKpid0(null);
			}
		}
		if (netwerkBord.getKpid1() != null) {

			if (StringUtils.isNotBlank(netwerkBord.getKpid1().toString())) {
				NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
						.loadObject(netwerkBord.getKpid1());
				netwerkBord.setKpnr1(knooppunt.getNummer());

				// Bepalen richting doorverwijsbord op basis van KPnr1
				NetwerkSegment seg = (NetwerkSegment) modelRepository
						.loadObject(netwerkBord.getSegmenten().get(0));

				NetwerkKnooppunt naarKp = (NetwerkKnooppunt) modelRepository
						.loadObject(seg.getNaarKnooppunt());

				if (netwerkBord.getBordType() != null
						&& netwerkBord.getBordType().equals("doorverwijs")) {

					if (naarKp.getNummer().equals(knooppunt.getNummer())) {
						netwerkBord.setRichting(RichtingEnum.FT);
					}

					else {
						netwerkBord.setRichting(RichtingEnum.TF);
					}
				}

			} else {
				netwerkBord.setKpnr1(null);
				netwerkBord.setKpid1(null);
			}
		}
		if (netwerkBord.getKpid2() != null) {

			if (StringUtils.isNotBlank(netwerkBord.getKpid2().toString())) {
				NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
						.loadObject(netwerkBord.getKpid2());
				netwerkBord.setKpnr2(knooppunt.getNummer());
			} else {
				netwerkBord.setKpnr2(null);
				netwerkBord.setKpid2(null);
			}
		}
		if (netwerkBord.getKpid3() != null) {

			if (StringUtils.isNotBlank(netwerkBord.getKpid3().toString())) {
				NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
						.loadObject(netwerkBord.getKpid3());
				netwerkBord.setKpnr3(knooppunt.getNummer());
			} else {
				netwerkBord.setKpnr3(null);
				netwerkBord.setKpid3(null);
			}
		}

		// Set BordBase = naam Regio van bord
		Regio regio = (Regio) Beans.getReference(ModelRepository.class)
				.loadObject(netwerkBord.getRegio());
		netwerkBord.setBordBase(regio.getNaam());

		// FNW Bord regioNaam is naam
		if (netwerkBord instanceof FietsNetwerkBord) {
			((FietsNetwerkBord) netwerkBord).setNaam(regio.getNaam());
		}

		// WNW Bord naam van gekoppeld segment is naam
		if (netwerkBord instanceof WandelNetwerkBord) {

			WandelNetwerkBord wnwBord = (WandelNetwerkBord) netwerkBord;

			if (wnwBord.getSegmenten().size() > 0) {
				ResourceIdentifier segmentId = wnwBord.getSegmenten().get(0);

				if (segmentId != null) {
					NetwerkSegment segment = (NetwerkSegment) modelRepository
							.loadObject(segmentId);
					((WandelNetwerkBord) netwerkBord)
							.setNaam(segment.getNaam());
				}
			}

			// Indien geen segment gekoppeld gebruik Regionaam
			else {
				wnwBord.setNaam(regio.getNaam());
			}
		}
	}
}
