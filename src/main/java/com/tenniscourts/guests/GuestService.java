package com.tenniscourts.guests;

import org.springframework.stereotype.Service;

import com.tenniscourts.exceptions.EntityNotFoundException;

import lombok.AllArgsConstructor;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public GuestDTO addGuest(GuestDTO guestDTO) {
        return guestMapper.map(guestRepository.saveAndFlush(guestMapper.map(guestDTO)));
    }

    public GuestDTO updateGuest(GuestDTO guestDTO, Long id) {
        Guest guest = guestRepository.findById(id).orElseThrow(() -> throwEntityNotFoundException(id));
        guest.setName(guestDTO.getName());

        return guestMapper.map(guestRepository.save(guest));
    }

    public GuestDTO deleteGuest(Long id) {
        return guestMapper.map(this.delete(id));
    }

    private Guest delete(Long id) {
        return guestRepository.findById(id)
                .map(guest -> {
                    guestRepository.delete(guest);
                    return guest;
                }).orElseThrow(() -> throwEntityNotFoundException(id));
    }

    public GuestDTO findGuestById(Long id) {
        return guestRepository.findById(id)
                .map(guestMapper::map)
                .orElseThrow(() -> throwEntityNotFoundException(id));
    }

    public GuestDTO findGuestByName(String name) {
        return guestRepository.findByName(name)
                .map(guestMapper::map)
                .orElseThrow(() -> throwEntityNotFoundException(name));
    }

    public List<GuestDTO> findAllGuests() {
        return guestRepository.findAll().stream().map(guestMapper::map).collect(toList());
    }

    private EntityNotFoundException throwEntityNotFoundException(Object guest) {
        return new EntityNotFoundException(String.format("Guest %s not found.", guest));
    }
}
