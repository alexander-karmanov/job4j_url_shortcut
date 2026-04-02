package ru.job4j.dto;


import lombok.Builder;
import lombok.Data;

@Builder(builderMethodName = "of")
@Data
public class AddressDto {

    private String code;
}
