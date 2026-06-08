package ru.job4j.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.job4j.domain.Address;
import ru.job4j.dto.AddressDto;
import ru.job4j.mapper.DtoMapper;
import ru.job4j.repository.AddressRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AddressService {

    private AddressRepository addressRepository;

    private final BaseConversion baseConversion;

    private static final Logger LOG = LoggerFactory.getLogger(AddressService.class.getName());

    public Optional<Address> save(Address address) {
        Optional<Address> rsl = Optional.empty();
        try {
            rsl = Optional.of(addressRepository.save(address));
        } catch (DataIntegrityViolationException e) {
            LOG.error("Failed to save address due to data integrity violation. "
                            + "URL: {}, Error: {}",
                    address.getUrl(), e.getMessage(), e);
        }
        return rsl;
    }

    public Optional<Address> findByUrl(String url) {
        Optional<Address> rsl = Optional.empty();
        try {
            rsl = addressRepository.findByUrl(url);
        } catch (Exception e) {
            LOG.error("Failed to find address by URL: '{}'. Error: {}", url, e.getMessage(), e);
        }
        return rsl;
    }

    @Transactional
    public AddressDto convertAndSave(Address address) {
        Optional<Address> existing = addressRepository.findByUrl(address.getUrl());
        if (existing.isPresent()) {
            return new DtoMapper().getAddressDto(existing.get());
        }

        Address saved = addressRepository.save(address);
        String code = baseConversion.encode(saved.getId());
        saved.setCode(code);
        addressRepository.save(saved);

        return new DtoMapper().getAddressDto(saved);
    }

    public boolean update(Address address) {
        addressRepository.save(address);
        return true;
    }

    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    @Transactional
    public Optional<Address> findAndIncrementTotalByCode(String code) {
        try {
            addressRepository.increaseTotal(code);
            Optional<Address> rsl = Optional.ofNullable(addressRepository.findByCode(code));
            return rsl;
        } catch (Exception e) {
            LOG.error("Failed to increment total and find address by code: '{}'. "
                            + "Operation: {}, Error type: {}, Message: {}",
                    code,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);
            return Optional.empty();
        }
    }
}
