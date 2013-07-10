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
import org.conscientia.api.model.annotation.Required;
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
@Label("Uitvoeringsronde")
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
		@Permission(profile = "group:Uitvoerder", action = "edit", allow = true) })
public class Uitvoeringsronde extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Status")
	@Description("Status")
	private UitvoeringsrondeStatus status;

	@Label("Datum uitvoering")
	@Description("Datum uitvoering")
	@Type(ModelPropertyType.DATE)
	private Date datumUitvoering;

	@Required
	@NotSearchable
	@Label("Afgelegde afstand")
	@Description("Afgelegde afstand")
	private int afstand;

	@NotSearchable
	@Label("Werkopdrachten")
	@Description("Werkopdrachten")
	@ModelClassName("WerkOpdracht")
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