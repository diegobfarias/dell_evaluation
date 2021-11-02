package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.GuestService;
import com.tenniscourts.reschedule.RescheduleRequestDTO;
import com.tenniscourts.schedules.ScheduleService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class ReservationService {

    private static final BigDecimal RESERVATION_VALUE = BigDecimal.TEN;

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final ScheduleService scheduleService;
    
    private final GuestService guestService;

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        ReservationDTO reservationDTO = createReservationDTO(createReservationRequestDTO);

        return reservationMapper.map(reservationRepository.saveAndFlush(reservationMapper.map(reservationDTO)));
    }

    private ReservationDTO createReservationDTO(CreateReservationRequestDTO createReservationRequestDTO) {
        return ReservationDTO.builder()
                .schedule(scheduleService.findSchedule(createReservationRequestDTO.getScheduleId()))
                .guest(guestService.findGuestById(createReservationRequestDTO.getGuestId()))
                .value(RESERVATION_VALUE)
                .startDateTime(createReservationRequestDTO.getStartDateTime())
                .endDateTime(createReservationRequestDTO.getStartDateTime().plusHours(1))
                .build();
    }

    public List<ReservationDTO> findAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::map)
                .collect(toList());
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellation(reservation);

            BigDecimal refundValue = getRefundValue(LocalDateTime.now(), reservation);
            return this.updateReservation(reservation, refundValue, ReservationStatus.CANCELLED);

        }).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    public BigDecimal getRefundValue(LocalDateTime referenceDate, Reservation reservation) {
        BigDecimal tax = BigDecimal.ZERO;

        long minutes = ChronoUnit.MINUTES.between(referenceDate, reservation.getStartDateTime());
        if (minutes >= 1440) {
            tax = BigDecimal.ONE;
        } else if (minutes >= 720) {
            tax = BigDecimal.valueOf(0.75);
        } else if (minutes >= 120) {
            tax = BigDecimal.valueOf(0.50);
        } else if (minutes >= 1) {
            tax = BigDecimal.valueOf(0.25);
        }

        return reservation.getValue().multiply(tax);
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId, RescheduleRequestDTO rescheduleRequestDTO) {
        Reservation previousReservation = cancel(previousReservationId);

        previousReservation.setReservationStatus(ReservationStatus.RESCHEDULED);
        reservationRepository.save(previousReservation);

        ReservationDTO newReservation = bookReservation(CreateReservationRequestDTO.builder()
                .guestId(previousReservation.getGuest().getId())
                .scheduleId(scheduleId)
                .startDateTime(rescheduleRequestDTO.getNewStartDateTime())
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));
        return newReservation;
    }
}
