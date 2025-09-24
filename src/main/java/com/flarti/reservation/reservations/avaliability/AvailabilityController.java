package com.flarti.reservation.reservations.avaliability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class AvailabilityController {
    private final ReservationAvailabilityService service;

    Logger log = LoggerFactory.getLogger(AvailabilityController.class);

    public AvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @Valid CheckAvailabilityRequest request
    ) {
        log.info("Called checkAvailability: request: {}", request);
        boolean isAvailable = service.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );

        var message = isAvailable ? "Room available to reservation" : "Room not available to reservations";
        var status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.RESERVED;

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CheckAvailabilityResponse(message, status));
    }
}
