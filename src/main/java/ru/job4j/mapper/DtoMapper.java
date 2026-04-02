package ru.job4j.mapper;

import org.springframework.stereotype.Component;
import ru.job4j.domain.Address;
import ru.job4j.domain.Site;
import ru.job4j.dto.AddressDto;
import ru.job4j.dto.SiteDto;

@Component
public class DtoMapper {

    public SiteDto getUserDto(Site user) {
        return SiteDto.of()
                .login(user.getLogin())
                .build();
    }

    public AddressDto getAddressDto(Address address) {
        return AddressDto.of()
                .code(address.getCode())
                .build();
    }
}
