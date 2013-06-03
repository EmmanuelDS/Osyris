package be.gim.tov.osyris.bean;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.conscientia.api.model.ModelObject;
import org.conscientia.api.model.ModelProperty;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.util.ModelUtils;

import be.gim.commons.resource.ResourceKey;
import be.gim.peritia.reference.Reference;
import be.gim.specto.api.configuration.MapConfiguration;
import be.gim.specto.api.context.MapContext;
import be.gim.specto.core.context.MapFactory;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class MapViewerBean {

	protected static final List<String> TOOLS = Arrays
			.asList(new String[] { "drawPoint" });

	@Inject
	private ModelRepository modelRepository;
	@Inject
	private MapFactory mapFactory;

	public MapConfiguration getConfiguration(ModelProperty property,
			ModelObject object) throws IOException {

		MapContext context = (MapContext) modelRepository
				.loadObject(new ResourceKey("Form@12"));

		if (context != null) {
			Reference<?> reference = ModelUtils.valuePointer(object, "geom");

			return mapFactory.getConfiguration(context.getId().toString(),
					context, reference, true, null, TOOLS, null);
		}

		return null;
	}
}
