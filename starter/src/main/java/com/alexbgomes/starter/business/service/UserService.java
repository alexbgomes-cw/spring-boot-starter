package com.alexbgomes.starter.business.service;

import com.alexbgomes.starter.business.domain.ValidationLevel;
import com.alexbgomes.starter.data.entity.User;
import com.alexbgomes.starter.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/* TODO: Remove validation from here into Custom Validator, preserve messages to return
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    private Optional<User> getUserByUsername(String username) {
        return userRepository.findById(username);
    }

    private String userNotFoundString(User user) {
        return String.format("User %s not found.", user.getUsername());
    }

    private ValidationLevel validate(User user, boolean login) {
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

    public Optional<ResponseEntity<String>> getResponseIfInvalid(User user) {
        return getResponseIfInvalid(user, false);
    }

    public Optional<ResponseEntity<String>> getResponseIfInvalid(User user, boolean login) {
        Optional<ResponseEntity<String>> result = Optional.empty();

        switch (validate(user, login)) {
            case USERTAKEN:
                result = Optional.of(new ResponseEntity<>(String.format("Username %s is already taken.", user.getUsername()), HttpStatus.CONFLICT));
                break;
            case USERNOTFOUND:
                result = Optional.of(new ResponseEntity<>(userNotFoundString(user), HttpStatus.NOT_FOUND));
                break;
            case USERLEN:
                result = Optional.of(new ResponseEntity<>("Username must be at least 8 characters and at most 30 characters.", HttpStatus.BAD_REQUEST));
                break;
            case PASSLEN:
                result = Optional.of(new ResponseEntity<>("Password must be at least 10 characters and at most 18 characters.", HttpStatus.BAD_REQUEST));
                break;
            case USERINVALID:
                result = Optional.of(new ResponseEntity<>("Username must be alphanumeric only.", HttpStatus.BAD_REQUEST));
                break;
            case PASSINVALID:
                result = Optional.of(new ResponseEntity<>("Password must contain an uppercase, a lowercase, a number, and a special character.", HttpStatus.BAD_REQUEST));
                break;
            case CLEAR:
                break;
        }

        return result;
    }

    public ResponseEntity<String> loginUser(User user) {
        Optional<User> thisExistingUser = getUserByUsername(user.getUsername());
        if (thisExistingUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(user.getPwd(), thisExistingUser.get().getPwd())) {
                return new ResponseEntity<>(String.format("User %s logged in at %s.", user.getUsername(), LocalDateTime.now()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Password is incorrect.", HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(userNotFoundString(user), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> setUser(User user) {
        user.setPwd(bCryptPasswordEncoder.encode(user.getPwd()));
        userRepository.save(user);
        return new ResponseEntity<>("New user created.", HttpStatus.CREATED);
    }

    public ResponseEntity<String> rmUser(User user) {
        Optional<User> thisExistingUser = getUserByUsername(user.getUsername());
        if (thisExistingUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(user.getPwd(), thisExistingUser.get().getPwd())) {
                userRepository.deleteById(user.getUsername());
                return new ResponseEntity<>(String.format("User %s removed.", user.getUsername()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Password is incorrect.", HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(userNotFoundString(user), HttpStatus.NOT_FOUND);
    }
}
