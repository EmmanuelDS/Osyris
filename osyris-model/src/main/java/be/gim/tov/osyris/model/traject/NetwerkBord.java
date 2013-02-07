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
public abstract class NetwerkBord extends Bord {

	// VARIABLES
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpnr0;
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpnr1;
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpnr2;
	@ModelClassName("NetwerkKnooppunt")
	private ResourceIdentifier kpnr3;

	// GETTERS AND SETTERS
	public ResourceIdentifier getKpnr0() {
		return kpnr0;
	}

	public void setKpnr0(ResourceIdentifier kpnr0) {
		this.kpnr0 = kpnr0;
	}

	public ResourceIdentifier getKpnr1() {
		return kpnr1;
	}

	public void setKpnr1(ResourceIdentifier kpnr1) {
		this.kpnr1 = kpnr1;
	}

	public ResourceIdentifier getKpnr2() {
		return kpnr2;
	}

	public void setKpnr2(ResourceIdentifier kpnr2) {
		this.kpnr2 = kpnr2;
	}

	public ResourceIdentifier getKpnr3() {
		return kpnr3;
	}

	public void setKpnr3(ResourceIdentifier kpnr3) {
		this.kpnr3 = kpnr3;
	}
}