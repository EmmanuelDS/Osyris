package be.gim.tov.osyris.model.codes;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.core.model.AbstractModelObject;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class NetwerkKnooppuntCode extends AbstractModelObject implements
		StorableObject {

	// VARIABLES
	private ResourceIdentifier code;
	private String label;
	private ResourceIdentifier regio;
	private Integer nummer;

	// GETTERS AND SETTERS
	public ResourceIdentifier getCode() {
		return code;
	}

	public void setCode(ResourceIdentifier code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ResourceIdentifier getRegio() {
		return regio;
	}

	public void setRegio(ResourceIdentifier regio) {
		this.regio = regio;
	}

	public Integer getNummer() {
		return nummer;
	}

	public void setNummer(Integer nummer) {
		this.nummer = nummer;
	}
}
