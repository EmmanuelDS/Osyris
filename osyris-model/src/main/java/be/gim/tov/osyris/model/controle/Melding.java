package be.gim.tov.osyris.model.controle;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
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
	@Label("Status")
	@Description("Status")
	private String status;

	@Label("Voornaam")
	@Description("Voornaam")
	private String voornaam;

	@Label("Naam")
	@Description("Naam")
	private String naam;

	@Label("Email")
	@Description("Email")
	private String email;

	@Label("Telefoon")
	@Description("Telefoon")
	private String telefoon;

	@Label("Datum vaststelling")
	@Description("Datum vaststelling")
	private Date datumVaststelling;

	@Label("Datum gemeld")
	@Description("Datum gemeld")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGemeld;

	@Label("Datum gevalideerd")
	@Description("Peter/Meter")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;

	@Label("Traject")
	@Description("Traject")
	@ModelClassName("Traject")
	private ResourceIdentifier traject;

	@Label("Medewerker")
	@Description("Medewerker")
	@ModelClassName("User")
	private ResourceIdentifier medewerker;

	@Label("Probleem")
	@Description("Probleem")
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