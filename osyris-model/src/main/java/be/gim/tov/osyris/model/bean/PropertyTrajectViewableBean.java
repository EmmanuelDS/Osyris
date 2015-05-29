package be.gim.tov.osyris.model.bean;

import javax.inject.Inject;
import javax.inject.Named;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;
import org.jboss.seam.security.Identity;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class PropertyTrajectViewableBean {

	@Inject
	private Identity identity;

	public boolean isViewable(ModelObject object, ModelProperty property) {

		// Expliciet verbergen van de peterMeter velden bij een traject indien
		// de ingelogde gebruiker in group PeterMeter of Uitvoerder is
		if (identity.inGroup("PeterMeter", "CUSTOM")
				|| identity.inGroup("Uitvoerder", "CUSTOM")) {

			if (property.getName().equals("peterMeter1")
					|| (property.getName().equals("peterMeter2"))
					|| (property.getName().equals("peterMeter3"))) {
				return false;
			}
		}

		return property.isViewable();
	}
}
