package be.gim.tov.osyris.model.listener;

import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;

import be.gim.commons.filter.FilterUtils;

/**
 * 
 * @author kristof
 * 
 */
public class TrajectVisitor extends DuplicatingFilterVisitor {

	private static final String GEEN_PETER_METER = "Geen PeterMeter toegewezen";

	@Override
	public Object visit(PropertyIsEqualTo equalTo, Object extraData) {

		if (equalTo.getExpression1().toString().equals("peterMeter")) {
			if (equalTo.getExpression2().toString().equals(GEEN_PETER_METER)
					|| equalTo.getExpression2().toString().isEmpty()) {
				return FilterUtils.or(FilterUtils.equal("peterMeter1", null),
						FilterUtils.equal("peterMeter2", null),
						FilterUtils.equal("peterMeter3", null));
			}
			return FilterUtils.or(FilterUtils.equal("peterMeter1", equalTo
					.getExpression2().toString()), FilterUtils.equal(
					"peterMeter2", equalTo.getExpression2().toString()),
					FilterUtils.equal("peterMeter3", equalTo.getExpression2()
							.toString()));
		}
		return equalTo;
	}
}
