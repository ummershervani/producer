package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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


        @GetMapping(value = "/producer/{message}")
        public String sendMessage(@PathVariable("message") String message) {
            System.out.println("Hello message is received " + message);
            messageBinder.sendMessage(message);
            return "Message sent successfully";
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
        public MessageBinder messageBinder(MessageProducerChannel channel){
            return new MessageBinder(channel);
        }

        public void sendMessage(String message){
            Message<String> msg = MessageBuilder.withPayload(message).build();
            channel.output().send(msg);
        }
    }

    interface MessageProducerChannel {

        @Output
        MessageChannel output();
    }
}