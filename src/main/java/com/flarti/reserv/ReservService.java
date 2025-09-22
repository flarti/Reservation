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
        return null;
    }

    public void deleteReserv(Long id) {
        if (!reservationMap.containsKey(id)) throw new NoSuchElementException("Not found By Id = " + id);

        reservationMap.remove(id);
    }
}
