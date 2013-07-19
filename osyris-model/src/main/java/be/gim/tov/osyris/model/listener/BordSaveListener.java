package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;

import be.gim.tov.osyris.model.traject.Bord;

import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Bord") })
public class BordSaveListener {

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		Bord bord = (Bord) event.getModelObject();

		// Automatically set X Y coordinates
		if (bord.getGeom() != null && bord.getGeom() instanceof Point) {
			Point p = (Point) bord.getGeom();
			bord.setX(p.getX());
			bord.setY(p.getY());
		}

		// TODO: bordVolgorde en BordLabels zetten?
	}
}
