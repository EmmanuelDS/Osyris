package be.gim.tov.osyris.model.werk;

import org.conscientia.api.model.StorableObject;
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
public class GebruiktMateriaal extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private int aantal;
	private StockMateriaal stockMateriaal;

	// GETTERS AND SETTERS
	public int getAantal() {
		return aantal;
	}

	public void setAantal(int aantal) {
		this.aantal = aantal;
	}

	public StockMateriaal getStockMateriaal() {
		return stockMateriaal;
	}

	public void setStockMateriaal(StockMateriaal stockMateriaal) {
		this.stockMateriaal = stockMateriaal;
	}
}