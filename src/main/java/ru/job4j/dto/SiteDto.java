package ru.job4j.dto;

import lombok.Builder;
import lombok.Data;

@Builder(builderMethodName = "of")
@Data
public class SiteDto {

    private boolean registration;

    private String login;

    private String password;


}
