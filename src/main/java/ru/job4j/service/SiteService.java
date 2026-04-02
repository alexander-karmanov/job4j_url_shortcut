package ru.job4j.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.job4j.domain.Site;
import ru.job4j.repository.UserRepository;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
@AllArgsConstructor
public class SiteService implements UserDetailsService {

    private final UserRepository userRepository;

    private static final Logger LOG = LoggerFactory.getLogger(SiteService.class.getName());

    public Optional<Site> save(Site user) {
        Optional<Site> rsl = Optional.empty();
        try {
            rsl = Optional.of(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            LOG.error("Error!", e);
        }
        return rsl;
    }

    public Optional<Site> findBySite(String site) {
        Optional<Site> rsl = Optional.empty();
        try {
            rsl = userRepository.findBySite(site);
        } catch (Exception e) {
            LOG.error("Error!", e);
        }
        return rsl;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Site user = userRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(), emptyList());
    }
}
