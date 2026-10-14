package com.madhav.incidentmanagement.controller;

import com.madhav.incidentmanagement.entity.ServiceEntity;
import com.madhav.incidentmanagement.service.IncidentDetectionService;
import com.madhav.incidentmanagement.service.ServiceService;
import org.springframework.web.bind.annotation.*;

// Handles HTTP requests related to services and manual incident detection.
@RestController // recieves http request
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final IncidentDetectionService incidentDetectionService;

    // Creates ServiceController and injects the services required
    // for service operations and incident detection.
    public ServiceController(
            ServiceService serviceService,
            IncidentDetectionService incidentDetectionService) {

        this.serviceService = serviceService;
        this.incidentDetectionService = incidentDetectionService;
    }

    // Creates a new service.
    @PostMapping
    public ServiceEntity createService(@RequestBody ServiceEntity service) {
        return serviceService.createService(service);
    }

    // Returns a service by its ID.
    @GetMapping("/{id}")
    public ServiceEntity getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id);
    }

    // Manually triggers incident detection for a specific service.
    // Used to test the detection logic before adding automatic scheduling.
    @PostMapping("/{id}/check")
    public void checkService(@PathVariable Long id) {
        incidentDetectionService.checkService(id);
    }
}