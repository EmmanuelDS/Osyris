package be.gim.tov.osyris.model.bean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

/**
 * 
 * @author kristof
 * 
 */
@Named
public class OsyrisBean {

	public List<Object[]> getStockMateriaalStates() {
		List<Object[]> stockMateriaalStates = new ArrayList<Object[]>();
		Object[] statusBesteld = { "1", "Besteld" };
		Object[] statusnietBesteld = { "0", "Niet besteld" };

		stockMateriaalStates.add(statusBesteld);
		stockMateriaalStates.add(statusnietBesteld);
		return stockMateriaalStates;
	}

	public List<Object[]> getPeterMeterProfileStates() {
		List<Object[]> profileStates = new ArrayList<Object[]>();
		Object[] statusActief = { "Actief", "Actief" };
		Object[] statusPassief = { "Passief", "Passief" };
		Object[] statusKandidaat = { "Kandidaat", "Kandidaat" };

		profileStates.add(statusActief);
		profileStates.add(statusPassief);
		profileStates.add(statusKandidaat);
		return profileStates;
	}

	public List<Object[]> getEnkeleRichting() {
		List<Object[]> enkeleRichting = new ArrayList<Object[]>();
		Object[] enkeleRichtingFalse = { "0", "Nee" };
		Object[] enkeleRichtingTrue = { "1", "Ja" };

		enkeleRichting.add(enkeleRichtingFalse);
		enkeleRichting.add(enkeleRichtingTrue);
		return enkeleRichting;
	}
}
