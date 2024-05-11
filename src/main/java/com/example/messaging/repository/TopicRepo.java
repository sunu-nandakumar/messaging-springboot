package com.example.messaging.repository;

import com.example.messaging.model.Topics;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepo extends CrudRepository<Topics, Long> {
    List<Topics> findAll();

    Optional<Topics> findByName(String name);
}
