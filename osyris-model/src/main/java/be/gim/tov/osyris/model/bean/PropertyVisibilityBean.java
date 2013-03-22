package be.gim.tov.osyris.model.bean;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.repository.ModelRepository;

@Named
public class PropertyVisibilityBean {

	private static final Log log = LogFactory
			.getLog(PropertyVisibilityBean.class);

	@Inject
	private ModelRepository modelRepository;
}
