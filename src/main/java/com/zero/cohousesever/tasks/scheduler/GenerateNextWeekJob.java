package com.zero.cohousesever.tasks.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class GenerateNextWeekJob extends QuartzJobBean {

  private final TaskAssignmentScheduler scheduler;

  @Override
  protected void executeInternal(JobExecutionContext context) {
    scheduler.generateNextWeek();
    log.info("[Quartz] generateNextWeek executed");
  }
}
