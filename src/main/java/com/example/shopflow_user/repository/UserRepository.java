package com.example.shopflow_user.repository;

import com.example.shopflow_user.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User,Long> {

    Mono<Boolean> existsByEmail(String email);

    Mono<User> findByEmail(String email);
}
