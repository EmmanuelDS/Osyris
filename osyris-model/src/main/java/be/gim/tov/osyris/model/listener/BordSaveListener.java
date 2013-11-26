package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.api.store.ModelStore;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.Bord;
import be.gim.tov.osyris.model.traject.NetwerkBord;
import be.gim.tov.osyris.model.traject.NetwerkKnooppunt;
import be.gim.tov.osyris.model.traject.Regio;
import be.gim.tov.osyris.model.traject.RouteBord;
import be.gim.tov.osyris.model.traject.Traject;

import com.ocpsoft.pretty.faces.util.StringUtils;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Bord") })
public class BordSaveListener {

	@Inject
	private ModelRepository modelRepository;

	@SuppressWarnings("unchecked")
	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		Bord bord = (Bord) event.getModelObject();

		// Indien nieuw Bord
		if (bord.getId() == null) {

			// Set correcte id range voor elk subtype
			ModelStore modelStore = modelRepository
					.getModelStore("OsyrisDataStore");
			Long maxId = (Long) modelStore.searchObjects(
					"SELECT MAX(id) FROM " + bord.getModelClass().getName())
					.get(0);
			Long newId = maxId + 1;
			bord.setId(newId);
			bord.setLabelId(newId.toString());
		}

		// Set route voor RouteBord indien niet aanwezig
		if (bord instanceof RouteBord) {

			if (((RouteBord) bord).getRoute() == null && bord.getNaam() != null) {

				QueryBuilder builder = new QueryBuilder("Traject");
				builder.addFilter(FilterUtils.equal("naam", bord.getNaam()));
				builder.maxResults(1);
				List<Traject> result = (List<Traject>) modelRepository
						.searchObjects(builder.build(), false, false);
				Traject route = result.get(0);
				((RouteBord) bord).setRoute(modelRepository
						.getResourceIdentifier(route));
			}
		}

		// Indien Netwerkbord set knooppuntnummers aan de hand van de ingevulde
		// knooppuntID
		if (bord instanceof NetwerkBord) {

			NetwerkBord netwerkBord = (NetwerkBord) bord;
			if (netwerkBord.getKpid0() != null) {

				if (StringUtils.isNotBlank(netwerkBord.getKpid0().toString())) {
					NetwerkKnooppunt knooppunt = (NetwerkKnooppunt) modelRepository
							.loadObject(netwerkBord.getKpid0());
					netwerkBord.setKpnr0(knooppunt.getNummer());
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

			if (netwerkBord.getBordBase() == null) {

				Regio regio = Beans.getReference(OsyrisModelFunctions.class)
						.getRegioForBord(bord);
				((NetwerkBord) bord).setBordBase(regio.getNaam());
			}

		}

		// Automatically set X Y coordinates indien niet aanwezig
		if (bord.getGeom() != null && bord.getGeom() instanceof Point) {

			Point p = (Point) bord.getGeom();
			bord.setX(p.getX());
			bord.setY(p.getY());
		}

		if (bord.getRegio() == null) {

			Regio regio = Beans.getReference(OsyrisModelFunctions.class)
					.getRegioForBord(bord);
			bord.setRegio(modelRepository.getResourceKey(regio));
		}
	}
}
