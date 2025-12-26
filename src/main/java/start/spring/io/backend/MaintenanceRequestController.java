package start.spring.io.backend;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Simple REST controller for MaintenanceRequest CRUD.
 * Basic endpoints to test create, read, update and delete.
 */
@RestController
@RequestMapping("/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService service;

    /** Injects the service layer. */
    public MaintenanceRequestController(MaintenanceRequestService service) {
        this.service = service;
    }

    /** Lists all maintenance requests. */
    @GetMapping(value = {"", "/"})
    public List<MaintenanceRequest> getAll() {
        return service.getAllRequests();
    }

    /** Gets a request by id, returns 404 if not found. */
    @GetMapping("/{id}")
    public MaintenanceRequest getOne(@PathVariable Integer id) {
        return service.getRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance request not found"));
    }

    /** Creates a new maintenance request (201 Created). */
    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRequest create(@RequestBody MaintenanceRequest request) {
        return service.createRequest(request);
    }

    /** Updates an existing request by id, 404 if not found. */
    @PutMapping("/{id}")
    public MaintenanceRequest update(@PathVariable Integer id, @RequestBody MaintenanceRequest request) {
        return service.updateRequest(id, request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance request not found"));
    }

    /** Deletes a request by id (204 No Content), 404 if not found. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!service.deleteRequest(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance request not found");
        }
    }
}
