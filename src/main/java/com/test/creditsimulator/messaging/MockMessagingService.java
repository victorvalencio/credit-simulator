package com.test.creditsimulator.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockMessagingService implements MessagingService {

    private static final Logger logger = LoggerFactory.getLogger(MockMessagingService.class);

    @Override
    public void sendMessage(String topic, String message) {
        logger.info("Simulated message sent to topic '{}': {}", topic, message);
    }
}