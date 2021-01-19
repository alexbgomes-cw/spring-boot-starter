package com.alexbgomes.starter.business.service;

import com.alexbgomes.starter.business.domain.ValidationLevel;
import com.alexbgomes.starter.data.entity.User;
import com.alexbgomes.starter.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        List<User> result = new ArrayList<>();
        Iterable<User> iUsers = this.userRepository.findAll();
        iUsers.forEach(result::add);

        return result;
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findById(username);
    }

    public ValidationLevel validate(User user, boolean login) {
        if (login) {
            if (getUserByUsername(user.getUsername()).isEmpty())
                return ValidationLevel.USERNOTFOUND;
        } else {
            if (getUserByUsername(user.getUsername()).isPresent())
                return ValidationLevel.USERTAKEN;
        }
        
        if (user.getUsername().length() < 8 || user.getUsername().length() > 30)
            return ValidationLevel.USERLEN;

        if (!Pattern.matches("^[a-zA-Z0-9]+$", user.getUsername()))
            return ValidationLevel.USERINVALID;

        if (user.getPwd().length() < 10 || user.getPwd().length() > 18)
            return ValidationLevel.PASSLEN;

        if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$", user.getPwd()))
            return ValidationLevel.PASSINVALID;
        
        return ValidationLevel.CLEAR;
    }

    public Optional<ResponseEntity> getResponseIfInvalid(User user) {
        return getResponseIfInvalid(user, false);
    }

    public Optional<ResponseEntity> getResponseIfInvalid(User user, boolean login) {
        Optional<ResponseEntity> result = Optional.empty();

        switch (validate(user, login)) {
            case USERTAKEN:
                result = Optional.of(new ResponseEntity(String.format("Username %s is already taken.", user.getUsername()), HttpStatus.CONFLICT));
                break;
            case USERNOTFOUND:
                result = Optional.of(new ResponseEntity(String.format("User %s not found.", user.getUsername()), HttpStatus.NOT_FOUND));
                break;
            case USERLEN:
                result = Optional.of(new ResponseEntity("Username must be at least 8 characters and at most 30 characters.", HttpStatus.BAD_REQUEST));
                break;
            case PASSLEN:
                result = Optional.of(new ResponseEntity("Password must be at least 10 characters and at most 18 characters.", HttpStatus.BAD_REQUEST));
                break;
            case USERINVALID:
                result = Optional.of(new ResponseEntity("Username must be alphanumeric only.", HttpStatus.BAD_REQUEST));
                break;
            case PASSINVALID:
                result = Optional.of(new ResponseEntity("Password must contain an uppercase, a lowercase, a number, and a special character.", HttpStatus.BAD_REQUEST));
                break;
        }

        return result;
    }

    public ResponseEntity loginUser(User user) {
        Optional<User> thisExistingUser = getUserByUsername(user.getUsername());
        if (thisExistingUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(user.getPwd(), thisExistingUser.get().getPwd())) {
                return new ResponseEntity(String.format("User %s logged in at %s.", user.getUsername(), LocalDateTime.now()), HttpStatus.OK);
            } else {
                return new ResponseEntity("Password is incorrect.", HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity(String.format("User %s not found.", user.getUsername()), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity setUser(User user) {
        user.setPwd(bCryptPasswordEncoder.encode(user.getPwd()));
        userRepository.save(user);
        return new ResponseEntity("New user created.", HttpStatus.OK);
    }

    public ResponseEntity rmUser(User user) {
        Optional<User> thisExistingUser = getUserByUsername(user.getUsername());
        if (thisExistingUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(user.getPwd(), thisExistingUser.get().getPwd())) {
                userRepository.deleteById(user.getUsername());
                return new ResponseEntity(String.format("User %s removed.", user.getUsername()), HttpStatus.OK);
            } else {
                return new ResponseEntity("Password is incorrect.", HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity(String.format("User %s not found.", user.getUsername()), HttpStatus.NOT_FOUND);
    }
}
