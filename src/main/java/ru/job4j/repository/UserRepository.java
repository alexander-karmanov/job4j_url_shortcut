package ru.job4j.repository;

import org.springframework.data.repository.CrudRepository;
import ru.job4j.domain.Site;

import java.util.Optional;

public interface UserRepository extends CrudRepository<Site, Integer> {

    Optional<Site> findBySite(String site);

    Site findByLogin(String login);
}
