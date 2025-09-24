package com.flarti.reservation.reservations.avaliability;

import com.flarti.reservation.reservations.Reservation;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CheckAvailabilityRequest(
        @NotNull
        Long roomId,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate
) {

}
