package be.gim.tov.osyris.model.utils;

import java.util.Comparator;
import java.util.Date;

import be.gim.tov.osyris.model.werk.WerkOpdracht;

/**
 * 
 * @author kristof
 * 
 */
public class DateSortingWO implements Comparator<WerkOpdracht> {

	@Override
	public int compare(WerkOpdracht o1, WerkOpdracht o2) {

		if (o1 == o2) {
			return 0;
		}
		Date date1 = o1.getDatumLaatsteWijziging();
		Date date2 = o2.getDatumLaatsteWijziging();

		return date2.compareTo(date1);
	}
}
