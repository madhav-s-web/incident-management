
package com.madhav.incidentmanagement;

import com.madhav.incidentmanagement.entity.Incident;
import com.madhav.incidentmanagement.entity.ServiceEntity;
import com.madhav.incidentmanagement.entity.User;
import com.madhav.incidentmanagement.repository.IncidentRepository;
import com.madhav.incidentmanagement.repository.ServiceRepository;
import com.madhav.incidentmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests incident API endpoints, validation, authorization, and business rules.
@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    // Verifies that unauthenticated users cannot view incidents.
    @Test
    void unauthenticatedUserCannotViewIncidents() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isUnauthorized());
    }

    // Verifies that a VIEWER can view incidents.
    @Test
    void viewerCanViewIncidents() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isOk());
    }

    // Verifies that a VIEWER cannot update incident status.
    @Test
    void viewerCannotUpdateIncidentStatus() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/1/status")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isForbidden());
    }

    // Verifies that an ADMIN can acknowledge a newly created OPEN incident.
    @Test
    void adminCanUpdateIncidentStatus() throws Exception {
        ServiceEntity service = new ServiceEntity();
        service.setName("admin-status-test-" + java.util.UUID.randomUUID());
        service.setDescription("Service for status update test");
        service.setStatus("UP");

        ServiceEntity savedService = serviceRepository.save(service);

        Incident incident = new Incident();
        incident.setTitle("Admin status update test");
        incident.setSeverity("HIGH");
        incident.setStatus("OPEN");
        incident.setService(savedService);

        Incident savedIncident = incidentRepository.save(incident);

        mockMvc.perform(
                patch("/api/v1/incidents/" + savedIncident.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                .roles("ADMIN"))
        ).andExpect(status().isOk());
    }

    // Verifies that unauthenticated users cannot update incident status.
    @Test
    void unauthenticatedUserCannotUpdateIncidentStatus() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}")
        ).andExpect(status().isUnauthorized());
    }

    // Verifies that a VIEWER can create an incident for a new service.
    @Test
    void viewerCanCreateIncident() throws Exception {
        ServiceEntity service = new ServiceEntity();
        service.setName("viewer-create-test-service-" + java.util.UUID.randomUUID());
        service.setDescription("Service created for incident controller test");
        service.setStatus("UP");
        service.setHealthUrl(null);

        ServiceEntity savedService = serviceRepository.save(service);

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/incidents")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Database outage",
                                    "severity": "HIGH",
                                    "serviceId": %d
                                }
                                """.formatted(savedService.getId()))
        ).andExpect(status().isOk());
    }

    // Verifies that invalid incident severity is rejected.
    @Test
    void viewerCannotCreateIncidentWithInvalidSeverity() throws Exception {
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/incidents")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Database outage",
                                    "severity": "URGENT",
                                    "serviceId": 1
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    // Verifies that requesting a nonexistent incident returns 404.
    @Test
    void requestingNonexistentIncidentReturnsNotFound() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents/999999")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isNotFound());
    }

    // Verifies that updating a nonexistent incident returns 404.
    @Test
    void updatingNonexistentIncidentReturnsNotFound() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/999999/status")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                .roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}")
        ).andExpect(status().isNotFound());
    }

    // Verifies that a VIEWER can filter incidents by status.
    @Test
    void viewerCanFilterIncidentsByStatus() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents/status/OPEN")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isOk());
    }

    // Verifies that a VIEWER can retrieve paginated incidents.
    @Test
    void viewerCanGetIncidentsWithPagination() throws Exception {
        mockMvc.perform(
                        get("/api/v1/incidents/page?page=0&size=5")
                                .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                        .roles("VIEWER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    // Verifies that creating an incident without a title returns 400.
    @Test
    void viewerCannotCreateIncidentWithoutTitle() throws Exception {
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/incidents")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "",
                                    "severity": "HIGH",
                                    "serviceId": 1
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    // Verifies that creating an incident without a service ID returns 400.
    @Test
    void viewerCannotCreateIncidentWithoutServiceId() throws Exception {
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/incidents")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Database outage",
                                    "severity": "HIGH"
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    // Verifies that an invalid incident status returns 400.
    @Test
    void adminCannotUpdateIncidentWithInvalidStatus() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/1/status")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin")
                                .roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID_STATUS\"}")
        ).andExpect(status().isBadRequest());
    }

    // Verifies that filtering by status returns a JSON array.
    @Test
    void filteringIncidentsByStatusReturnsJsonArray() throws Exception {
        mockMvc.perform(
                        get("/api/v1/incidents/status/OPEN")
                                .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                        .roles("VIEWER"))
                )
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // Verifies that combined status and severity filters are accepted.
    @Test
    void viewerCanFilterIncidentsByStatusAndSeverity() throws Exception {
        mockMvc.perform(
                        get("/api/v1/incidents?status=OPEN&severity=HIGH")
                                .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                        .roles("VIEWER"))
                )
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // Verifies that an invalid severity filter is rejected.
    @Test
    void viewerCannotFilterByInvalidSeverity() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents?severity=URGENT")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isBadRequest());
    }

    // Verifies that an unsupported status filter is rejected.
    @Test
    void viewerCannotFilterByInvalidStatus() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents?status=INVALID")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isBadRequest());
    }

    // Verifies that a non-positive service ID in a filter is rejected.
    @Test
    void viewerCannotFilterByInvalidServiceId() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents?serviceId=-1")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isBadRequest());
    }

    // Verifies that an invalid severity filter returns its expected message.
    @Test
    void invalidSeverityFilterReturnsErrorMessage() throws Exception {
        mockMvc.perform(
                        get("/api/v1/incidents?severity=URGENT")
                                .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                        .roles("VIEWER"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid severity filter"));
    }

    // Verifies that a VIEWER cannot assign an incident to a user.
    @Test
    void viewerCannotAssignIncident() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/11/assign/2")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isForbidden());
    }

    // Verifies that an ADMIN can assign an incident to an existing user.
    @Test
    void adminCanAssignIncident() throws Exception {
        ServiceEntity service = new ServiceEntity();
        service.setName("admin-assign-test-" + java.util.UUID.randomUUID());
        service.setDescription("Service for assignment test");
        service.setStatus("UP");

        ServiceEntity savedService = serviceRepository.save(service);

        Incident incident = new Incident();
        incident.setTitle("Admin assignment test");
        incident.setSeverity("HIGH");
        incident.setStatus("OPEN");
        incident.setService(savedService);

        Incident savedIncident = incidentRepository.save(incident);

        User targetUser = new User();
        targetUser.setUsername(
                "assignment-target-" + java.util.UUID.randomUUID()
        );
        targetUser.setPassword("test-password");
        targetUser.setRole("VIEWER");

        User savedUser = userRepository.save(targetUser);

        mockMvc.perform(
                patch("/api/v1/incidents/" + savedIncident.getId()
                        + "/assign/" + savedUser.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("ADMIN"))
        ).andExpect(status().isOk());
    }

    // Verifies that assigning an incident to a nonexistent user returns 404.
    @Test
    void adminCannotAssignIncidentToNonexistentUser() throws Exception {
        ServiceEntity service = new ServiceEntity();
        service.setName("missing-user-test-" + java.util.UUID.randomUUID());
        service.setDescription("Service for missing-user assignment test");
        service.setStatus("UP");

        ServiceEntity savedService = serviceRepository.save(service);

        Incident incident = new Incident();
        incident.setTitle("Missing user assignment test");
        incident.setSeverity("HIGH");
        incident.setStatus("OPEN");
        incident.setService(savedService);

        Incident savedIncident = incidentRepository.save(incident);

        mockMvc.perform(
                patch("/api/v1/incidents/" + savedIncident.getId()
                        + "/assign/99999")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("ADMIN"))
        ).andExpect(status().isNotFound());
    }

    // Verifies that assigning a nonexistent incident returns 404.
    @Test
    void adminCannotAssignNonexistentIncident() throws Exception {
        User targetUser = new User();
        targetUser.setUsername(
                "nonexistent-incident-target-" + java.util.UUID.randomUUID()
        );
        targetUser.setPassword("test-password");
        targetUser.setRole("VIEWER");

        User savedUser = userRepository.save(targetUser);

        mockMvc.perform(
                patch("/api/v1/incidents/99999/assign/" + savedUser.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("ADMIN"))
        ).andExpect(status().isNotFound());
    }

    // Verifies that creating a second OPEN incident for one service returns 409.
    @Test
    void cannotCreateSecondOpenIncidentForSameService() throws Exception {
        ServiceEntity service = new ServiceEntity();
        service.setName("duplicate-open-test-" + java.util.UUID.randomUUID());
        service.setDescription("Service for duplicate incident test");
        service.setStatus("UP");

        ServiceEntity savedService = serviceRepository.save(service);

        // Explicitly save the first incident as OPEN to test the duplicate rule.
        Incident existingIncident = new Incident();
        existingIncident.setTitle("Existing open incident");
        existingIncident.setSeverity("HIGH");
        existingIncident.setStatus("OPEN");
        existingIncident.setService(savedService);

        incidentRepository.save(existingIncident);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/v1/incidents")
                                .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                        .roles("VIEWER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "title": "Duplicate open incident",
                                    "severity": "HIGH",
                                    "serviceId": %d
                                }
                                """.formatted(savedService.getId()))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("An open incident already exists for this service"));
    }
}
