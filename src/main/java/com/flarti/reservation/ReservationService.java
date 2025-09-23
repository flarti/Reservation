package com.flarti.reservation;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));


        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservation() {
        List<ReservationEntity> allEntities = reservationRepository.findAll();


        return allEntities.stream()
                .map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if (reservationToCreate.id() != null) throw new IllegalArgumentException("Id should be empty");

        if(reservationToCreate.status() != null) throw new IllegalArgumentException("Status should be empty");

        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        var savedEntity = reservationRepository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updeteReservation(
            Long id,
            Reservation reservationToUpdate
    ) {

        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify reservation: status=" + reservationEntity.getStatus());
        }

        var updatedReservation = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        return toDomainReservation(reservationRepository.save(updatedReservation));
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) throw new EntityNotFoundException("Reservation not found for id " + id);

        reservationRepository.deleteById(id);
    }

    public Reservation approveReservation(Long id) {

        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));


        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve reservation: status=" + reservationEntity.getStatus());
        }

        var isConflict = isReservationConflict(reservationEntity);

        if (isConflict) throw new IllegalArgumentException("Cannot approve reservation");

        reservationEntity.setStatus(ReservationStatus.APPROVED);

        return toDomainReservation(reservationRepository.save(reservationEntity));
    }

    private boolean isReservationConflict(
            ReservationEntity reservation
    ) {

        var allReservations = reservationRepository.findAll();

        for(ReservationEntity existingReservation : allReservations) {
            if (reservation.getId().equals(existingReservation.getId())) continue;
            if (!reservation.getRoomId().equals(existingReservation.getRoomId())) continue;
            if (!reservation.getStatus().equals(ReservationStatus.APPROVED)) continue;
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate())
                    && existingReservation.getStartDate().isBefore(reservation.getEndDate())) return true;
        }

        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }
}
