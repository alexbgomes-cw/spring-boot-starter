package com.alexbgomes.starter.data.repository;

import com.alexbgomes.starter.data.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
}
