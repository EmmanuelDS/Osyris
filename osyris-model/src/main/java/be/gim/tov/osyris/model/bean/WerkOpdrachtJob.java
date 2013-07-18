package be.gim.tov.osyris.model.bean;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.LogFactory;
import org.conscientia.api.repository.ModelRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Named
public class WerkOpdrachtJob implements Job {

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(WerkOpdrachtJob.class);

	@Inject
	protected ModelRepository modelRepository;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		LOG.debug("WERKOPDRACHT JOB SCHEDULED EVERY MINUTE HAS EXECUTED!!");

		// try {
		// DefaultQuery query = new DefaultQuery();
		// query.setModelClassName("WerkOpdracht");
		// query.setFilter(FilterUtils.equal("status",
		// WerkopdrachtStatus.UITGESTELD));
		//
		// // Zoeken alle uitgestelde opdrachten
		// List<WerkOpdracht> uitgesteldeOpdrachten = (List<WerkOpdracht>)
		// modelRepository
		// .searchObjects(query, false, false);
		//
		// // Voor alle uitgestelde opdrachten checken of datum
		// // lateruitTeVoeren gelijk is aan de datum van vandaag
		// if (uitgesteldeOpdrachten != null) {
		//
		// for (WerkOpdracht opdracht : uitgesteldeOpdrachten) {
		// Calendar datumOpdracht = Calendar.getInstance();
		// Calendar datumVandaag = Calendar.getInstance();
		//
		// datumOpdracht.setTime(opdracht.getDatumLaterUitTeVoeren());
		// datumVandaag.setTime(new Date());
		//
		// if ((datumOpdracht.get(Calendar.ERA) == datumVandaag
		// .get(Calendar.ERA)
		// && datumOpdracht.get(Calendar.YEAR) == datumVandaag
		// .get(Calendar.YEAR) && datumOpdracht
		// .get(Calendar.DAY_OF_YEAR) == datumVandaag
		// .get(Calendar.DAY_OF_YEAR))) {
		//
		// opdracht.setStatus(WerkopdrachtStatus.TE_CONTROLEREN);
		// opdracht.setDatumTeControleren(new Date());
		// modelRepository.saveObject(opdracht);
		// }
		// }
		// }
		// } catch (IOException e) {
		// LOG.error("Can not search WerkOpdracht.", e);
		// }
	}
}
