package com.flarti.reservation.reservations.avaliability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {
}
