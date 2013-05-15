package be.gim.tov.osyris.model.werk;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class GebruiktMateriaal extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Aantal")
	@Description("Aantal")
	private int aantal;

	@Label("Stockmateriaal")
	@Description("Stockmateriaal")
	@ModelClassName("StockMateriaal")
	private ResourceIdentifier stockMateriaal;

	// GETTERS AND SETTERS
	public int getAantal() {
		return aantal;
	}

	public void setAantal(int aantal) {
		this.aantal = aantal;
	}

	public ResourceIdentifier getStockMateriaal() {
		return stockMateriaal;
	}

	public void setStockMateriaal(ResourceIdentifier stockMateriaal) {
		this.stockMateriaal = stockMateriaal;
	}
}