package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private static Properties getProperties() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        try {
            Properties config = getProperties();
            try (Connection connection = DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")
            )) {
                try {
                    int interval = Integer.parseInt(config.getProperty("rabbit.interval"));
                    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                    scheduler.start();
                    JobDataMap data = new JobDataMap();
                    data.put("connection", connection);
                    JobDetail job = newJob(Rabbit.class)
                            .usingJobData(data)
                            .build();
                    SimpleScheduleBuilder times = simpleSchedule()
                            .withIntervalInSeconds(interval)
                            .repeatForever();
                    Trigger trigger = newTrigger()
                            .startNow()
                            .withSchedule(times)
                            .build();
                    scheduler.scheduleJob(job, trigger);
                    Thread.sleep(10000);
                    scheduler.shutdown();
                } catch (SchedulerException se) {
                    se.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here....");
            try (Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection")) {
                try (PreparedStatement statement
                             = connection.prepareStatement("INSERT INTO rabbit(created_date) VALUES(?)")) {
                    statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    statement.execute();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
