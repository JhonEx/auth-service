package com.example.authservice.controller;

import com.example.authservice.service.auth.AuthenticationService;
import com.example.authservice.service.auth.dtos.AuthResponseDTO;
import com.example.authservice.service.auth.dtos.LoginRequestDTO;
import com.example.authservice.service.auth.dtos.RegisterUserRequestDTO;
import com.example.authservice.service.auth.dtos.UserDTO;
import com.example.authservice.utils.exceptions.AppExceptionConstants;
import com.example.authservice.utils.exceptions.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        log.info("Authentication API: login requested by user: ", loginRequest.getUsername());
        AuthResponseDTO authResponseDTO = authenticationService.loginUser(loginRequest);
        return new ResponseEntity<>(authResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterUserRequestDTO registerUserRequestDTO) {
        log.info("Authentication API: createUser: ", registerUserRequestDTO.getEmail());
        UserDTO userDTO;
        try {
            userDTO = authenticationService.createUser(registerUserRequestDTO);
        } catch (DataIntegrityViolationException | SQLIntegrityConstraintViolationException e) {
            throw new BadRequestException(AppExceptionConstants.USER_RECORD_FOUND);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }
}