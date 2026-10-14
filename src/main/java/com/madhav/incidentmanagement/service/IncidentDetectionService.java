package com.madhav.incidentmanagement.service;

import com.madhav.incidentmanagement.entity.AuditLog;
import com.madhav.incidentmanagement.entity.Incident;
import com.madhav.incidentmanagement.entity.ServiceEntity;
import com.madhav.incidentmanagement.repository.AuditLogRepository;
import com.madhav.incidentmanagement.repository.IncidentRepository;
import com.madhav.incidentmanagement.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

// Monitors real service health, creates incidents for failures,
// resolves incidents after recovery, and records audit history.
@Service
public class IncidentDetectionService {

    private final ServiceRepository serviceRepository;
    private final IncidentRepository incidentRepository;
    private final AuditLogRepository auditLogRepository;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    // Injects repositories required for monitoring, incidents, and auditing.
    public IncidentDetectionService(
            ServiceRepository serviceRepository,
            IncidentRepository incidentRepository,
            AuditLogRepository auditLogRepository) {

        this.serviceRepository = serviceRepository;
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // Controls whether automatic scheduled monitoring is enabled.
    @Value("${incident.detection.scheduling.enabled:true}")
    private boolean schedulingEnabled;

    // Sends an HTTP request to the service health URL.
    // A 2xx response means UP; a failed request means DOWN.
    private boolean isServiceHealthy(String healthUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            return response.statusCode() >= 200
                    && response.statusCode() < 300;

        } catch (Exception exception) {
            return false;
        }
    }

    // Checks a service, updates its recorded status, and creates
    // or resolves incidents while recording automatic audit entries.
    @Transactional
    public void checkService(Long serviceId) {

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() ->
                        new RuntimeException("Service not found"));

        // Skips monitoring when no health URL has been configured.
        if (service.getHealthUrl() == null
                || service.getHealthUrl().isBlank()) {
            return;
        }

        // Determines the actual status by contacting the health endpoint.
        String oldServiceStatus = service.getStatus();
        String actualStatus = isServiceHealthy(service.getHealthUrl())
                ? "UP"
                : "DOWN";

        service.setStatus(actualStatus);
        serviceRepository.save(service);

        // Resolves an existing OPEN incident when the service recovers.
        if ("UP".equals(actualStatus)) {

            incidentRepository
                    .findByServiceIdAndStatus(serviceId, "OPEN")
                    .ifPresent(incident -> {

                        String oldStatus = incident.getStatus();
                        incident.setStatus("RESOLVED");

                        Incident savedIncident =
                                incidentRepository.save(incident);

                        // Records the automatic incident resolution.
                        AuditLog auditLog = new AuditLog();
                        auditLog.setIncidentId(savedIncident.getId());
                        auditLog.setUsername("SYSTEM");
                        auditLog.setAction("STATUS_CHANGED");
                        auditLog.setOldValue(oldStatus);
                        auditLog.setNewValue("RESOLVED");
                        auditLog.setCreatedAt(LocalDateTime.now());

                        auditLogRepository.save(auditLog);
                    });

            return;
        }

        // Avoids creating another incident if one is already OPEN.
        if (incidentRepository
                .findByServiceIdAndStatus(serviceId, "OPEN")
                .isPresent()) {
            return;
        }

        // Creates an incident when the health check reports failure.
        Incident incident = new Incident();
        incident.setTitle(service.getName() + " is down");
        incident.setSeverity("HIGH");
        incident.setService(service);

        Incident savedIncident = incidentRepository.save(incident);

        // Records the automatic incident creation in the audit history.
        AuditLog auditLog = new AuditLog();
        auditLog.setIncidentId(savedIncident.getId());
        auditLog.setUsername("SYSTEM");
        auditLog.setAction("INCIDENT_CREATED");
        auditLog.setOldValue(null);
        auditLog.setNewValue("OPEN");
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    // Checks every registered service every 60 seconds when scheduling is enabled.
    @Scheduled(fixedRate = 60000)
    public void checkAllServices() {
        if (!schedulingEnabled) {
            return;
        }

        for (ServiceEntity service : serviceRepository.findAll()) {
            checkService(service.getId());
        }
    }
}