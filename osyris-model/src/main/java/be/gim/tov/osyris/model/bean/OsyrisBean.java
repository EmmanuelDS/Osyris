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

}
