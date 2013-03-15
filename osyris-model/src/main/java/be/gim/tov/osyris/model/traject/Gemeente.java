package be.gim.tov.osyris.model.traject;

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
public class Gemeente extends AbstractModelObject {

	// VARIABLES
	@Label("Naam")
	@Description("Naam")
	private String naam;

	@Label("NIS code")
	@Description("NIS code")
	private String niscode;

	@Label("EU code")
	@Description("EU code")
	private String eucode;

	@Label("Perimeter")
	@Description("Perimeter")
	private float perimeter;

	// GETTERS AND SETTERS
	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getNiscode() {
		return niscode;
	}

	public void setNiscode(String niscode) {
		this.niscode = niscode;
	}

	public String getEucode() {
		return eucode;
	}

	public void setEucode(String eucode) {
		this.eucode = eucode;
	}

	public float getPerimeter() {
		return perimeter;
	}

	public void setPerimeter(float perimeter) {
		this.perimeter = perimeter;
	}
}