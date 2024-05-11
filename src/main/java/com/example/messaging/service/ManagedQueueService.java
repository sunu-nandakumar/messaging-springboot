package com.example.messaging.service;


import com.example.messaging.dto.MessageRequestDTO;
import com.example.messaging.dto.MessagingDTO;
import com.example.messaging.model.ConsumerDetails;
import com.example.messaging.model.Topics;
import com.example.messaging.repository.ConsumerRepo;
import com.example.messaging.repository.TopicRepo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ManagedQueueService {


    @Autowired
    private TopicRepo topicRepo;
    @Autowired
    private ConsumerRepo consumerRepo;

    public MessagingDTO getTopicList() {
        try {
            List<Topics> topicsList = topicRepo.findAll();
            List<String> dtoList = topicsList.stream().flatMap(val -> Stream.of(val.getName())).collect(Collectors.toList());
            return MessagingDTO.builder().statusCode(200)
                    .message("Topics Fetched.")
                    .topicList(dtoList).build();
        } catch (Exception e) {
            return MessagingDTO.builder().statusCode(500)
                    .message("unable to fetch topics.")
                    .build();
        }
    }

    public MessagingDTO addData(MessageRequestDTO messageRequestDTO) {
        try {
            Optional<Topics> topics = topicRepo.findByName(messageRequestDTO.getTopicName());
            if (topics.isEmpty()) {
                return MessagingDTO.builder().statusCode(204)
                        .message("No topic found with given name.")
                        .build();
            }
            List<String> dataList = new ArrayList<>();
            if (topics.get().getData() == null) {
                dataList.add(messageRequestDTO.getData());
            } else {
                dataList.addAll(convertStringToList(topics.get().getData()));
                dataList.add(messageRequestDTO.getData());
            }
            topics.get().setData(dataList.toString());
            topicRepo.save(topics.get());
            return MessagingDTO.builder().statusCode(200)
                    .message("Data saved to queue.").build();

        } catch (Exception e) {
            log.error("Error occurred while saving :{}", Arrays.toString(e.getStackTrace()));
            return MessagingDTO.builder().statusCode(500)
                    .message("unable to save data to queue.")
                    .build();
        }
    }

    public MessagingDTO subscribeToQueue(MessageRequestDTO messageRequestDTO) {
        try {
            Optional<Topics> topics = topicRepo.findByName(messageRequestDTO.getTopicName());
            if (topics.isEmpty()) {
                return MessagingDTO.builder().statusCode(204)
                        .message("No topic found with given name.")
                        .build();
            }
            if (messageRequestDTO.getConsumerId() == null || messageRequestDTO.getConsumerId().isEmpty()) {
                String consumerId = UUID.randomUUID().toString();
                consumerRepo.save(ConsumerDetails.builder()
                        .consumerId(consumerId)
                        .subscribedTopics(Collections.singletonList(messageRequestDTO.getTopicName()).toString())
                        .lastData(null)
                        .build());
                return MessagingDTO.builder().statusCode(200)
                        .message("successfully subscribed to the topic.")
                        .consumerId(consumerId)
                        .build();
            } else {
                Optional<ConsumerDetails> consumerDetails = consumerRepo.findByConsumerId(messageRequestDTO.getConsumerId());
                if (consumerDetails.isEmpty()) {
                    return MessagingDTO.builder().statusCode(204)
                            .message("No consumer found with given id.")
                            .build();
                } else {
                    List<String> tempList = convertStringToList(consumerDetails.get().getSubscribedTopics());
                    if (tempList.contains(messageRequestDTO.getTopicName())) {
                        return MessagingDTO.builder().statusCode(201)
                                .message("subscribed to the topic.")
                                .build();
                    }
                    tempList.add(messageRequestDTO.getTopicName());
                    consumerDetails.get().setSubscribedTopics(tempList.toString());
                    consumerRepo.save(consumerDetails.get());
                    return MessagingDTO.builder().statusCode(200)
                            .message("successfully subscribed to the topic.")
                            .consumerId(messageRequestDTO.getConsumerId())
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while saving :{}", Arrays.toString(e.getStackTrace()));
            return MessagingDTO.builder().statusCode(500)
                    .message("unable to subscribe to topic.")
                    .build();
        }
    }


    public List<String> convertStringToList(String stringList) {
        return Arrays.stream(stringList.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public MessagingDTO fetchData(MessageRequestDTO messageRequestDTO) {
        try {
            Optional<ConsumerDetails> consumerDetails = consumerRepo.findByConsumerId(messageRequestDTO.getConsumerId());
            if (consumerDetails.isEmpty()) {
                return MessagingDTO.builder().statusCode(204)
                        .message("No consumer found with given id.")
                        .build();
            }

            List<String> tempList = convertStringToList(consumerDetails.get().getSubscribedTopics());
            String topicName = messageRequestDTO.getTopicName();
            if (!tempList.contains(topicName)) {
                return MessagingDTO.builder().statusCode(201)
                        .message("You are not subscribed to the requested topic")
                        .build();
            }

            Optional<Topics> topics = topicRepo.findByName(topicName);
            if (topics.isEmpty()) {
                return MessagingDTO.builder().statusCode(204)
                        .message("No topic found with given name.")
                        .build();
            }

            String topicData = topics.get().getData();
            if (topicData == null || topicData.isEmpty()) {
                return MessagingDTO.builder().statusCode(204)
                        .message("No data present in the requested topic.")
                        .build();
            }
            List<String> topicDataList = convertStringToList(topicData);
            String lastData = consumerDetails.get().getLastData();
            JSONObject jsonObject = new JSONObject((lastData != null && !lastData.isEmpty())? lastData : "{}");
            int position = jsonObject.has(topicName) ? jsonObject.getInt(topicName) + 1 : 0;
            jsonObject.put(topicName, position);

            consumerDetails.get().setLastData(jsonObject.toString());
            consumerRepo.save(consumerDetails.get());

            return MessagingDTO.builder().statusCode(200)
                    .message("Successfully fetched data.")
                    .data(topicDataList.get(Math.min(position, topicDataList.size() - 1)))
                    .consumerId(messageRequestDTO.getConsumerId())
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while saving :{}", Arrays.toString(e.getStackTrace()));
            return MessagingDTO.builder().statusCode(500)
                    .message("unable to fetch data for topic.")
                    .build();
        }
    }
}
