package com.flarti.reservation.reservations;


import com.flarti.reservation.reservations.avaliability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReservationService {

    final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;

    private final ReservationMapper mapper;

    private final ReservationAvailabilityService availabilityService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationMapper mapper, ReservationAvailabilityService availabilityService
    ) {
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));


        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {

        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;

        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEntities = reservationRepository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );


        return allEntities.stream()
                .map(mapper::toDomain).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.status() != null) throw new IllegalArgumentException("Status should be empty");

        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier than end date");
        }

        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = reservationRepository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(
            Long id,
            Reservation reservationToUpdate
    ) {

        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify reservation: status=" + reservationEntity.getStatus());
        }

        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day earlier than end date");
        }

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        return mapper.toDomain(reservationRepository.save(reservationToSave));
    }

    @Transactional
    public void cancelReservation(Long id) {
        if (!reservationRepository.existsById(id)) throw new EntityNotFoundException("Reservation not found for id " + id);

        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager.");
        }

        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel the reservation. Reservation has been cancelled.");
        }

        reservationRepository.setStatus(id, ReservationStatus.CANCELLED);

        logger.info("Successfully cancelled reservation: id={}", id);
    }

    public Reservation approveReservation(Long id) {

        var reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found for id " + id));


        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve reservation: status=" + reservationEntity.getStatus());
        }

        var isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if (!isAvailableToApprove) throw new IllegalArgumentException("Cannot approve reservation");

        reservationEntity.setStatus(ReservationStatus.APPROVED);

        return mapper.toDomain(reservationRepository.save(reservationEntity));
    }
}
