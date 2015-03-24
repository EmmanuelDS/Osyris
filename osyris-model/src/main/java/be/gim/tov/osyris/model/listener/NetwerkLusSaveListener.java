package be.gim.tov.osyris.model.listener;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.Transformer;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.bean.Beans;
import be.gim.commons.collections.CollectionUtils;
import be.gim.commons.filter.FilterUtils;
import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.label.LabelUtils;
import be.gim.commons.resource.ResourceKey;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.traject.NetwerkLus;
import be.gim.tov.osyris.model.traject.Regio;

import com.vividsolutions.jts.geom.Geometry;

@Listener(rules = { @Rule(_for = "NetwerkLus", type = "save") })
public class NetwerkLusSaveListener {

	@SuppressWarnings("unchecked")
	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {

		NetwerkLus lus = (NetwerkLus) event.getModelObject();

		ModelRepository modelRepository = event.getSource();

		QueryBuilder builder = new QueryBuilder("NetwerkSegment");

		builder.result(FilterUtils.property("geom"));

		builder.addFilter(FilterUtils.in("id", CollectionUtils.transform(
				lus.getSegmenten(), new Transformer() {

					@Override
					public Object transform(Object input) {
						return new Long(((ResourceKey) input).getIdPart());
					}
				})));

		List<Geometry> geoms = (List) modelRepository.searchObjects(
				builder.build(), false, false);

		lus.setGeom(GeometryUtils.union(geoms));

		// Automatisch setten Regio via lijst van geometries van de segmenten in
		// de lus
		Regio regio = Beans.getReference(OsyrisModelFunctions.class)
				.getRegioForSegmenten(geoms);
		if (regio != null) {
			lus.setRegio(modelRepository.getResourceKey(regio));
		}

		// Bereken of de lus al dan niet gesloten is
		// Standaard is dit niet het geval
		lus.setLusGesloten("0");

		boolean isGesloten = Beans.getReference(OsyrisModelFunctions.class)
				.checkIsLusGesloten(lus.getSegmenten());

		if (isGesloten) {
			lus.setLusGesloten("1");
		}

		// Automatically set naam
		String naam = LabelUtils.upperSpaced(lus.getModelClass().getName()
				.replace("Netwerk", "").toLowerCase())
				+ " " + regio.getNaam() + " " + lus.getId().toString();
		lus.setNaam(naam);
	}
}
