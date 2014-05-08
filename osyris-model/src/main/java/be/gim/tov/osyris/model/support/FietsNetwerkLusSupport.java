package be.gim.tov.osyris.model.support;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.core.handler.object.AbstractObjectSupport;

import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.localization.InternationalString;
import be.gim.tov.osyris.model.traject.FietsNetwerkLus;
import be.gim.tov.osyris.model.traject.Traject;

@For("FietsNetwerkLus")
@Handler(type = "object")
public class FietsNetwerkLusSupport extends
		AbstractObjectSupport<FietsNetwerkLus> {

	@Override
	public InternationalString getLabel(ModelObject object) {
		Traject traject = (Traject) object;
		return new DefaultInternationalString(traject.getNaam());
	}
}
