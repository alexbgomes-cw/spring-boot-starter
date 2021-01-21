package com.alexbgomes.starter.data.repository;

import com.alexbgomes.starter.data.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {
    List<User> findAll();
}
