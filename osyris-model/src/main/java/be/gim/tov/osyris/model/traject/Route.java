package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Search;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class Route extends Traject {

	// VARIABLES
	// @Type(value = ModelPropertyType.ENUM)
	// @ValuesExpression("#{osyrisModelFunctions.getCodeList('RouteTypeCode')}")
	@Edit(type = "suggestions")
	@Search(type = "suggestions:like-wildcard-nocase")
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