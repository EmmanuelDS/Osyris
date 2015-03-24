package be.gim.tov.osyris.model.werk;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.tov.osyris.model.werk.status.ActieStockStatus;

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
	private StockMateriaal stockMateriaal;

	private int aantal;

	@NotSearchable
	private ActieStockStatus actieStock;

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

	public ActieStockStatus getActieStock() {
		return actieStock;
	}

	public void setActieStock(ActieStockStatus actieStock) {
		this.actieStock = actieStock;
	}
}