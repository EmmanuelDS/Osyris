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
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.WandelNetwerkBord;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "create", _for = "NetwerkBord") })
public class NetwerkBordCreateListener {

	private static final Log log = LogFactory
			.getLog(NetwerkBordCreateListener.class);

	@Inject
	private ModelRepository modelRepository;

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkBord netwerkBord = (NetwerkBord) event.getModelObject();
		netwerkBord.setActief("1");

		// Automatically set Regio
		if (netwerkBord.getRegio() == null) {

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForBord(netwerkBord);
			netwerkBord.setRegio(modelRepository.getResourceKey(regio));
		}

		// Automatically set Gemeente
		if (netwerkBord.getGemeente() == null) {

			String gemeente = Beans.getReference(OsyrisModelFunctions.class)
					.getGemeenteForBord(netwerkBord);
			netwerkBord.setGemeente(gemeente);
		}

		if (netwerkBord.getSegmenten() == null
				|| netwerkBord.getSegmenten().isEmpty()) {

			List<ResourceIdentifier> segmentIds = Beans.getReference(
					OsyrisModelFunctions.class).getSegmentenForNetwerkBord(
					netwerkBord);

			netwerkBord.getSegmenten().addAll(segmentIds);
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
