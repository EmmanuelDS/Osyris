package be.gim.tov.osyris.model.werk;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.ModelPropertyType;
import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;
import org.conscientia.api.model.annotation.Permission;
import org.conscientia.api.model.annotation.Permissions;
import org.conscientia.api.model.annotation.Target;
import org.conscientia.api.model.annotation.Type;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;
import be.gim.tov.osyris.model.werk.status.UitvoeringsrondeStatus;

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

		@Permission(profile = "group:PeterMeter", action = "search", allow = false),

		@Permission(profile = "group:Uitvoerder", action = "search", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "view", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "edit", allow = true),
		@Permission(profile = "group:Uitvoerder", action = "delete", allow = true) })
public class Uitvoeringsronde extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@NotEditable
	private UitvoeringsrondeStatus status;

	@Type(ModelPropertyType.DATE)
	private Date datumUitvoering;

	@NotSearchable
	@Type(value = ModelPropertyType.TEXT)
	private String omschrijving;

	@NotSearchable
	private int afstand;

	@NotSearchable
	@NotEditable
	@NotViewable
	@Target("_blank")
	private ResourceIdentifier uitvoerder;

	@NotSearchable
	@ModelClassName("WerkOpdracht")
	@Target("_blank")
	private List<ResourceIdentifier> opdrachten;

	// GETTERS AND SETTERS
	public UitvoeringsrondeStatus getStatus() {
		return status;
	}

	public void setStatus(UitvoeringsrondeStatus status) {
		this.status = status;
	}

	public Date getDatumUitvoering() {
		return datumUitvoering;
	}

	public void setDatumUitvoering(Date datumUitvoering) {
		this.datumUitvoering = datumUitvoering;
	}

	public String getOmschrijving() {
		return omschrijving;
	}

	public void setOmschrijving(String omschrijving) {
		this.omschrijving = omschrijving;
	}

	public ResourceIdentifier getUitvoerder() {
		return uitvoerder;
	}

	public void setUitvoerder(ResourceIdentifier uitvoerder) {
		this.uitvoerder = uitvoerder;
	}

	public List<ResourceIdentifier> getOpdrachten() {
		return opdrachten;
	}

	public void setOpdrachten(List<ResourceIdentifier> opdrachten) {
		this.opdrachten = opdrachten;
	}

	public int getAfstand() {
		return afstand;
	}

	public void setAfstand(int afstand) {
		this.afstand = afstand;
	}
}