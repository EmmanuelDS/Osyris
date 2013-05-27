package be.gim.tov.osyris.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log LOG = LogFactory.getLog(MapViewerBean.class);

	@Inject
	private ModelRepository modelRepository;

	@Inject
	private MapFactory mapFactory;

	private MapConfiguration configuration;

	public MapConfiguration getConfiguration(ModelProperty property) {
		MapContext context;
		List<String> tools = new ArrayList<String>();
		tools.add("drawLineString");

		try {
			context = (MapContext) modelRepository.loadObject(new ResourceKey(
					"Form@12"));

			if (context != null) {
				// Bind reference and start Mapconfiguration
				Reference<?> reference = ModelUtils.valuePointer(property,
						"geom");
				reference.getType();
				// configuration = mapFactory.getConfiguration(context.getId()
				// .toString(), context, reference, true, null, tools,
				// null);
				configuration = mapFactory.getConfiguration(context.getId()
						.toString(), context);
			}
		} catch (IOException e) {
			LOG.error("Can not load mapcontext.", e);
		}
		return configuration;
	}
}
