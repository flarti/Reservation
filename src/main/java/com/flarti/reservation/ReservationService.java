package com.flarti.reservation;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong idCounter;

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong();
    }

    public Reservation getReservationById(Long id) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation By Id = " + id);
        }

        return reservationMap.get(id);
    }

    public List<Reservation> findAllReservation() {
        List<ReservationEntity> allEntities = reservationRepository.findAll();


        return allEntities.stream()
                .map(it ->
                    new Reservation(
                            it.getId(),
                            it.getUserId(),
                            it.getRoomId(),
                            it.getStartDate(),
                            it.getEndDate(),
                            it.getStatus()
                    )
                ).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

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

    public Reservation updeteReservation(
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

    public void deleteReservation(Long id) {
        if (!reservationMap.containsKey(id)) throw new NoSuchElementException("Not found By Id = " + id);

        reservationMap.remove(id);
    }

    public Reservation approveReservation(Long id) {
        if (!reservationMap.containsKey(id)) throw new NoSuchElementException("Not found By Id = " + id);

        var reservation = reservationMap.get(id);
        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve reservation: status=" + reservation.status());
        }
        var isConflict = idReservationConflict(reservation);
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

    private boolean idReservationConflict(
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
