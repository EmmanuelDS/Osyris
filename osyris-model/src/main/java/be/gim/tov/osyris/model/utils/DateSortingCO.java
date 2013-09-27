package be.gim.tov.osyris.model.utils;

import java.util.Comparator;
import java.util.Date;

import be.gim.tov.osyris.model.controle.ControleOpdracht;

/**
 * 
 * @author kristof
 * 
 */
public class DateSortingCO implements Comparator<ControleOpdracht> {

	@Override
	public int compare(ControleOpdracht o1, ControleOpdracht o2) {

		if (o1 == o2) {
			return 0;
		}
		Date date1 = o1.getDatumLaatsteWijziging();
		Date date2 = o2.getDatumLaatsteWijziging();

		return date2.compareTo(date1);
	}
}
