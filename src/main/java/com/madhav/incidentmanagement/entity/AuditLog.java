package com.madhav.incidentmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Represents one historical action performed on an incident.
// It stores who performed the action, what changed, and when it happened.
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long incidentId;

    private String username;

    private String action;

    private String oldValue;

    private String newValue;

    private LocalDateTime createdAt;

    public AuditLog() {
    }

    // Returns the unique ID of this audit record.
    public Long getId() {
        return id;
    }

    // Sets the unique ID of this audit record.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the ID of the incident associated with this audit record.
    public Long getIncidentId() {
        return incidentId;
    }

    // Sets the incident ID associated with this audit record.
    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    // Returns the username of the user who performed the action.
    public String getUsername() {
        return username;
    }

    // Sets the username of the user who performed the action.
    public void setUsername(String username) {
        this.username = username;
    }

    // Returns the type of action performed.
    public String getAction() {
        return action;
    }

    // Sets the type of action performed.
    public void setAction(String action) {
        this.action = action;
    }

    // Returns the previous value before the change.
    public String getOldValue() {
        return oldValue;
    }

    // Sets the previous value before the change.
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    // Returns the new value after the change.
    public String getNewValue() {
        return newValue;
    }

    // Sets the new value after the change.
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    // Returns the time when the audit action occurred.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Sets the time when the audit action occurred.
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}