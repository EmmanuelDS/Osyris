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
public class StockMateriaal extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String magazijn;
	private String categorie;
	private String type;
	private String nummer;
	private int inStock;
	private int min;
	private int max;
	private int teBestellen;
	private boolean besteld;

	// GETTERS AND SETTERS
	public String getMagazijn() {
		return magazijn;
	}

	public void setMagazijn(String magazijn) {
		this.magazijn = magazijn;
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNummer() {
		return nummer;
	}

	public void setNummer(String nummer) {
		this.nummer = nummer;
	}

	public int getInStock() {
		return inStock;
	}

	public void setInStock(int inStock) {
		this.inStock = inStock;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getTeBestellen() {
		return teBestellen;
	}

	public void setTeBestellen(int teBestellen) {
		this.teBestellen = teBestellen;
	}

	public boolean isBesteld() {
		return besteld;
	}

	public void setBesteld(boolean besteld) {
		this.besteld = besteld;
	}
}