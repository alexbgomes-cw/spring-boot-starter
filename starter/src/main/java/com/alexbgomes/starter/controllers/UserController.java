package com.alexbgomes.starter.controllers;

import com.alexbgomes.starter.business.domain.ValidationLevel;
import com.alexbgomes.starter.business.service.UserService;
import com.alexbgomes.starter.data.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/api/users")
    public List<User> Users() {
        return userService.getUsers();
    }

    @PostMapping("/api/register")
    public ResponseEntity registerUser(@RequestBody User user) {
        Optional<ResponseEntity> invalidReponse = userService.getResponseIfInvalid(user);
        if (invalidReponse.isPresent())
            return invalidReponse.get();

        return userService.setUser(user);
    }

    @PostMapping("/api/login")
    public ResponseEntity loginUser(@RequestBody User user) {
        Optional<ResponseEntity> invalidResponse = userService.getResponseIfInvalid(user, true);
        if (invalidResponse.isPresent())
            return invalidResponse.get();

        return userService.loginUser(user);
    }

    @PostMapping("/api/unregister")
    public ResponseEntity unregisterUser(@RequestBody User user) {
        Optional<ResponseEntity> invalidResponse = userService.getResponseIfInvalid(user, true);
        if (invalidResponse.isPresent())
            return invalidResponse.get();

        return userService.rmUser(user);
    }
}
