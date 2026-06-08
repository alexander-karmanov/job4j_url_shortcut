package ru.job4j.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.domain.Address;
import ru.job4j.domain.Site;
import ru.job4j.dto.AddressDto;
import ru.job4j.dto.SiteDto;
import ru.job4j.mapper.DtoMapper;
import ru.job4j.service.AddressService;
import ru.job4j.service.SiteService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import org.apache.commons.lang3.RandomStringUtils;

@RestController
@AllArgsConstructor
public class SiteController {

    private final AddressService addressService;
    private final SiteService userService;
    private PasswordEncoder encoder;

    @PostMapping("/registration")
    public ResponseEntity<SiteDto> registration(@Valid @RequestBody Site user) {
        String login = RandomStringUtils.randomNumeric(8);
        String password = RandomStringUtils.randomNumeric(8);

        user.setLogin(login);
        user.setPassword(encoder.encode(password));

        Optional<Site> optionalRsl = userService.saveWithUniqueConstraintHandling(user);

        if (optionalRsl.isEmpty()) {
            Optional<Site> existingSite = userService.findBySite(user.getSite());
            if (existingSite.isPresent()) {
                SiteDto regSite = new DtoMapper().getUserDto(existingSite.get());
                regSite.setRegistration(false);
                return new ResponseEntity<>(regSite, HttpStatus.CONFLICT);
            } else {
                throw new RuntimeException("Unexpected error during registration");
            }
        }

        SiteDto newRegSite = new DtoMapper().getUserDto(optionalRsl.get());
        newRegSite.setPassword(password);
        newRegSite.setRegistration(true);
        return new ResponseEntity<>(newRegSite, HttpStatus.CREATED);
    }

    @PostMapping("/convert")
    public ResponseEntity<AddressDto> convert(@Valid @RequestBody Address address) {
        AddressDto result = addressService.convertAndSave(address);
        boolean isNew = result.getCode() != null;
        return ResponseEntity
                .status(isNew ? HttpStatus.CREATED : HttpStatus.CONFLICT)
                .body(result);
    }

    @GetMapping("/redirect/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        Optional<Address> optionalAddress = addressService.findAndIncrementTotalByCode(code);

        if (optionalAddress.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        Address address = optionalAddress.get();
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header("URL", address.getUrl())
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
