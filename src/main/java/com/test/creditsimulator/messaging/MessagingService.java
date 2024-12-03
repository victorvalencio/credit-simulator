package com.test.creditsimulator.messaging;

public interface MessagingService {
    void sendMessage(String topic, String message);
}