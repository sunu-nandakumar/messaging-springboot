package com.example.messaging.repository;

import com.example.messaging.model.ConsumerDetails;
import com.example.messaging.model.Topics;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerRepo extends CrudRepository<ConsumerDetails, Long> {
    Optional<ConsumerDetails> findByConsumerId(String name);
}
