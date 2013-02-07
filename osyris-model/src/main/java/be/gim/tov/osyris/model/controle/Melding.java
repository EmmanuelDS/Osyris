package be.gim.tov.osyris.model.controle;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class Melding extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String status;
	private String voornaam;
	private String naam;
	private String email;
	private String telefoon;
	private Date datumVaststelling;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGemeld;
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;
	@ModelClassName("Traject")
	private ResourceIdentifier traject;
	@ModelClassName("User")
	private ResourceIdentifier medewerker;
	private Probleem probleem;

	// GETTERS AND SETTERS
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVoornaam() {
		return voornaam;
	}

	public void setVoornaam(String voornaam) {
		this.voornaam = voornaam;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefoon() {
		return telefoon;
	}

	public void setTelefoon(String telefoon) {
		this.telefoon = telefoon;
	}

	public Date getDatumVaststelling() {
		return datumVaststelling;
	}

	public void setDatumVaststelling(Date datumVaststelling) {
		this.datumVaststelling = datumVaststelling;
	}

	public Date getDatumGemeld() {
		return datumGemeld;
	}

	public void setDatumGemeld(Date datumGemeld) {
		this.datumGemeld = datumGemeld;
	}

	public Date getDatumGevalideerd() {
		return datumGevalideerd;
	}

	public void setDatumGevalideerd(Date datumGevalideerd) {
		this.datumGevalideerd = datumGevalideerd;
	}

	public ResourceIdentifier getTraject() {
		return traject;
	}

	public void setTraject(ResourceIdentifier traject) {
		this.traject = traject;
	}

	public ResourceIdentifier getMedewerker() {
		return medewerker;
	}

	public void setMedewerker(ResourceIdentifier medewerker) {
		this.medewerker = medewerker;
	}

	public Probleem getProbleem() {
		return probleem;
	}

	public void setProbleem(Probleem probleem) {
		this.probleem = probleem;
	}
}