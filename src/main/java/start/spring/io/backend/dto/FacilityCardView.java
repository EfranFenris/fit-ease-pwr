package start.spring.io.backend.dto;

import start.spring.io.backend.model.Facility;

public record FacilityCardView(
        Facility facility,
        String imageUrl,
        String location,
        int capacity,
        String statusLabel,
        String statusClass) {
}
