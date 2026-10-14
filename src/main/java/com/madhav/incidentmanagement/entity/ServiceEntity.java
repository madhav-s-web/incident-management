  package com.madhav.incidentmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String status = "UP";

    // Stores the URL used to check whether the service is healthy.
    @Column(name = "health_url")
    private String healthUrl;

    // Required by JPA to create entity objects from database records.
    public ServiceEntity() {}

    // Returns the unique ID of the service.
    public Long getId() {
        return id;
    }

    // Returns the service name.
    public String getName() {
        return name;
    }

    // Sets the service name.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the service description.
    public String getDescription() {
        return description;
    }

    // Sets the service description.
    public void setDescription(String description) {
        this.description = description;
    }

    // Returns the current recorded status of the service.
    public String getStatus() {
        return status;
    }

    // Updates the recorded status of the service.
    public void setStatus(String status) {
        this.status = status;
    }

    // Returns the health-check URL.
    public String getHealthUrl() {
        return healthUrl;
    }

    // Sets the health-check URL.
    public void setHealthUrl(String healthUrl) {
        this.healthUrl = healthUrl;
    }
}
