package be.gim.tov.osyris.model.werk;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.api.model.annotation.View;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.bean.Beans;
import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;
import be.gim.tov.osyris.model.controle.Probleem;
import be.gim.tov.osyris.model.werk.status.ValidatieStatus;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@Label("Werkopdracht")
@Permissions({
		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),
		@Permission(profile = "group:Routedokter", action = "delete", allow = true),

		@Permission(profile = "group:Medewerker", action = "search", allow = true),
		@Permission(profile = "group:Medewerker", action = "view", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true),

		@Permission(profile = "group:Uitvoerder", action = "search", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "view", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "edit", allow = true) })
public class WerkOpdracht extends AbstractModelObject implements StorableObject {

	// VARIABLES
	@NotEditable
	@Label("In ronde")
	@Description("in ronde")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String inRonde;

	@Label("Uitvoerder")
	@Description("Uitvoerder")
	@ModelClassName("User")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getSuggestions('Uitvoerder')}")
	private ResourceIdentifier uitvoerder;

	@Label("Handelingen")
	@Description("Handelingen")
	@Edit(type = "table")
	@View(type = "table")
	private List<WerkHandeling> handelingen;

	@Label("Medewerker")
	@Description("Medewerker")
	@ModelClassName("User")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getSuggestions('Medewerker')}")
	private ResourceIdentifier medewerker;

	@NotEditable
	@Label("Status")
	@Description("Status")
	private WerkopdrachtStatus status;

	@Label("Validatie")
	@Description("Validatie")
	private ValidatieStatus validatie;

	@NotSearchable
	@Label("Commentaar medewerker")
	@Description("Commentaar medewerker")
	@Type(value = ModelPropertyType.TEXT)
	private String commentaarMedewerker;

	@NotSearchable
	@Label("Commentaar uitvoerder")
	@Description("Commentaar uitvoerder")
	@Type(value = ModelPropertyType.TEXT)
	private String commentaarUitvoerder;

	@NotSearchable
	@Label("Foto")
	@Description("Foto")
	private String foto;

	@NotSearchable
	@NotEditable
	@Label("Datum geannuleerd")
	@Description("Datum geannuleerd")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGeannuleerd;

	@NotSearchable
	@NotEditable
	@Label("Datum gerapporteerd")
	@Description("Datum gerapporteerd")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGerapporteerd;

	@NotSearchable
	@NotEditable
	@Label("Datum gevalideerd")
	@Description("Datum gevalideerd")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;

	@NotSearchable
	@NotEditable
	@Label("Datum te controleren")
	@Description("Datum te controleren")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumTeControleren;

	@NotSearchable
	@NotEditable
	@Label("Datum uit te voeren")
	@Description("Datum uit te voeren")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumUitTeVoeren;

	@NotSearchable
	@NotEditable
	@Label("Datum later uit te voeren")
	@Description("Datum later uit te voeren")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumLaterUitTeVoeren;

	@NotSearchable
	@Label("Probleem")
	@Description("Probleem")
	private Probleem probleem;

	@NotSearchable
	@NotEditable
	@NotViewable
	@Label("Traject")
	@Description("Traject")
	@ModelClassName("Traject")
	private ResourceIdentifier traject;

	@NotSearchable
	@Label("Gebruikte materialen")
	@Description("Gebruikte materialen")
	@Edit(type = "table")
	@View(type = "table")
	private List<GebruiktMateriaal> materialen;

	// GETTERS AND SETTERS
	public String getInRonde() {
		return inRonde;
	}

	public void setInRonde(String inRonde) {
		this.inRonde = inRonde;
	}

	public WerkopdrachtStatus getStatus() {
		return status;
	}

	public void setStatus(WerkopdrachtStatus status) {
		this.status = status;
	}

	public ValidatieStatus getValidatie() {
		return validatie;
	}

	public void setValidatie(ValidatieStatus validatie) {
		this.validatie = validatie;
	}

	public String getCommentaarMedewerker() {
		return commentaarMedewerker;
	}

	public void setCommentaarMedewerker(String commentaarMedewerker) {
		this.commentaarMedewerker = commentaarMedewerker;
	}

	public String getCommentaarUitvoerder() {
		return commentaarUitvoerder;
	}

	public void setCommentaarUitvoerder(String commentaarUitvoerder) {
		this.commentaarUitvoerder = commentaarUitvoerder;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public Date getDatumGeannuleerd() {
		return datumGeannuleerd;
	}

	public void setDatumGeannuleerd(Date datumGeannuleerd) {
		this.datumGeannuleerd = datumGeannuleerd;
	}

	public Date getDatumGerapporteerd() {
		return datumGerapporteerd;
	}

	public void setDatumGerapporteerd(Date datumGerapporteerd) {
		this.datumGerapporteerd = datumGerapporteerd;
	}

	public Date getDatumGevalideerd() {
		return datumGevalideerd;
	}

	public void setDatumGevalideerd(Date datumGevalideerd) {
		this.datumGevalideerd = datumGevalideerd;
	}

	public Date getDatumTeControleren() {
		return datumTeControleren;
	}

	public void setDatumTeControleren(Date datumTeControleren) {
		this.datumTeControleren = datumTeControleren;
	}

	public Date getDatumUitTeVoeren() {
		return datumUitTeVoeren;
	}

	public void setDatumUitTeVoeren(Date datumUitTeVoeren) {
		this.datumUitTeVoeren = datumUitTeVoeren;
	}

	public Date getDatumLaterUitTeVoeren() {
		return datumLaterUitTeVoeren;
	}

	public void setDatumLaterUitTeVoeren(Date datumLaterUitTeVoeren) {
		this.datumLaterUitTeVoeren = datumLaterUitTeVoeren;
	}

	public ResourceIdentifier getMedewerker() {
		return medewerker;
	}

	public void setMedewerker(ResourceIdentifier medewerker) {
		this.medewerker = medewerker;
	}

	public ResourceIdentifier getUitvoerder() {
		return uitvoerder;
	}

	public void setUitvoerder(ResourceIdentifier uitvoerder) {
		this.uitvoerder = uitvoerder;
	}

	public Probleem getProbleem() {
		return probleem;
	}

	public void setProbleem(Probleem probleem) {
		this.probleem = probleem;
	}

	public ResourceIdentifier getTraject() {
		return traject;
	}

	public void setTraject(ResourceIdentifier traject) {
		this.traject = traject;
	}

	public List<GebruiktMateriaal> getMaterialen() {
		return materialen;
	}

	public void setMaterialen(List<GebruiktMateriaal> materialen) {
		this.materialen = materialen;
	}

	public List<WerkHandeling> getHandelingen() {
		return handelingen;
	}

	public void setHandelingen(List<WerkHandeling> handelingen) {
		this.handelingen = handelingen;
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

	@Transient
	@NotViewable
	@NotSearchable
	@NotEditable
	@Label("Gemeente")
	public String getGemeente() {
		return Beans.getReference(OsyrisModelFunctions.class)
				.getWerkOpdrachtGemeente(this.probleem);
	}
}