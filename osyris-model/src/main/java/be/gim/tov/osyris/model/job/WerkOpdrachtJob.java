package be.gim.tov.osyris.model.job;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.LogFactory;
import org.conscientia.api.model.annotation.Listener;
import org.conscientia.api.model.annotation.Schedule;
import org.conscientia.api.model.event.ModelEvent;
import org.conscientia.api.repository.ModelRepository;
import org.conscientia.core.search.QueryBuilder;
import org.conscientia.core.security.annotation.RunPrivileged;

import be.gim.commons.filter.FilterUtils;
import be.gim.tov.osyris.model.werk.WerkOpdracht;
import be.gim.tov.osyris.model.werk.status.WerkopdrachtStatus;

@Listener(schedules = @Schedule("0 0 1 * * ?"))
// @Listener(schedules = @Schedule("0 * * * * ?"))
public class WerkOpdrachtJob {

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(WerkOpdrachtJob.class);

	@Inject
	protected ModelRepository modelRepository;

	@RunPrivileged
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

						if (datumOpdracht.getTimeInMillis() < datumVandaag
								.getTimeInMillis()
								|| DateUtils.isSameDay(datumOpdracht,
										datumVandaag)) {

							opdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
							opdracht.setDatumTeControleren(new Date());
							opdracht.setDatumLaterUitTeVoeren(null);
							opdracht.setDatumLaatsteWijziging(new Date());
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
