package be.gim.tov.osyris.model.traject;

import java.util.List;

import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;

import be.gim.commons.resource.ResourceKey;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class NetwerkLus extends Traject {

	// VARIABLES
	@Label("Actief")
	@Description("Actief")
	private boolean actief;

	@Label("Netwerksegmenten")
	@Description("Netwerksegmenten")
	@ModelClassName("NetwerkSegment")
	private List<ResourceKey> segmenten;

	// GETTERS AND SETTERS
	public boolean isActief() {
		return actief;
	}

	public void setActief(boolean actief) {
		this.actief = actief;
	}

	public List<ResourceKey> getSegmenten() {
		return segmenten;
	}

	public void setSegmenten(List<ResourceKey> segmenten) {
		this.segmenten = segmenten;
	}
}