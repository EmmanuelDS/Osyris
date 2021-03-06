package be.gim.tov.osyris.model.controle;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Edit;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Search;
import org.conscientia.api.model.annotation.SubClassPersistence;
import org.conscientia.api.model.annotation.Target;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.api.model.annotation.ValuesExpression;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.annotation.EditableInGroup;
import be.gim.tov.osyris.model.controle.status.ControleOpdrachtStatus;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
@SubClassPersistence(UNION)
@Edit(type = "controleOpdracht")
@Search(type = "controleOpdracht")
@Permissions({
		@Permission(profile = "group:Medewerker", action = "search", allow = true),
		@Permission(profile = "group:Medewerker", action = "create", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true),
		@Permission(profile = "group:Medewerker", action = "view", allow = true),
		@Permission(profile = "group:Medewerker", action = "delete", allow = true),

		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),
		@Permission(profile = "group:Routedokter", action = "delete", allow = true),

		@Permission(profile = "group:PeterMeter", action = "search", allow = true) })
public abstract class ControleOpdracht extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PeriodeCode')}")
	private String periode;

	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getJaren()}")
	private String jaar;

	private ControleOpdrachtStatus status;

	@NotSearchable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

	@NotSearchable
	@Type(ModelPropertyType.DATE)
	@NotEditable
	private Date datumGerapporteerd;

	@NotSearchable
	@Type(ModelPropertyType.DATE)
	@NotEditable
	private Date datumGevalideerd;

	@NotSearchable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Type(ModelPropertyType.DATE)
	private Date datumTeControleren;

	@NotSearchable
	@NotEditable
	@Type(ModelPropertyType.DATE)
	private Date datumUitTeVoeren;

	@NotSearchable
	@EditableInGroup({ "PeterMeter" })
	@Type(ModelPropertyType.DATE)
	private Date datumTerreinBezoek;

	@NotSearchable
	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@ModelClassName("Traject")
	@Target("_blank")
	private ResourceIdentifier traject;

	@EditableInGroup({ "Medewerker", "Routedokter" })
	@ModelClassName("User")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getVerantwoordelijkenTOV()}")
	@Target("_blank")
	private ResourceIdentifier medewerker;

	@EditableInGroup({ "Medewerker", "Routedokter" })
	@ModelClassName("User")
	@Edit(type = "menu")
	@Search(type = "menu:equals")
	@ValuesExpression("#{osyrisModelFunctions.getPeterMeterNaamCodes(false)}")
	@Target("_blank")
	private ResourceIdentifier peterMeter;

	@NotViewable
	@EditableInGroup({ "PeterMeter" })
	@NotSearchable
	private List<Probleem> problemen;

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
	@Type(ModelPropertyType.DATE)
	private Date datumLaatsteWijziging;

	// GETTERS AND SETTERS
	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public String getJaar() {
		return jaar;
	}

	public void setJaar(String jaar) {
		this.jaar = jaar;
	}

	public ControleOpdrachtStatus getStatus() {
		return status;
	}

	public void setStatus(ControleOpdrachtStatus status) {
		this.status = status;
	}

	public String getCommentaar() {
		return commentaar;
	}

	public void setCommentaar(String commentaar) {
		this.commentaar = commentaar;
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

	public Date getDatumTerreinBezoek() {
		return datumTerreinBezoek;
	}

	public void setDatumTerreinBezoek(Date datumTerreinBezoek) {
		this.datumTerreinBezoek = datumTerreinBezoek;
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

	public ResourceIdentifier getPeterMeter() {
		return peterMeter;
	}

	public void setPeterMeter(ResourceIdentifier peterMeter) {
		this.peterMeter = peterMeter;
	}

	public List<Probleem> getProblemen() {
		return problemen;
	}

	public void setProblemen(List<Probleem> problemen) {
		this.problemen = problemen;
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