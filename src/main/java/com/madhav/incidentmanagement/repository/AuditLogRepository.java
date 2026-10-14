package com.madhav.incidentmanagement.repository;

import com.madhav.incidentmanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Provides database operations for audit logs and retrieves
// audit history belonging to a specific incident.
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Finds all audit records associated with the given incident ID.
    List<AuditLog> findByIncidentId(Long incidentId);
}