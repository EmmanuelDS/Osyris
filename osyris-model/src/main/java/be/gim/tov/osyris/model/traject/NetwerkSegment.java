package be.gim.tov.osyris.model.traject;

import org.conscientia.api.model.annotation.Description;
import org.conscientia.api.model.annotation.Label;
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
	@Label("Enkele richting")
	@Description("Enkele richting")
	private boolean enkelRichting;

	@Label("Van knooppunt")
	@Description("van knooppunt")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier vanKnooppunt;

	@Label("Naar knooppunt")
	@Description("Naar knooppunt")
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier naarKnooppunt;

	// GETTERS AND SETTERS
	public boolean isEnkelRichting() {
		return enkelRichting;
	}

	public void setEnkelRichting(boolean enkelRichting) {
		this.enkelRichting = enkelRichting;
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