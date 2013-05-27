package be.gim.tov.osyris.model.controle;

import static org.conscientia.api.model.SubClassPersistence.UNION;

import java.util.Date;
import java.util.List;

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
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.SubClassPersistence;
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
@Permissions({
		@Permission(profile = "group:Medewerker", action = "search", allow = true),
		@Permission(profile = "group:Medewerker", action = "create", allow = true),
		@Permission(profile = "group:Medewerker", action = "edit", allow = true),
		@Permission(profile = "group:Medewerker", action = "view", allow = true),

		@Permission(profile = "group:Routedokter", action = "search", allow = true),
		@Permission(profile = "group:Routedokter", action = "view", allow = true),
		@Permission(profile = "group:Routedokter", action = "create", allow = true),
		@Permission(profile = "group:Routedokter", action = "edit", allow = true),
		@Permission(profile = "group:Routedokter", action = "delete", allow = true),

		@Permission(profile = "group:PeterMeter", action = "search", allow = true) })
public abstract class ControleOpdracht extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Label("Periode")
	@Type(value = ModelPropertyType.ENUM)
	@ValuesExpression("#{osyrisModelFunctions.getCodeList('PeriodeCode')}")
	private String periode;

	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Label("Status")
	@Description("Status")
	private ControleOpdrachtStatus status;

	@Label("Commentaar")
	@Description("Commentaar")
	@NotSearchable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Type(value = ModelPropertyType.TEXT)
	private String commentaar;

	@Label("Datum gerapporteerd")
	@Description("Datum gerapporteerd")
	@Type(ModelPropertyType.DATE)
	@NotEditable
	private Date datumGerapporteerd;

	@Label("Datum gevalideerd")
	@Description("Datum gevalideerd")
	@Type(ModelPropertyType.DATE)
	@NotEditable
	private Date datumGevalideerd;

	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Label("Datum te controleren")
	@Description("Datum te controleren")
	@Type(ModelPropertyType.DATE)
	private Date datumTeControleren;

	@NotEditable
	@Label("Datum uitgesteld")
	@Description("Datum uitgesteld")
	@Type(ModelPropertyType.DATE)
	private Date datumUitgesteld;

	@NotEditable
	@EditableInGroup({ "PeterMeter" })
	@Label("Datum uitvoering")
	@Description("Datum uitvoering")
	@Type(ModelPropertyType.DATE)
	private Date datumUitTeVoeren;

	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Label("Traject")
	@Description("Traject")
	@ModelClassName("Traject")
	private ResourceIdentifier traject;

	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Label("Medewerker")
	@Description("Medewerker")
	@ModelClassName("User")
	private ResourceIdentifier medewerker;

	@NotEditable
	@EditableInGroup({ "Medewerker", "Routedokter" })
	@Label("Peter/Meter")
	@Description("Peter/Meter")
	@ModelClassName("User")
	private ResourceIdentifier peterMeter;

	@NotEditable
	@EditableInGroup({ "PeterMeter" })
	@Label("Problemen")
	@Description("Problemen")
	@NotSearchable
	private List<Probleem> problemen;

	// GETTERS AND SETTERS
	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
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

	public Date getDatumUitgesteld() {
		return datumUitgesteld;
	}

	public void setDatumUitgesteld(Date datumUitgesteld) {
		this.datumUitgesteld = datumUitgesteld;
	}

	public Date getDatumUitTeVoeren() {
		return datumUitTeVoeren;
	}

	public void setDatumUitTeVoeren(Date datumUitTeVoeren) {
		this.datumUitTeVoeren = datumUitTeVoeren;
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
}