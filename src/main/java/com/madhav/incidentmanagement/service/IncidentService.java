
        package com.madhav.incidentmanagement.service;

        import com.madhav.incidentmanagement.entity.AuditLog;
        import com.madhav.incidentmanagement.entity.Incident;
        import com.madhav.incidentmanagement.entity.ServiceEntity;
        import com.madhav.incidentmanagement.entity.User;
        import com.madhav.incidentmanagement.exception.ResourceNotFoundException;
        import com.madhav.incidentmanagement.repository.AuditLogRepository;
        import com.madhav.incidentmanagement.repository.IncidentRepository;
        import com.madhav.incidentmanagement.repository.ServiceRepository;
        import com.madhav.incidentmanagement.repository.UserRepository;
        import org.springframework.data.domain.Page;
        import org.springframework.data.domain.Pageable;
        import org.springframework.security.core.context.SecurityContextHolder;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        import java.time.LocalDateTime;
        import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final ServiceRepository serviceRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // Injects the repositories required for incidents, services,
    // audit history, and finding users for incident assignment.
    public IncidentService(
            IncidentRepository incidentRepository,
            ServiceRepository serviceRepository,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository) {

        this.incidentRepository = incidentRepository;
        this.serviceRepository = serviceRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    // Creates a new incident associated with an existing service.
    public Incident createIncident(String title, String severity, Long serviceId) {

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Service not found"));

        Incident incident = new Incident();
        incident.setTitle(title);
        incident.setSeverity(severity);
        incident.setService(service);

        return incidentRepository.save(incident);
    }

    // Assigns an incident to an existing registered user.
    // Records the assignment in the audit history.
    @Transactional
    public Incident assignIncident(Long incidentId, Long userId) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Incident not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Avoid creating a duplicate audit entry for the same assignment.
        if (incident.getAssignedTo() != null &&
                incident.getAssignedTo().getId().equals(userId)) {
            return incident;
        }

        // Store the previous assignee for the audit log.
        String oldValue = incident.getAssignedTo() == null
                ? null
                : incident.getAssignedTo().getUsername();

        incident.setAssignedTo(user);
        Incident savedIncident = incidentRepository.save(incident);

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        AuditLog auditLog = new AuditLog();
        auditLog.setIncidentId(savedIncident.getId());
        auditLog.setUsername(username);
        auditLog.setAction("INCIDENT_ASSIGNED");
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(user.getUsername());
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);

        return savedIncident;
    }

    // Updates an incident's status and records the change in the audit history.
    @Transactional
    public Incident updateIncidentStatus(Long id, String status) {

        if (!status.equals("OPEN")
                && !status.equals("ACKNOWLEDGED")
                && !status.equals("RESOLVED")) {
            throw new IllegalArgumentException("Invalid incident status");
        }

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Incident not found"));

        String oldStatus = incident.getStatus();
        validateStatusTransition(oldStatus, status);

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        incident.setStatus(status);
        Incident savedIncident = incidentRepository.save(incident);

        AuditLog auditLog = new AuditLog();
        auditLog.setIncidentId(savedIncident.getId());
        auditLog.setUsername(username);
        auditLog.setAction("STATUS_CHANGED");
        auditLog.setOldValue(oldStatus);
        auditLog.setNewValue(status);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);

        return savedIncident;
    }


    // Validates whether an incident can transition to its requested status.
    private void validateStatusTransition(String currentStatus, String newStatus) {
        boolean valid =
                (currentStatus.equals("OPEN")
                        && (newStatus.equals("ACKNOWLEDGED")
                        || newStatus.equals("RESOLVED")))
                        || (currentStatus.equals("ACKNOWLEDGED")
                        && newStatus.equals("RESOLVED"));

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition: " + currentStatus + " -> " + newStatus
            );
        }
    }

    // Returns incidents matching the requested status.
    public List<Incident> getIncidentsByStatus(String status) {
        return incidentRepository.findByStatus(status);
    }

    // Finds an incident by ID or reports that it does not exist.
    public Incident getIncidentById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Incident not found"));
    }

    // Returns audit records associated with an existing incident.
    public List<AuditLog> getAuditHistory(Long incidentId) {

        incidentRepository.findById(incidentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Incident not found"));

        return auditLogRepository.findByIncidentId(incidentId);
    }

    // Returns all incidents.
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    // Returns incidents matching all supplied optional filters.
    public List<Incident> getFilteredIncidents(
            String status,
            String severity,
            Long serviceId,
            Long assignedTo) {

        return incidentRepository.findFilteredIncidents(
                status, severity, serviceId, assignedTo
        );
    }
    // Returns a page of incidents using the requested pagination settings.
    public Page<Incident> getIncidents(Pageable pageable) {
        return incidentRepository.findAll(pageable);
    }


    // Returns incidents for the requested page.
    public Page<Incident> getIncidentsWithPagination(Pageable pageable) {
        return incidentRepository.findAll(pageable);
    }
}
