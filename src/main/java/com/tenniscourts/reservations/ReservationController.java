package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import com.tenniscourts.reschedule.RescheduleRequestDTO;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/reservations")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @PostMapping("/book")
    @ApiOperation(value = "API operation to create a new reservation.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Reservation was succesfully created.")
    })
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    @GetMapping
    @ApiOperation(value = "API operation that returns all reservations.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of all reservations found."),
            @ApiResponse(code = 404, message = "List of all reservations not found.")
    })
    public ResponseEntity<List<ReservationDTO>> findAllReservations() {
        return ResponseEntity.ok(reservationService.findAllReservations());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "API operation to return a reservation by its ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Reservation found."),
        @ApiResponse(code = 404, message = "Reservation nout found.")
    })
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "API operation that cancel a reservation by ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Reservation was succesfully deleted."),
        @ApiResponse(code = 404, message = "Reservation to be deleted was not found.")
    })
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @PutMapping("/reschedule/{reservationId}/{scheduleId}")
    @ApiOperation(value = "API operation to reschedule a reservation by ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Reservation was succesfully rescheduled."),
        @ApiResponse(code = 404, message = "Reservation to be rescheduled was not found.")
    })
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long reservationId, @PathVariable Long scheduleId, @RequestBody RescheduleRequestDTO rescheduleRequestDTO) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, scheduleId, rescheduleRequestDTO));
    }
}
