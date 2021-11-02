package com.tenniscourts.schedules;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.tenniscourts.tenniscourts.TennisCourtService;
import com.tenniscourts.exceptions.BusinessException;
import com.tenniscourts.exceptions.EntityNotFoundException;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private TennisCourtService tennisCourtService;

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        ScheduleDTO scheduleDTO = createScheduleDTO(createScheduleRequestDTO);

        verifyAddSchedule(scheduleDTO);

        return scheduleMapper.map(scheduleRepository.saveAndFlush(scheduleMapper.map(scheduleDTO)));
    }

    private void verifyAddSchedule(ScheduleDTO scheduleDTO) {
        LocalDateTime startDateTime = scheduleDTO.getStartDateTime();
        LocalDateTime endDateTime = scheduleDTO.getEndDateTime();

        if (startDateTime.isBefore(LocalDateTime.now())) {
            throwBusinessException("It is not possible to schedule before the local date time.");
        }
        if (endDateTime.isBefore(startDateTime)) {
            throwBusinessException("It is not possible to schedule before the start date time.");
        }
    }

    private ScheduleDTO createScheduleDTO(CreateScheduleRequestDTO createScheduleRequestDTO) {
        return ScheduleDTO.builder()
                .startDateTime(createScheduleRequestDTO.getStartDateTime())
                .endDateTime(createScheduleRequestDTO.getEndDateTime())
                .tennisCourtId(createScheduleRequestDTO.getTennisCourtId())
                .tennisCourt(tennisCourtService.findTennisCourtById(createScheduleRequestDTO.getTennisCourtId()))
                .build();
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        //TODO: implement
        return null;
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .map(scheduleMapper::map)
                .orElseThrow(() -> throwEntityNotFoundException(scheduleId));
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }

    private EntityNotFoundException throwEntityNotFoundException(Object schedule) {
        return new EntityNotFoundException(String.format("Schedule %s was not found.", schedule));
    }

    private void throwBusinessException(String message) {
        throw new BusinessException(message);
    }
}
