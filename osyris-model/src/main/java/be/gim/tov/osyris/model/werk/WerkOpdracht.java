package be.gim.tov.osyris.model.werk;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.controle.Probleem;
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
	@Label("Type werkopdracht")
	@Description("Type werkopdracht")
	private String type;

	@Label("Status")
	@Description("Status")
	private WerkopdrachtStatus status;

	@NotSearchable
	@Label("Beoordeling")
	@Description("Beoordeling")
	private String beoordeling;

	@NotSearchable
	@Label("Omschrijving opdracht")
	@Description("Omschrijving opdracht")
	private String omschrijvingOpdracht;

	@NotSearchable
	@Label("Commentaar medewerker")
	@Description("Commentaar medewerker")
	private String commentaarMedewerker;

	@NotSearchable
	@Label("Omschrijving uitvoering")
	@Description("Omschrijving uitvoering")
	private String omschrijvingUitvoering;

	@NotSearchable
	@Label("Commentaar uitvoerder")
	@Description("Commentaar uitvoerder")
	private String commentaarUitvoerder;

	@NotSearchable
	@Label("Foto")
	@Description("Foto")
	private String foto;

	@Label("Datum geannuleerd")
	@Description("Datum geannuleerd")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGeannuleerd;

	@Label("Datum gerapporteerd")
	@Description("Datum gerapporteerd")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGerapporteerd;

	@Label("Datum gevalideerd")
	@Description("Datum gevalideerd")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;

	@Label("Datum te controleren")
	@Description("Datum te controleren")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumTeControleren;

	@Label("Datum uit te voeren")
	@Description("Datum uit te voeren")
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumUitTeVoeren;

	@Label("Medewerker")
	@Description("Medewerker")
	@ModelClassName("User")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier medewerker;

	@Label("Uitvoerder")
	@Description("Uitvoerder")
	@ModelClassName("User")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private ResourceIdentifier uitvoerder;

	@NotSearchable
	@Label("Probleem")
	@Description("Probleem")
	private Probleem probleem;

	@NotSearchable
	@Label("Gebruikt materiaal")
	@Description("Gebruikt materiaal")
	private GebruiktMateriaal gebruiktMateriaal;

	@Label("Handelingen")
	@Description("Handelingen")
	private List<WerkHandeling> handelingen;

	// GETTERS AND SETTERS
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public WerkopdrachtStatus getStatus() {
		return status;
	}

	public void setStatus(WerkopdrachtStatus status) {
		this.status = status;
	}

	public String getBeoordeling() {
		return beoordeling;
	}

	public void setBeoordeling(String beoordeling) {
		this.beoordeling = beoordeling;
	}

	public String getOmschrijvingOpdracht() {
		return omschrijvingOpdracht;
	}

	public void setOmschrijvingOpdracht(String omschrijvingOpdracht) {
		this.omschrijvingOpdracht = omschrijvingOpdracht;
	}

	public String getCommentaarMedewerker() {
		return commentaarMedewerker;
	}

	public void setCommentaarMedewerker(String commentaarMedewerker) {
		this.commentaarMedewerker = commentaarMedewerker;
	}

	public String getOmschrijvingUitvoering() {
		return omschrijvingUitvoering;
	}

	public void setOmschrijvingUitvoering(String omschrijvingUitvoering) {
		this.omschrijvingUitvoering = omschrijvingUitvoering;
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

	public GebruiktMateriaal getGebruiktMateriaal() {
		return gebruiktMateriaal;
	}

	public void setGebruiktMateriaal(GebruiktMateriaal gebruiktMateriaal) {
		this.gebruiktMateriaal = gebruiktMateriaal;
	}

	public List<WerkHandeling> getHandelingen() {
		return handelingen;
	}

	public void setHandelingen(List<WerkHandeling> handelingen) {
		this.handelingen = handelingen;
	}
}