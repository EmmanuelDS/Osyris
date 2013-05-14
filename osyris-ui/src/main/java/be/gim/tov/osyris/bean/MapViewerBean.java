package be.gim.tov.osyris.bean;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.ModelProperty;
import org.conscientia.api.repository.ModelRepository;

import be.gim.commons.resource.ResourceKey;
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

		try {
			context = (MapContext) modelRepository.loadObject(new ResourceKey(
					"Form@10"));

			if (context != null) {
				configuration = mapFactory.getConfiguration(context.getId()
						.toString(), context);
			}
		} catch (IOException e) {
			LOG.error("Can not load mapcontext.", e);
		}
		return configuration;
	}
}
