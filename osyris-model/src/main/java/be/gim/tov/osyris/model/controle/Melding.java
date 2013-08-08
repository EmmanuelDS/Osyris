package be.gim.tov.osyris.model.controle;

import java.util.Date;

import javax.persistence.Transient;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Label;
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

import be.gim.commons.bean.Beans;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.annotation.EditableInStatus;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
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
@Label("Melding")
@Edit(type = "melding")
public class Melding extends AbstractModelObject implements StorableObject {

	// VARIABLES
	@NotSearchable
	@NotEditable
	@EditableInStatus("")
	@Label("Traject")
	@Description("Traject")
	@ModelClassName("Traject")
	private ResourceIdentifier traject;

	@NotEditable
	@Label("Status")
	@Description("Status")
	private MeldingStatus status;

	@EditableInStatus("")
	@NotSearchable
	@Label("Voornaam")
	@Description("Voornaam")
	private String voornaam;

	@EditableInStatus("")
	@NotSearchable
	@Label("Naam")
	@Description("Naam")
	private String naam;

	@Required
	@EditableInStatus("")
	@NotSearchable
	@Label("Email")
	@Description("Email")
	@Length(min = 3, max = 128)
	@Pattern("[\\w%\\.\\+\\-]+@[\\w%\\.\\+\\-]+\\.[a-zA-Z0-9]{2,4}")
	private String email;

	@EditableInStatus("")
	@NotSearchable
	@Label("Telefoon")
	@Description("Telefoon")
	@Length(min = 3, max = 30)
	@Pattern("(\\+?[\\d]+)?")
	private String telefoon;

	@Label("Medewerker")
	@Description("Medewerker")
	@ModelClassName("User")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getSuggestions('Medewerker')}")
	private ResourceIdentifier medewerker;

	@NotSearchable
	@EditableInStatus("")
	@Label("Datum vaststelling")
	@Description("Datum vaststelling")
	@Type(ModelPropertyType.DATE)
	private Date datumVaststelling;

	@NotSearchable
	@NotEditable
	@Label("Datum gemeld")
	@Description("Datum gemeld")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGemeld;

	@NotSearchable
	@NotEditable
	@Label("Datum gevalideerd")
	@Description("Peter/Meter")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;

	@EditableInStatus({ "", "GEMELD" })
	@Required
	@NotSearchable
	@Label("Probleem")
	@Description("Probleem")
	private Probleem probleem;

	@EditableInStatus({ "GEMELD", "GEVALIDEERD" })
	@NotSearchable
	@Label("Commentaar TOV")
	@Description("Commentaar TOV")
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

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

	@Transient
	@NotViewable
	@NotSearchable
	@NotEditable
	@Label("Datum laatste statuswijziging")
	public Date getDatumLaatsteWijziging() {
		return Beans.getReference(OsyrisModelFunctions.class)
				.getDatumLaatsteWijziging(this);
	}

	@Transient
	@NotViewable
	@NotSearchable
	@NotEditable
	@Label("TrajectType")
	public String getTrajectType() {
		return Beans.getReference(OsyrisModelFunctions.class).getTrajectType(
				this.traject);
	}

	@Transient
	@NotViewable
	@NotSearchable
	@NotEditable
	@Label("Regio")
	public String getRegio() {
		return Beans.getReference(OsyrisModelFunctions.class).getTrajectRegio(
				this.traject);
	}
}