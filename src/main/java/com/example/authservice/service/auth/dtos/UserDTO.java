package com.example.authservice.service.auth.dtos;

import com.example.authservice.entities.Phone;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String name;
    private String email;
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private List<Phone> phones;
}