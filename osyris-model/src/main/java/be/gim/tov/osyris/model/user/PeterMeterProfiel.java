package be.gim.tov.osyris.model.user;

import java.util.Date;
import java.util.List;

import org.conscientia.api.model.StorableObject;
import org.conscientia.api.model.annotation.Model;
import org.conscientia.api.model.annotation.ModelStore;
import org.conscientia.core.model.AbstractModelObject;

/**
 * 
 * @author kristof
 * 
 */
@Model
@ModelStore("OsyrisDataStore")
public class PeterMeterProfiel extends AbstractModelObject implements StorableObject {

	// VARIABLES
	private String status;
	private Date actiefSinds;
	private Date actiefTot;
	private List<PeterMeterVoorkeur> voorkeuren;

	// GETTERS AND SETTERS
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getActiefSinds() {
		return actiefSinds;
	}

	public void setActiefSinds(Date actiefSinds) {
		this.actiefSinds = actiefSinds;
	}

	public Date getActiefTot() {
		return actiefTot;
	}

	public void setActiefTot(Date actiefTot) {
		this.actiefTot = actiefTot;
	}

	public List<PeterMeterVoorkeur> getVoorkeuren() {
		return voorkeuren;
	}

	public void setVoorkeuren(List<PeterMeterVoorkeur> voorkeuren) {
		this.voorkeuren = voorkeuren;
	}
}