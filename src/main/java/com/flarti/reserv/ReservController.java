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
        log.info("Called getReservById id=" + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservService.getReservById(id));
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
        return ResponseEntity.status(201)
                .body(reservService.createReserv(reservationToCreate));
    }
}
