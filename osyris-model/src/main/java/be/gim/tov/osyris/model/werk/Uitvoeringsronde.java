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
public class Uitvoeringsronde extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	@Label("Status")
	@Description("Status")
	private String status;

	@Label("Datum uitvoering")
	@Description("Datum uitvoering")
	@Type(ModelPropertyType.DATE)
	private Date datumUitvoering;

	@Label("Werkopdrachten")
	@Description("Werkopdrachten")
	@ModelClassName("WerkOpdracht")
	@Type(value = ModelPropertyType.RESOURCE_IDENTIFIER)
	private List<ResourceIdentifier> opdrachten;

	// GETTERS AND SETTERS
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
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
}