package org.mb.boot.monitoring.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import de.codecentric.boot.admin.notify.LoggingNotifier;
import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.RemindingNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;

@Configuration
@EnableScheduling
public class NotifierConfiguration {

    @Bean
    @Primary
    public RemindingNotifier remindingNotifier() {
        RemindingNotifier remindingNotifier = new RemindingNotifier(loggingNotifier());
        remindingNotifier.setReminderPeriod(TimeUnit.MINUTES.toMillis(5)); 
        return remindingNotifier;
    }

    @Scheduled(fixedRate = 60000L) 
    public void remind() {
        remindingNotifier().sendReminders();
    }
    
	@Bean
	public FilteringNotifier filteringNotifier(Notifier delegate) {
		return new FilteringNotifier(delegate);
	}
    
    @Bean
    public LoggingNotifier loggingNotifier(){
    	return new LoggingNotifier();
    }
}