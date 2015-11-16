/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.framework.utils;

import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {

    private static SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();

    private static String JOB_GROUP_NAME = "TIME_JOBGROUP_NAME";

    private static String TRIGGER_GROUP_NAME = "TIME_TRIGGERGROUP_NAME";

    public synchronized void addJob(String taskKey, Class<? extends Job> classObj,
            Map<?, ?> jobDataMao, String cron) throws SchedulerException {
        JobKey jobkey = JobKey.jobKey(taskKey, JOB_GROUP_NAME);
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        JobDataMap dataMap = new JobDataMap(jobDataMao);

        JobDetail jobDetail =
                JobBuilder.newJob(classObj).withIdentity(jobkey).usingJobData(dataMap).build();

        CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cron);

        TriggerKey triggerKey = TriggerKey.triggerKey(taskKey, TRIGGER_GROUP_NAME);
        CronTrigger trigger =
                TriggerBuilder.newTrigger().withIdentity(triggerKey)
                        .withIdentity(jobkey.toString()).startNow().withSchedule(builder).build();

        scheduler.scheduleJob(jobDetail, trigger);

        if (!scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    public synchronized void addJob(String taskKey, JobDetail jobDetail, String cron)
            throws SchedulerException {
        JobKey jobkey = JobKey.jobKey(taskKey, JOB_GROUP_NAME);
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cron);

        TriggerKey triggerKey = TriggerKey.triggerKey(taskKey, TRIGGER_GROUP_NAME);
        CronTrigger trigger =
                TriggerBuilder.newTrigger().withIdentity(triggerKey)
                        .withIdentity(jobkey.toString()).startNow().withSchedule(builder).build();

        scheduler.scheduleJob(jobDetail, trigger);

        if (!scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    public synchronized void deleteJob(String taskKey) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(taskKey, JOB_GROUP_NAME);
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        scheduler.pauseJob(jobKey);
        TriggerKey triggerKey = TriggerKey.triggerKey(taskKey, TRIGGER_GROUP_NAME);
        scheduler.unscheduleJob(triggerKey);
        scheduler.deleteJob(jobKey);
    }

    public synchronized void modifyJobCron(String taskKey, String newCron)
            throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(taskKey, JOB_GROUP_NAME);
        Scheduler scheduler = gSchedulerFactory.getScheduler();

        JobDetail jobDetail = scheduler.getJobDetail(jobKey);

        if (jobDetail == null) {
            return;
        }
        deleteJob(taskKey);

        addJob(taskKey, jobDetail, newCron);
    }

    public void shutdownJobs(Boolean forceShutdown) throws SchedulerException {
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        scheduler.shutdown(forceShutdown);
    }

    public void startJobs() throws SchedulerException {
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        scheduler.start();
    }
}
