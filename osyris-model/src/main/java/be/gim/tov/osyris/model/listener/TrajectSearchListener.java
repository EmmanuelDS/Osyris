package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.CountModelEvent;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.model.event.SearchModelEvent;
import org.conscientia.api.search.Query;
import org.opengis.filter.Filter;

/**
 * 
 * @author kristof
 * 
 */
@Listener(rules = { @Rule(_for = "Traject", type = "search"),
		@Rule(_for = "Traject", type = "count") })
public class TrajectSearchListener {

	public void processEvent(ModelEvent event) throws IOException {

		Filter filter = (Filter) getQuery(event).getFilter().accept(
				new TrajectVisitor(), event.getModelObject());

		getQuery(event).setFilter(filter);
	}

	protected Query getQuery(ModelEvent event) {
		if (event instanceof SearchModelEvent) {
			return ((SearchModelEvent) event).getQuery();
		}
		if (event instanceof CountModelEvent) {
			return ((CountModelEvent) event).getQuery();
		}
		return null;
	}
}