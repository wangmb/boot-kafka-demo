package org.mb.boot.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.codecentric.boot.admin.config.EnableAdminServer;

@SpringBootApplication
@EnableAdminServer
public class MonitoringApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MonitoringApplication.class, args);
	}
	
}