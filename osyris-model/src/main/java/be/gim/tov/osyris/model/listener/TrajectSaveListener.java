package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;

import be.gim.tov.osyris.model.traject.Traject;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(type = "save", _for = "Traject") })
public class TrajectSaveListener {

	private static final String GEEN_PETER_METER = "Geen PeterMeter toegewezen";

	public void processEvent(ModelEvent event) throws IOException,
			InstantiationException, IllegalAccessException {
		Traject traject = (Traject) event.getModelObject();

		if (traject.getPeterMeter1() != null
				&& traject.getPeterMeter1().equals(GEEN_PETER_METER)) {
			traject.setPeterMeter1(null);
		}
		if (traject.getPeterMeter2() != null
				&& traject.getPeterMeter2().equals(GEEN_PETER_METER)) {
			traject.setPeterMeter2(null);
		}
		if (traject.getPeterMeter3() != null
				&& traject.getPeterMeter3().equals(GEEN_PETER_METER)) {
			traject.setPeterMeter3(null);
		}
	}
}
