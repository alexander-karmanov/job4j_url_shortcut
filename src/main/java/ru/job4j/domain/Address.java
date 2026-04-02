package ru.job4j.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity(name = "address")
@Table(name = "address")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @EqualsAndHashCode.Include
    @NotBlank(message = "Title must be not empty")
    private String url;

    private String code;

    private int total = 1;
}
