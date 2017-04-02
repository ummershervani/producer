package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.*;
import org.springframework.messaging.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;


@SpringBootApplication
public class ProducerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @RestController
    @RequestMapping(value = "/")
    public static class MyController {

        private final MessageBinder messageBinder;

        @Autowired
        public MyController(MessageBinder messageBinder) {
            this.messageBinder = messageBinder;
        }


        @GetMapping(value = "/producer/{firstName}/{lastName}")
        public String sendMessage(@PathVariable("firstName") String firstName, @PathVariable("lastName") String lastName) throws JsonProcessingException {
            System.out.println("Sending message for : " + firstName + " " + lastName);
            Person person = new Person(firstName, lastName);
            messageBinder.sendMessage(person);
            return "Message sent successfully";
        }

    }

    static class Person implements Serializable {
        static final long serialVersionUID = 42L;

        private String firstName;
        private String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    @EnableBinding(MessageProducerChannel.class)
    @Configuration
    public static class MessageBinder {

        private final MessageProducerChannel channel;

        public MessageBinder(MessageProducerChannel channel) {
            this.channel = channel;
        }

        @Bean
        @Primary
        public MessageBinder messageBinder(MessageProducerChannel channel) {
            return new MessageBinder(channel);
        }

        public void sendMessage(Object message) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            Message<String> msg = MessageBuilder.withPayload(objectMapper.writeValueAsString(message))
                    .setHeader(MessageHeaders.CONTENT_TYPE, "application/json").build();
            channel.output().send(msg);
        }
    }

    interface MessageProducerChannel {

        @Output("foo")
        MessageChannel output();
    }
}