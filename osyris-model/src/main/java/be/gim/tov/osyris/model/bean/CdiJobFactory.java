package be.gim.tov.osyris.model.bean;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.solder.beanManager.BeanManagerAware;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

@Named
public class CdiJobFactory extends BeanManagerAware implements JobFactory {

	private static final Log LOG = LogFactory.getLog(CdiJobFactory.class);

	@Override
	public Job newJob(TriggerFiredBundle bundle, Scheduler Scheduler)
			throws SchedulerException {

		final JobDetail jobDetail = bundle.getJobDetail();
		final Class<? extends Job> jobClass = jobDetail.getJobClass();
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Producing instance of Job '" + jobDetail.getKey()
						+ "', class=" + jobClass.getName());
			}

			return getBean(jobClass);
		} catch (Exception e) {
			throw new SchedulerException("Problem instantiating class '"
					+ jobDetail.getJobClass().getName() + "'", e);
		}
	}

	private Job getBean(Class jobClazz) {
		final BeanManager bm = getBeanManager();
		final Bean<?> bean = bm.getBeans(jobClazz).iterator().next();
		final CreationalContext<?> ctx = bm.createCreationalContext(bean);
		return (Job) bm.getReference(bean, jobClazz, ctx);
	}
}
