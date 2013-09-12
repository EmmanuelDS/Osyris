package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class Route extends Traject {

	// VARIABLES
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('RouteTypeCode')}")
	private String routeType;

	// GETTERS AND SETTERS
	@Override
	@NotEditable
	@NotSearchable
	public Long getId() {
		return (Long) super.getId();
	}

	public String getRouteType() {
		return routeType;
	}

	public void setRouteType(String routeType) {
		this.routeType = routeType;
	}
}