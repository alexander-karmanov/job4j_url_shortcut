package ru.job4j.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.domain.Address;
import ru.job4j.domain.Site;
import ru.job4j.dto.AddressDto;
import ru.job4j.dto.SiteDto;
import ru.job4j.mapper.DtoMapper;
import ru.job4j.service.AddressService;
import ru.job4j.service.BaseConversion;
import ru.job4j.service.SiteService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


import org.apache.commons.lang3.RandomStringUtils;

@RestController
@AllArgsConstructor
public class SiteController {

    private final AddressService addressService;
    private final SiteService userService;
    private PasswordEncoder encoder;
    private BaseConversion baseConversion;

    @PostMapping("/registration")
    public ResponseEntity<SiteDto> registration(@Valid @RequestBody Site user) throws SQLException {
        Optional<Site> optionalSite = userService.findBySite(user.getSite());
        if (!optionalSite.isEmpty()) {
            SiteDto regSite = new DtoMapper().getUserDto(optionalSite.get());
            regSite.setRegistration(false);
            return new ResponseEntity<SiteDto>(regSite, HttpStatus.CONFLICT);
        }

        String login = RandomStringUtils.randomNumeric(8);
        String password = RandomStringUtils.randomNumeric(8);

        user.setLogin(login);
        user.setPassword(encoder.encode(password));
        Optional<Site> optionalRsl = userService.save(user);
        if (optionalRsl.isEmpty()) {
            throw new SQLException("Error!");
        }
        SiteDto newRegSite = new DtoMapper().getUserDto(optionalRsl.get());
        newRegSite.setPassword(password);
        newRegSite.setRegistration(true);
        return new ResponseEntity<SiteDto>(newRegSite,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/convert")
    public ResponseEntity<AddressDto> convert(@Valid @RequestBody Address address) throws SQLException {
        Optional<Address> optionalAddress = addressService.findByUrl(address.getUrl());
        if (!optionalAddress.isEmpty()) {
            AddressDto oldCode = new DtoMapper().getAddressDto(optionalAddress.get());
            return new ResponseEntity<AddressDto>(oldCode, HttpStatus.CONFLICT);
        }

        Optional<Address> optionalRsl = addressService.save(address);
        if (optionalRsl.isEmpty()) {
            throw new SQLException("Error!");
        }
        Address rsl = optionalRsl.get();
        String code = baseConversion.encode(rsl.getId());
        rsl.setCode(code);
        return new ResponseEntity<AddressDto>(new DtoMapper().getAddressDto(rsl),
                addressService.update(rsl) ? HttpStatus.CREATED : HttpStatus.NO_CONTENT);
    }

    @GetMapping("/redirect/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        Optional<Address> optionalAddress = addressService.findByCode(code);
        if (optionalAddress.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        }
        Address address = optionalAddress.get();
        addressService.update(address);
        addressService.increaseTotal(code);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header("URL", optionalAddress.get().getUrl())
                .build();
    }

    @GetMapping("/statistic")
    public ResponseEntity<List<Map<String, String>>> statistic() {
        List<Address> addressList = addressService.findAll();
        List<Map<String, String>> listToMap = addressList
                .stream()
                .map(f -> Map.of(
                        "url",
                        f.getUrl(),
                        "total",
                        String.valueOf(f.getTotal()))
                )
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(listToMap);
    }
}
