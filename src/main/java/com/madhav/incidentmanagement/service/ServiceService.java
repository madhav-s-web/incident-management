package com.madhav.incidentmanagement.service;

import com.madhav.incidentmanagement.entity.ServiceEntity;
import com.madhav.incidentmanagement.repository.ServiceRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public ServiceEntity createService(ServiceEntity service) {
        return serviceRepository.save(service);
    }
    public ServiceEntity getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Service not found"));
    }


}

