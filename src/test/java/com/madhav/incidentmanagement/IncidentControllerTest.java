package com.madhav.incidentmanagement;
import com.madhav.incidentmanagement.entity.Incident;
        import com.madhav.incidentmanagement.entity.ServiceEntity;
        import com.madhav.incidentmanagement.repository.IncidentRepository;
        import com.madhav.incidentmanagement.repository.ServiceRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired
    private ServiceRepository serviceRepository;


    @Test
    void unauthenticatedUserCannotViewIncidents() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCanViewIncidents() throws Exception {
        mockMvc.perform(get("/api/v1/incidents")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER")))
                .andExpect(status().isOk());
    }

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

        // Create a unique service to avoid conflicts with existing database records.
        ServiceEntity service = new ServiceEntity();
        service.setName("admin-status-test-" + java.util.UUID.randomUUID());
        service.setDescription("Service for status update test");
        service.setStatus("UP");

        ServiceEntity savedService = serviceRepository.save(service);

        // Create an incident whose initial status is OPEN.
        Incident incident = new Incident();
        incident.setTitle("Admin status update test");
        incident.setSeverity("HIGH");
        incident.setService(savedService);

        Incident savedIncident = incidentRepository.save(incident);

        // Request the valid transition OPEN -> ACKNOWLEDGED as an ADMIN.
        mockMvc.perform(
                patch("/api/v1/incidents/" + savedIncident.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}")
                        .with(
                                SecurityMockMvcRequestPostProcessors.user("admin")
                                        .roles("ADMIN")
                        )
        ).andExpect(status().isOk());
    }


    @Test
    void unauthenticatedUserCannotUpdateIncidentStatus() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}")
        ).andExpect(status().isUnauthorized());
    }





    /*
     * Verifies that a user with the VIEWER role can create an incident
     * using a newly created service, avoiding conflicts with existing incidents.
     */
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
                                .with(
                                        org.springframework.security.test.web.servlet.request
                                                .SecurityMockMvcRequestPostProcessors
                                                .user("madhav")
                                                .roles("VIEWER")
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "title": "Database outage",
                                        "severity": "HIGH",
                                        "serviceId": %d
                                    }
                                    """.formatted(savedService.getId()))
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .status().isOk());
    }


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

    @Test
    void requestingNonexistentIncidentReturnsNotFound() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents/999999")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isNotFound());
    }

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

    @Test
    void viewerCanFilterIncidentsByStatus() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents/status/OPEN")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isOk());
    }

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

    // Tests that creating an incident without a title returns 400 Bad Request.
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

    // Tests that creating an incident without a service ID returns 400 Bad Request.
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

    // Tests that an invalid incident status returns 400 Bad Request.
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

    // Tests that filtering by status returns a JSON array.
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

    // Verifies that combined incident filters are accepted.
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

    // Verifies that an invalid severity filter returns 400 Bad Request.
    @Test
    void viewerCannotFilterByInvalidSeverity() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents?severity=URGENT")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isBadRequest());
    }

    // Verifies that an unsupported incident status filter returns 400 Bad Request.
    @Test
    void viewerCannotFilterByInvalidStatus() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents?status=INVALID")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isBadRequest());
    }

    // Verifies that a non-positive service ID is rejected.
    @Test
    void viewerCannotFilterByInvalidServiceId() throws Exception {
        mockMvc.perform(
                get("/api/v1/incidents?serviceId=-1")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("VIEWER"))
        ).andExpect(status().isBadRequest());
    }

    // Verifies that an invalid severity filter returns the expected error message.
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

    // Verifies that an ADMIN can assign an incident successfully.
    @Test
    void adminCanAssignIncident() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/11/assign/2")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("ADMIN"))
        ).andExpect(status().isOk());
    }

    // Verifies that assigning an incident to a nonexistent user returns 404.
    @Test
    void adminCannotAssignIncidentToNonexistentUser() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/11/assign/99999")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("ADMIN"))
        ).andExpect(status().isNotFound());
    }

    // Verifies that assigning a nonexistent incident returns 404 Not Found.
    @Test
    void adminCannotAssignNonexistentIncident() throws Exception {
        mockMvc.perform(
                patch("/api/v1/incidents/99999/assign/2")
                        .with(SecurityMockMvcRequestPostProcessors.user("madhav")
                                .roles("ADMIN"))
        ).andExpect(status().isNotFound());
    }

    // Verifies that duplicate OPEN incidents for one service are rejected.
    @Test
    void cannotCreateSecondOpenIncidentForSameService() throws Exception {
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
                                            "serviceId": 1
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("An open incident already exists for this service"));
    }
}
