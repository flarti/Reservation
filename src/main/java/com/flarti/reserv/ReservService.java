package com.flarti.reserv;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    public ReservService() {
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong();
    }

    public Reservation getReservById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation By Id = " + id);
        }

        return reservationMap.get(id);
    }

    public List<Reservation> findAllReservation() {
        return reservationMap.values().stream().toList();
    }

    public Reservation createReserv(Reservation reservationToCreate) {

        if (reservationToCreate.id() != null) throw new IllegalArgumentException("Id should be empty");

        if(reservationToCreate.status() != null) throw new IllegalArgumentException("Status should be empty");

        var newReservation = new Reservation(
                idCounter.incrementAndGet(),
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(newReservation.id(), newReservation);
        return newReservation;
    }

    public Reservation updeteReserv(
            Long id,
            Reservation reservationToUpdate
    ) {
        if (!reservationMap.containsKey(id)) throw new NoSuchElementException("Not found reservation By Id = " + id);

        var reservation = reservationMap.get(id);

        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify reservation: status=" + reservation.status());
        }

        var updatedReservation = new Reservation(
                reservation.id(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(id, updatedReservation);

        return updatedReservation;
    }

    public void deleteReserv(Long id) {
        if (!reservationMap.containsKey(id)) throw new NoSuchElementException("Not found By Id = " + id);

        reservationMap.remove(id);
    }

    public Reservation approveReserv(Long id) {
        if (!reservationMap.containsKey(id)) throw new NoSuchElementException("Not found By Id = " + id);

        var reservation = reservationMap.get(id);
        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve reservation: status=" + reservation.status());
        }
        var isConflict = idReservConflict(reservation);
        if (isConflict) throw new IllegalArgumentException("Cannot approve reservation");

        var approvedReservation = new Reservation(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );

        reservationMap.put(id, approvedReservation);
        return approvedReservation;
    }

    private boolean idReservConflict(
            Reservation reservation
    ) {

        for(Reservation existingReservation : reservationMap.values()) {
            if (reservation.id().equals(existingReservation.id())) continue;
            if (!reservation.roomId().equals(existingReservation.roomId())) continue;
            if (!reservation.status().equals(ReservationStatus.APPROVED)) continue;
            if (reservation.startDate().isBefore(existingReservation.endDate())
                    && existingReservation.startDate().isBefore(reservation.endDate())) return true;
        }

        return false;
    }
}
