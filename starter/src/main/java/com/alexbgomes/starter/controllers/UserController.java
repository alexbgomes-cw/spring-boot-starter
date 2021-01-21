package com.alexbgomes.starter.controllers;

import com.alexbgomes.starter.business.service.UserService;
import com.alexbgomes.starter.data.dto.UserDTO;
import com.alexbgomes.starter.data.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    private static final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private UserService userService;

    @GetMapping("/api/users")
    public List<User> users() {
        return userService.getUsers();
    }

    @PostMapping("/api/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        Optional<ResponseEntity<String>> invalidResponse = userService.getResponseIfInvalid(user);
        return invalidResponse.orElse(userService.setUser((user)));
    }

    @PostMapping("/api/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        Optional<ResponseEntity<String>> invalidResponse = userService.getResponseIfInvalid(user, true);
        return invalidResponse.orElse(userService.loginUser((user)));
    }

    @PostMapping("/api/unregister")
    public ResponseEntity<String> unregisterUser(@RequestBody UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        Optional<ResponseEntity<String>> invalidResponse = userService.getResponseIfInvalid(user, true);
        return invalidResponse.orElse(userService.rmUser((user)));
    }
}
