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
public class PropertyControleOpdrachtSearchableBean {

	@Inject
	private Identity identity;

	public boolean isSearchable(ModelObject object, ModelProperty property) {

		if (property.getName().equals("peterMeter")) {
			if (identity.inGroup("PeterMeter", "CUSTOM")) {
				property.setSearchable(false);
			} else {
				property.setSearchable(true);
			}
		}

		return property.isSearchable();
	}

}
