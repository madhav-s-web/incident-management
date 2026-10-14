//Username: madhav
//
//
//
//Password: TestReset@123   Rohit@123



package com.madhav.incidentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Starts the Spring Boot application and enables scheduled tasks.
@SpringBootApplication
@EnableScheduling
public class IncidentManagementApplication {

	// Starts the Spring Boot application.
	public static void main(String[] args) {
		SpringApplication.run(IncidentManagementApplication.class, args);
	}
}