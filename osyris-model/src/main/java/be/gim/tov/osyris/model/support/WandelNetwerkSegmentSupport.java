package be.gim.tov.osyris.model.support;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.handler.object.AbstractObjectSupport;

import be.gim.commons.bean.Beans;
import be.gim.commons.localization.DefaultInternationalString;
import be.gim.commons.localization.InternationalString;
import be.gim.tov.osyris.model.traject.NetwerkSegment;
import be.gim.tov.osyris.model.traject.WandelNetwerkSegment;

@For("WandelNetwerkSegment")
@Handler(type = "object")
public class WandelNetwerkSegmentSupport extends
		AbstractObjectSupport<WandelNetwerkSegment> {

	@Override
	public InternationalString getLabel(ModelObject object) {
		NetwerkSegment segment = (NetwerkSegment) object;
		return new DefaultInternationalString(Beans
				.getReference(ModelRepository.class)
				.getResourceIdentifier(segment).toString());
	}
}
