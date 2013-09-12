package be.gim.tov.osyris.model.user;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.LabelProperty;
import org.conscientia.api.model.annotation.Length;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Pattern;
import org.conscientia.core.model.AbstractModelObject;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class UitvoerderBedrijf extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	private String adres;

	private String contactPersoon;

	@Length(min = 3, max = 128)
	@Pattern("[\\w%\\.\\+\\-]+@[\\w%\\.\\+\\-]+\\.[a-zA-Z0-9]{2,4}")
	private String email;

	@Length(min = 3, max = 30)
	@Pattern("(\\+?[\\d]+)?")
	private String fax;

	private String gemeente;

	@LabelProperty
	private String naam;

	private String postcode;

	@Length(min = 3, max = 30)
	@Pattern("(\\+?[\\d]+)?")
	private String telefoon;

	// GETTERS AND SETTERS
	public String getAdres() {
		return adres;
	}

	public void setAdres(String adres) {
		this.adres = adres;
	}

	public String getContactPersoon() {
		return contactPersoon;
	}

	public void setContactPersoon(String contactPersoon) {
		this.contactPersoon = contactPersoon;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getGemeente() {
		return gemeente;
	}

	public void setGemeente(String gemeente) {
		this.gemeente = gemeente;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getTelefoon() {
		return telefoon;
	}

	public void setTelefoon(String telefoon) {
		this.telefoon = telefoon;
	}
}