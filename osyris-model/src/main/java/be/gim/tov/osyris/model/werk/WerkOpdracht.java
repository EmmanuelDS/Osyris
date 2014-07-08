package be.gim.tov.osyris.model.werk;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.ContentType;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.FileSize;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.Target;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.api.model.annotation.View;
import org.conscientia.api.model.annotation.Width;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
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
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.canonicalBoolean}")
	private String inRonde;

	@ModelClassName("User")
	@Search(type = "menu:equals")
	@Edit(type = "menu")
	@ValuesExpression("#{osyrisModelFunctions.getSuggestions('Uitvoerder')}")
	@Target("_blank")
	private ResourceIdentifier uitvoerder;

	@Edit(type = "table")
	@View(type = "table")
	private List<WerkHandeling> handelingen;

	@ModelClassName("User")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getSuggestions('Medewerker')}")
	@Target("_blank")
	private ResourceIdentifier medewerker;

	@NotEditable
	private WerkopdrachtStatus status;

	private ValidatieStatus validatie;

	@NotSearchable
	@Type(value = ModelPropertyType.TEXT)
	private String commentaarMedewerker;

	@NotSearchable
	@Type(value = ModelPropertyType.TEXT)
	private String commentaarUitvoerder;

	@NotSearchable
	@Type(value = ModelPropertyType.TEXT)
	private String commentaarValidatie;

	@View(type = "image")
	@NotSearchable
	@ContentType("image/*")
	@FileSize(2 * 1024 * 1024)
	@Width(350)
	private byte[] foto;

	@View(type = "image")
	@NotSearchable
	@ContentType("image/*")
	@FileSize(2 * 1024 * 1024)
	@Width(350)
	private byte[] foto2;

	@View(type = "image")
	@NotSearchable
	@ContentType("image/*")
	@FileSize(2 * 1024 * 1024)
	@Width(350)
	private byte[] foto3;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGeannuleerd;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGerapporteerd;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumGevalideerd;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumTeControleren;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.TIMESTAMP)
	private Date datumUitTeVoeren;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.DATE)
	private Date datumLaterUitTeVoeren;

	@NotViewable
	@NotSearchable
	private Probleem probleem;

	@NotSearchable
	@NotEditable
	// @NotViewable
	@ModelClassName("Traject")
	@Target("_blank")
	private ResourceIdentifier traject;

	@NotSearchable
	@Edit(type = "table")
	@NotViewable
	private List<GebruiktMateriaal> materialen;

	// @NotViewable
	@NotSearchable
	@NotEditable
	private String trajectType;

	// @NotViewable
	@NotSearchable
	@NotEditable
	@Target("_blank")
	private ResourceIdentifier regioId;

	// @NotViewable
	@NotSearchable
	@NotEditable
	private String gemeente;

	// @NotViewable
	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.DATE)
	private Date datumLaatsteWijziging;

	// GETTERS AND SETTERS
	@Override
	@NotSearchable
	@NotEditable
	public Long getId() {
		return (Long) super.getId();
	}

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

	public String getCommentaarValidatie() {
		return commentaarValidatie;
	}

	public void setCommentaarValidatie(String commentaarValidatie) {
		this.commentaarValidatie = commentaarValidatie;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	public byte[] getFoto2() {
		return foto2;
	}

	public void setFoto2(byte[] foto2) {
		this.foto2 = foto2;
	}

	public byte[] getFoto3() {
		return foto3;
	}

	public void setFoto3(byte[] foto3) {
		this.foto3 = foto3;
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

	public String getGemeente() {
		return gemeente;
	}

	public void setGemeente(String gemeente) {
		this.gemeente = gemeente;
	}

	public Date getDatumLaatsteWijziging() {
		return datumLaatsteWijziging;
	}

	public void setDatumLaatsteWijziging(Date datumLaatsteWijziging) {
		this.datumLaatsteWijziging = datumLaatsteWijziging;
	}
}