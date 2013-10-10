package be.gim.tov.osyris.model.traject;

import java.util.List;

import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.api.model.annotation.NotEditable;
import org.conscientia.api.model.annotation.NotSearchable;
import org.conscientia.api.model.annotation.NotViewable;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class NetwerkLus extends Traject {

	// VARIABLES
	@NotSearchable
	private boolean actief;

	@NotSearchable
	@NotViewable
	@NotEditable
	private RichtingEnum richting;

	@NotSearchable
	@ModelClassName("Traject")
	private List<ResourceIdentifier> segmenten;

	// GETTERS AND SETTERS
	public boolean isActief() {
		return actief;
	}

	public void setActief(boolean actief) {
		this.actief = actief;
	}

	public RichtingEnum getRichting() {
		return richting;
	}

	public void setRichting(RichtingEnum richting) {
		this.richting = richting;
	}

	public List<ResourceIdentifier> getSegmenten() {
		return segmenten;
	}

	public void setSegmenten(List<ResourceIdentifier> segmenten) {
		this.segmenten = segmenten;
	}
}