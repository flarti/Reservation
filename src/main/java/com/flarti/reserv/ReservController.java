package com.flarti.reserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservController {

    private static final Logger log = LoggerFactory.getLogger(ReservController.class);

    private final ReservService reservService;

    public ReservController(ReservService reservService) {
        this.reservService = reservService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservById(@PathVariable("id") Long id) {
        log.info("Called getReservById id={}", id);

        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservService.getReservById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }

    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReserv() {
        log.info("Called getAllReserv");
        return ResponseEntity.ok(reservService.findAllReservation());
    }

    @PostMapping
    public ResponseEntity<Reservation> createReserv(
            @RequestBody Reservation reservationToCreate
    ) {
        log.info("Called createReserv");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservService.createReserv(reservationToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReserv(
            @PathVariable("id") Long id,
            @RequestBody Reservation reservationToUpdate
    ) {
        log.info("Called updateReserv id={} reservationToUpdate={}", id, reservationToUpdate);

        var updated = reservService.updeteReserv(id, reservationToUpdate);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReserv(
            @PathVariable("id") Long id
    ) {
        log.info("Called deleteReserv id={}", id);

        try {
            reservService.deleteReserv(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReserv(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReserv: id={}", id);
        var reservation = reservService.approveReserv(id);
        return ResponseEntity.ok(reservation);
    }
}
