package com.example.messaging.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity(name = "consumer_details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native")
    private long id;
    @Column(name = "consumer_id")
    private String consumerId;
    @Column(name = "subscribed_topics")
    private String subscribedTopics;
    @Column(name = "last_data")
    private String lastData;
}
