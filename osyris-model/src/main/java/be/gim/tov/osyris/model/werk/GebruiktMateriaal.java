package be.gim.tov.osyris.model.werk;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.core.model.AbstractModelObject;

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
	@Label("Stockmateriaal")
	@Description("Stockmateriaal")
	private StockMateriaal stockMateriaal;
	
	@Label("Aantal")
	@Description("Aantal")
	private int aantal;

	// GETTERS AND SETTERS
	public StockMateriaal getStockMateriaal() {
		return stockMateriaal;
	}

	public void setStockMateriaal(StockMateriaal stockMateriaal) {
		this.stockMateriaal = stockMateriaal;
	}
	
	public int getAantal() {
		return aantal;
	}

	public void setAantal(int aantal) {
		this.aantal = aantal;
	}
}