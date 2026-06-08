package ru.job4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.job4j.controller.SiteController;
import ru.job4j.domain.Address;
import ru.job4j.domain.Site;
import ru.job4j.dto.SiteDto;
import ru.job4j.mapper.DtoMapper;
import ru.job4j.service.AddressService;
import ru.job4j.service.BaseConversion;
import ru.job4j.service.SiteService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SiteControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AddressService addressService;

    @Mock
    private SiteService userService;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private BaseConversion baseConversion;

    @InjectMocks
    private SiteController siteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(siteController).build();
    }

    @Test
    void whenRegisterExistingSiteThenReturnConflict() throws Exception {
        Site existingSite = new Site();
        existingSite.setSite("existing.com");

        SiteDto regSite = new DtoMapper().getUserDto(existingSite);
        regSite.setRegistration(false);

        when(userService.findBySite("existing.com")).thenReturn(Optional.of(existingSite));

        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"site\": \"existing.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.registration").value(false));
    }

    @Test
    void whenRegisterNewSiteThenReturnCreated() throws Exception {
        Site newSite = new Site();
        newSite.setSite("new.com");
        when(userService.findBySite("new.com")).thenReturn(Optional.empty());
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        Site savedSite = new Site();
        savedSite.setId(1);
        savedSite.setSite("new.com");
        savedSite.setLogin("12345678");
        savedSite.setPassword("encodedPassword");

        when(userService.saveWithUniqueConstraintHandling(any(Site.class)))
                .thenReturn(Optional.of(savedSite));

        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"site\": \"new.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registration").value(true))
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.password").isString())
                .andExpect(jsonPath("$.login").value("12345678"));
    }

    @Test
    void whenRedirectThenReturnFound() throws Exception {
        String existingCode = "abc123";

        Address mockAddress = new Address();
        mockAddress.setId(1);
        mockAddress.setCode(existingCode);
        mockAddress.setUrl("http://test.com");

        when(addressService.findAndIncrementTotalByCode(existingCode)).thenReturn(Optional.of(mockAddress));
        when(addressService.update(any(Address.class))).thenReturn(true);

        mockMvc.perform(get("/redirect/{code}", existingCode))
                .andExpect(status().isFound())
                .andExpect(header().string("URL", "http://test.com"));
    }

    @Test
    void whenGetStatisticsThenReturnData() throws Exception {
        Address address1 = new Address();
        address1.setUrl("url1.com");
        address1.setTotal(5);

        Address address2 = new Address();
        address2.setUrl("url2.com");
        address2.setTotal(10);

        List<Address> addressList = List.of(address1, address2);

        when(addressService.findAll()).thenReturn(addressList);

        mockMvc.perform(get("/statistic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("url1.com"))
                .andExpect(jsonPath("$[0].total").value("5"))
                .andExpect(jsonPath("$[1].url").value("url2.com"))
                .andExpect(jsonPath("$[1].total").value("10"));
    }
}
