package com.zero.cohousesever.common.config;

import com.zero.cohousesever.task.scheduler.GenerateNextWeekJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

  private static final String JOB_NAME = "generateNextWeekJob";
  private static final String TRIGGER_NAME = "generateNextWeekTrigger";

  @Bean
  public JobDetail generateNextWeekJobDetail() {
    return JobBuilder.newJob(GenerateNextWeekJob.class)
        .withIdentity(JOB_NAME)
        .storeDurably()
        .build();
  }

  // 할 일 자동 배정
  @Bean
  public Trigger generateNextWeekTrigger(JobDetail generateNextWeekJobDetail) {
    CronScheduleBuilder cron = CronScheduleBuilder
        .cronSchedule("0 10 0 ? * SAT")
        .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"));

    return TriggerBuilder.newTrigger()
        .forJob(generateNextWeekJobDetail)
        .withIdentity(TRIGGER_NAME)
        .withSchedule(cron)
        .build();
  }
}
