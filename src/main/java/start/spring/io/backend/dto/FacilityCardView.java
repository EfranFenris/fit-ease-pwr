package start.spring.io.backend.dto;

import start.spring.io.backend.model.Facility;

/**
 * This is a "Data Transfer Object" (DTO) specifically for the User Interface.
 * The raw 'Facility' from the database might be
 * ugly or missing information (like which Image URL to use or which CSS color
 * matches the status).
 * * We pack everything neatly into this 'Record' so the HTML page can simply
 * display it without having to do any difficult logic.
 */
public record FacilityCardView(
        Facility facility,          // The original data from the database
        String imageUrl,            // A pretty picture chosen by the Controller
        String location,            // Text like "Sports Hub"
        int capacity,               // How many people fit (for example 4 for Padel)
        String statusLabel,         // Text to show: "Available", "Maintenance", etc.
        String statusClass,         // CSS class for color: "green", "orange", "red"
        boolean hasActiveMaintenance // Helps the HTML decide if it should show a warning icon
) {
}