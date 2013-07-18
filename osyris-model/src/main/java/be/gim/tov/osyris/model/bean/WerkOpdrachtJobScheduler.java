package be.gim.tov.osyris.model.bean;

import java.util.Date;

import javax.enterprise.event.Observes;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.LogFactory;
import org.jboss.solder.servlet.WebApplication;
import org.jboss.solder.servlet.event.Destroyed;
import org.jboss.solder.servlet.event.Started;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

@Named
@ApplicationScoped
public class WerkOpdrachtJobScheduler {

	private static final org.apache.commons.logging.Log LOG = LogFactory
			.getLog(WerkOpdrachtJobScheduler.class);

	private Scheduler scheduler;
	@Inject
	private CdiJobFactory cdiJobFactory;

	public void onStartup(@Observes @Started WebApplication webapp) {

		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.setJobFactory(cdiJobFactory);
			final JobDetail testJobDetail = JobBuilder
					.newJob(WerkOpdrachtJob.class)
					.withIdentity("werkOpdracht", "werkOpdracht-jobs").build();

			final SimpleTrigger trigger = TriggerBuilder
					.newTrigger()
					.startAt(new Date())
					.withSchedule(
							SimpleScheduleBuilder.repeatMinutelyForever(1))
					.build();
			scheduler.scheduleJob(testJobDetail, trigger);
			// Start the scheduler a minute after the webapplication is started.
			scheduler.startDelayed(60);

		} catch (SchedulerException e) {
			LOG.error("Unable to start Scheduler.", e);
		}
	}

	public void onShutdown(@Observes @Destroyed WebApplication webapp) {
		if (scheduler != null) {
			try {
				scheduler.shutdown(true);
			} catch (SchedulerException e) {
				LOG.error("Error while shutting down scheduler.", e);
			}
		}
	}
}
