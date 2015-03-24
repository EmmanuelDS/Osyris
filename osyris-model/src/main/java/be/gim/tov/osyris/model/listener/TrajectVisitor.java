package be.gim.tov.osyris.model.listener;

import java.util.List;

import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;

import be.gim.commons.bean.Beans;
import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.bean.OsyrisModelFunctions;

/**
 * 
 * @author kristof
 * 
 */
public class TrajectVisitor extends DuplicatingFilterVisitor {

	private static final String GEEN_PETER_METER = "Geen PeterMeter toegewezen";

	@Override
	public Object visit(PropertyIsEqualTo equalTo, Object extraData) {

		if (equalTo.getExpression1().toString().equals("inLus")) {

			if (equalTo.getExpression2().toString().equals("0")) {

				List<String> ids = Beans.getReference(
						OsyrisModelFunctions.class).getSegmentenInNetwerkLus();
				return FilterUtils.not(FilterUtils.id(ids));
			} else if (equalTo.getExpression2().toString().equals("1")) {
				List<String> ids = Beans.getReference(
						OsyrisModelFunctions.class).getSegmentenInNetwerkLus();
				return FilterUtils.id(ids);
			}
		}

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
