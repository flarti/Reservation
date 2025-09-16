package com.flarti.reserv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservController {

    private static final Logger log = LoggerFactory.getLogger(ReservController.class);

    private final ReservService reservService;

    public ReservController(ReservService reservService) {
        this.reservService = reservService;
    }

    @GetMapping("/{id}")
    public Reservation getReservById(@PathVariable("id") Long id) {
        log.info("Called getReservById id=" + id);
        return reservService.getReservById(id);
    }

    @GetMapping()
    public List<Reservation> getAllReserv() {
        log.info("Called getAllReserv");
        return reservService.findAllReservation();
    }
}
