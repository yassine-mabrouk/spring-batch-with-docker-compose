package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class SchedulerConfig {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job exportCustomerJob;


    @Scheduled(cron = "${batch.job.cron}")
    public void runBatchJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            System.out.println("Starting scheduled batch job at: "+System.currentTimeMillis());
            jobLauncher.run(exportCustomerJob, jobParameters);
            System.out.println("Scheduled Batch job completed successfully!");

        } catch (Exception e) {
            System.err.println("Error running batch job: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
