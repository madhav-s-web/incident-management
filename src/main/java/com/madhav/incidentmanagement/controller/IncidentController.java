
package com.madhav.incidentmanagement.controller;

import com.madhav.incidentmanagement.entity.AuditLog;
import com.madhav.incidentmanagement.entity.Incident;
import com.madhav.incidentmanagement.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Exposes REST endpoints for creating, filtering, retrieving, and updating incidents.
@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    // Injects the service that handles incident operations.
    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    // Creates an incident using the validated request body.
    @PostMapping
    public Incident createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {
        return incidentService.createIncident(
                request.title(),
                request.severity(),
                request.serviceId()
        );
    }

    // Defines and validates the required fields for incident creation.
    public record CreateIncidentRequest(
            @jakarta.validation.constraints.NotBlank String title,
            @jakarta.validation.constraints.Pattern(
                    regexp = "LOW|MEDIUM|HIGH|CRITICAL"
            ) String severity,
            @jakarta.validation.constraints.NotNull Long serviceId
    ) {}

    // Defines and validates the JSON body for updating an incident's status.
    public record UpdateIncidentStatusRequest(
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Pattern(
                    regexp = "OPEN|ACKNOWLEDGED|RESOLVED"
            )
            String status
    ) {}

    // Returns incidents matching any supplied filters.
    @GetMapping
    public List<Incident> getAllIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long assignedTo) {

        // Rejects unsupported severity values in incident filters.
        if (severity != null &&
                !List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(severity)) {
            throw new IllegalArgumentException("Invalid severity filter");
        }

        // Rejects unsupported status values in incident filters.
        if (status != null &&
                !List.of("OPEN", "ACKNOWLEDGED", "RESOLVED").contains(status)) {
            throw new IllegalArgumentException("Invalid status filter");
        }

        // Rejects non-positive service IDs.
        if (serviceId != null && serviceId <= 0) {
            throw new IllegalArgumentException("serviceId must be positive");
        }

        // Rejects non-positive assignee IDs.
        if (assignedTo != null && assignedTo <= 0) {
            throw new IllegalArgumentException("assignedTo must be positive");
        }

        return incidentService.getFilteredIncidents(
                status, severity, serviceId, assignedTo
        );
    }

    // Returns a paginated list of incidents.
    @GetMapping("/page")
    public Page<Incident> getIncidentsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(page, size);
        return incidentService.getIncidentsWithPagination(pageable);
    }

    // Returns a specific incident by its ID.
    @GetMapping("/{id}")
    public Incident getIncidentById(@PathVariable Long id) {
        return incidentService.getIncidentById(id);
    }

    // Returns the audit history for a specific incident.
    @GetMapping("/{id}/audit")
    public List<AuditLog> getAuditHistory(@PathVariable Long id) {
        return incidentService.getAuditHistory(id);
    }

    // Assigns an incident to a user using their user ID.
    @PatchMapping("/{incidentId}/assign/{userId}")
    public Incident assignIncident(
            @PathVariable Long incidentId,
            @PathVariable Long userId) {
        return incidentService.assignIncident(incidentId, userId);
    }

    // Returns all incidents matching the requested status.
    @GetMapping("/status/{status}")
    public List<Incident> getIncidentsByStatus(@PathVariable String status) {
        return incidentService.getIncidentsByStatus(status);
    }

    // Updates an incident's status using a validated JSON request body.
    @PatchMapping("/{id}/status")
    public Incident updateIncidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return incidentService.updateIncidentStatus(id, request.status());
    }
}
