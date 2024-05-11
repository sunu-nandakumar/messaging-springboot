package com.example.messaging.controller;

import com.example.messaging.dto.MessageRequestDTO;
import com.example.messaging.dto.MessagingDTO;
import com.example.messaging.service.ManagedQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queue")
public class MessagingController {
    @Autowired
    private ManagedQueueService queueService;


    @GetMapping("/topics")
    public MessagingDTO getTopic() {
        return queueService.getTopicList();
    }

    @PostMapping("/addData")
    public MessagingDTO addDataToQueue(@RequestBody MessageRequestDTO messageRequestDTO) {
        return queueService.addData(messageRequestDTO);
    }
    @PostMapping("/subscribeToTopic")
    public MessagingDTO subscribeToQueue(@RequestBody MessageRequestDTO messageRequestDTO) {
        return queueService.subscribeToQueue(messageRequestDTO);
    }
    @PostMapping("/fetchDataForTopic")
    public MessagingDTO dataForTopic(@RequestBody MessageRequestDTO messageRequestDTO) {
        return queueService.fetchData(messageRequestDTO);
    }
}
