package be.gim.tov.osyris.model.listener;

import java.io.IOException;

import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Rule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.model.event.SearchModelEvent;
import org.opengis.filter.Filter;

@Listener(rules = @Rule(_for = "Traject", type = "search"))
public class TrajectSearchListener {

	public void processEvent(ModelEvent event) throws IOException {

		SearchModelEvent searchEvent = (SearchModelEvent) event;

		Filter filter = (Filter) searchEvent.getQuery().getFilter()
				.accept(new TrajectVisitor(), searchEvent.getModelObject());

		searchEvent.getQuery().setFilter(filter);
	}
}