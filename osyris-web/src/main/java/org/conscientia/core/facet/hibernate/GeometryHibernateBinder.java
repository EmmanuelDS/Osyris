package org.conscientia.core.facet.hibernate;

import java.util.Properties;

import org.conscientia.api.model.ModelProperty;
import org.conscientia.api.model.annotation.For;
import org.conscientia.api.model.annotation.Handler;
import org.conscientia.core.model.property.GeometryModelProperty;

import be.gim.commons.geometry.GeometryUtils;
import be.gim.commons.hibernate.DatabaseProduct;
import be.gim.commons.hibernate.usertype.GeometryUserType;

@Handler(type = "propertyHibernateBinder")
@For("GeometryModelProperty")
public class GeometryHibernateBinder extends AbstractHibernateBinder {

	@Override
	protected String getTypeName(DatabaseProduct product,
			ModelProperty modelProperty) {
		return GeometryUserType.class.getName();
	}

	@Override
	protected boolean getLazy(ModelProperty modelProperty) {
		return false;
	}

	@Override
	protected Properties getTypeParameters(DatabaseProduct product,
			ModelProperty modelProperty) {

		Properties parameters = new Properties();
		parameters.put("srid", Integer.toString(GeometryUtils
				.toSRID(((GeometryModelProperty) modelProperty).getCrs())));
		return parameters;
	}
}
