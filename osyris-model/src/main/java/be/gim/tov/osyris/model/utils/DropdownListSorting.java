package be.gim.tov.osyris.model.utils;

import java.util.Comparator;

/**
 * 
 * @author kristof
 * 
 */
public class DropdownListSorting implements Comparator<Object[]> {

	@Override
	public int compare(Object[] o1, Object[] o2) {
		String p1 = (String) o1[1];
		String p2 = (String) o2[1];
		int res = p1.compareToIgnoreCase(p2);
		if (res != 0) {
			return res;
		}
		return p1.compareToIgnoreCase(p2);
	}
}
