package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelClassName;
import org.conscientia.api.model.annotation.ModelStore;

import be.gim.commons.resource.ResourceIdentifier;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public abstract class NetwerkSegment extends Traject {

	// VARIABLES
	private boolean enkelRichting;
	@ModelClassName("NetwerkBord")
	private ResourceIdentifier segment;
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier vanKnooppunt;
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier naarKnooppunt;

	// GETTERS AND SETTERS
	public boolean isEnkelRichting() {
		return enkelRichting;
	}

	public void setEnkelRichting(boolean enkelRichting) {
		this.enkelRichting = enkelRichting;
	}

	public ResourceIdentifier getSegment() {
		return segment;
	}

	public void setSegment(ResourceIdentifier segment) {
		this.segment = segment;
	}

	public ResourceIdentifier getVanKnooppunt() {
		return vanKnooppunt;
	}

	public void setVanKnooppunt(ResourceIdentifier vanKnooppunt) {
		this.vanKnooppunt = vanKnooppunt;
	}

	public ResourceIdentifier getNaarKnooppunt() {
		return naarKnooppunt;
	}

	public void setNaarKnooppunt(ResourceIdentifier naarKnooppunt) {
		this.naarKnooppunt = naarKnooppunt;
	}
}