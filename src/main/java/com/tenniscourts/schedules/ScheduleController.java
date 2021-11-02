package com.tenniscourts.schedules;

import com.tenniscourts.config.BaseRestController;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/schedules")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ScheduleController extends BaseRestController {

    private final ScheduleService scheduleService;

    @PostMapping
    @ApiOperation(value = "API Operation that adds a new schedule.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "The schedule was succesfully created.")
    })
    public ResponseEntity<Void> addScheduleTennisCourt(@RequestBody CreateScheduleRequestDTO createScheduleRequestDTO) {
        return ResponseEntity.created(locationByEntity(scheduleService.addSchedule(createScheduleRequestDTO.getTennisCourtId(), createScheduleRequestDTO).getId())).build();
    }

    @GetMapping("/{startDate}/{endDate}")
    @ApiOperation(value = "API Operation thar returns a schedule by its start and end dates.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Schedule found."),
        @ApiResponse(code = 404, message = "Schedule not found.")
    })
    public ResponseEntity<List<ScheduleDTO>> findSchedulesByDates(LocalDate startDate,
                                                                  LocalDate endDate) {
        return ResponseEntity.ok(scheduleService.findSchedulesByDates(LocalDateTime.of(startDate, LocalTime.of(0, 0)), LocalDateTime.of(endDate, LocalTime.of(23, 59))));
    }

    @GetMapping("/{scheduleId}")
    @ApiOperation(value = "API Operation that returns a schedule by its ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Schedule found."),
        @ApiResponse(code = 404, message = "Schedule not found.")
    })
    public ResponseEntity<ScheduleDTO> findByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findSchedule(scheduleId));
    }

    @GetMapping("/courts/{tennisCourtId}")
    @ApiOperation(value = "API operation that return a list of schedules by tennis court ID.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Schedule found."),
            @ApiResponse(code = 404, message = "Schedule not found.")
    })
    public ResponseEntity<List<ScheduleDTO>> findScheduleTennisCourtId(@PathVariable Long tennisCourtId) {
        return ResponseEntity.ok(scheduleService.findSchedulesByTennisCourtId(tennisCourtId));
    }
}
