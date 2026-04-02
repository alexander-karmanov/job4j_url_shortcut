package ru.job4j.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.job4j.domain.Address;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends CrudRepository<Address, Integer> {

    Optional<Address> findByUrl(String url);

    Address findByCode(String code);

    List<Address> findAll();

    @Modifying
    @Transactional
    @Query("UPDATE address a SET a.total = a.total + 1 WHERE a.code = :code")
    void increaseTotal(String code);
}
