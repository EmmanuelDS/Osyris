package be.gim.tov.osyris.model.controle;

import java.util.Date;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Length;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Pattern;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Required;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.annotation.EditableInStatus;
import be.gim.tov.osyris.model.controle.status.MeldingStatus;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@Permissions({
		@Permission(profile = "group:Medewerker", action = "search", allow = true),
		@Permission(profile = "group:Medewerker", action = "view", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true),

		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true) })
@Edit(type = "melding")
public class Melding extends AbstractModelObject implements StorableObject {

	// VARIABLES
	@NotSearchable
	@NotEditable
	@EditableInStatus("")
	@ModelClassName("Traject")
	private ResourceIdentifier traject;

	@NotEditable
	private MeldingStatus status;

	@EditableInStatus("")
	@NotSearchable
	private String voornaam;

	@EditableInStatus("")
	@NotSearchable
	private String naam;

	@Required
	@EditableInStatus("")
	@NotSearchable
	@Length(min = 3, max = 128)
	@Pattern("[\\w%\\.\\+\\-]+@[\\w%\\.\\+\\-]+\\.[a-zA-Z0-9]{2,4}")
	private String email;

	@EditableInStatus("")
	@NotSearchable
	@Length(min = 3, max = 30)
	@Pattern("(\\+?[\\d]+)?")
	private String telefoon;

	@ModelClassName("User")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getSuggestions('Medewerker')}")
	private ResourceIdentifier medewerker;

	@NotSearchable
	@EditableInStatus("")
	@Type(ModelPropertyType.DATE)
	private Date datumVaststelling;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGemeld;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;

	@EditableInStatus({ "", "GEMELD" })
	@Required
	@NotSearchable
	private Probleem probleem;

	@EditableInStatus({ "GEMELD", "GEVALIDEERD" })
	@NotSearchable
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

	@NotViewable
	@NotSearchable
	@NotEditable
	private String trajectType;

	@NotViewable
	@NotSearchable
	@NotEditable
	private ResourceIdentifier regioId;

	@NotViewable
	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.DATE)
	private Date datumLaatsteWijziging;

	// GETTERS AND SETTERS
	public ResourceIdentifier getTraject() {
		return traject;
	}

	public void setTraject(ResourceIdentifier traject) {
		this.traject = traject;
	}

	public MeldingStatus getStatus() {
		return status;
	}

	public void setStatus(MeldingStatus status) {
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

	public ResourceIdentifier getMedewerker() {
		return medewerker;
	}

	public void setMedewerker(ResourceIdentifier medewerker) {
		this.medewerker = medewerker;
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

	public Probleem getProbleem() {
		return probleem;
	}

	public void setProbleem(Probleem probleem) {
		this.probleem = probleem;
	}

	public String getCommentaar() {
		return commentaar;
	}

	public void setCommentaar(String commentaar) {
		this.commentaar = commentaar;
	}

	public String getTrajectType() {
		return trajectType;
	}

	public void setTrajectType(String trajectType) {
		this.trajectType = trajectType;
	}

	public ResourceIdentifier getRegioId() {
		return regioId;
	}

	public void setRegioId(ResourceIdentifier regioId) {
		this.regioId = regioId;
	}

	public Date getDatumLaatsteWijziging() {
		return datumLaatsteWijziging;
	}

	public void setDatumLaatsteWijziging(Date datumLaatsteWijziging) {
		this.datumLaatsteWijziging = datumLaatsteWijziging;
	}
}