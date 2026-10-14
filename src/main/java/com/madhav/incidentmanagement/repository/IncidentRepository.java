package com.madhav.incidentmanagement.repository;

import com.madhav.incidentmanagement.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Provides database operations for Incident entities.
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Finds all incidents having the requested status.
    List<Incident> findByStatus(String status);

    // Finds an existing incident for a specific service that is still OPEN.
    // Used to prevent duplicate incidents for the same service.
    Optional<Incident> findByServiceIdAndStatus(Long serviceId, String status);
    // Returns incidents matching all supplied optional filters.
    @Query("""
    SELECT i FROM Incident i
    WHERE (:status IS NULL OR i.status = :status)
      AND (:severity IS NULL OR i.severity = :severity)
      AND (:serviceId IS NULL OR i.service.id = :serviceId)
      AND (:assignedTo IS NULL OR i.assignedTo.id = :assignedTo)
    """)
    List<Incident> findFilteredIncidents(
            @Param("status") String status,
            @Param("severity") String severity,
            @Param("serviceId") Long serviceId,
            @Param("assignedTo") Long assignedTo
    );

}