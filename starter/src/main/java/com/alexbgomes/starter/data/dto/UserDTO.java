package com.alexbgomes.starter.data.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserDTO {
    @Id
    @NotNull
    private String username;

    @NotNull
    private String pwd;
}
