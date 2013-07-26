package be.gim.tov.osyris.model.bean;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Schedule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

@Listener(schedules = @Schedule("0 0 1 * * ?"))
public class WerkOpdrachtJob {
	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(WerkOpdrachtJob.class);

	@Inject
	protected ModelRepository modelRepository;

	public void processEvent(ModelEvent event) {

		try {
			QueryBuilder builder = new QueryBuilder("WerkOpdracht");

			builder.addFilter(FilterUtils.equal("status",
					WerkopdrachtStatus.UITGESTELD));

			// Zoeken alle uitgestelde opdrachten
			List<WerkOpdracht> uitgesteldeOpdrachten = (List<WerkOpdracht>) modelRepository
					.searchObjects(builder.build(), false, false);

			// Voor alle uitgestelde opdrachten checken of datum
			// lateruitTeVoeren gelijk is aan de datum van vandaag
			if (uitgesteldeOpdrachten != null) {
				for (WerkOpdracht opdracht : uitgesteldeOpdrachten) {
					try {
						// Check datums
						Calendar datumOpdracht = Calendar.getInstance();
						datumOpdracht.setTime(opdracht
								.getDatumLaterUitTeVoeren());
						Calendar datumVandaag = Calendar.getInstance();
						datumVandaag.setTime(new Date());

						if ((datumOpdracht.get(Calendar.ERA) == datumVandaag
								.get(Calendar.ERA)
								&& datumOpdracht.get(Calendar.YEAR) == datumVandaag
										.get(Calendar.YEAR) && datumOpdracht
									.get(Calendar.DAY_OF_YEAR) == datumVandaag
								.get(Calendar.DAY_OF_YEAR))) {

							opdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
							opdracht.setDatumTeControleren(new Date());

							modelRepository.saveObject(opdracht);
						}
					} catch (Exception e) {
						LOG.error("Can not update WerkOpdracht.", e);
					}
				}
			}
		} catch (IOException e) {
			LOG.error("Can not search WerkOpdracht.", e);
		}
	}
}
