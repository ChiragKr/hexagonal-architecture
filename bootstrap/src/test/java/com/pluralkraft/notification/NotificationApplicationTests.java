package com.pluralkraft.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    topics = {"notifications"},
    brokerProperties = {
        "listeners=EXTERNAL://localhost:0,CONTROLLER://localhost:0",
        "listener.security.protocol.map=EXTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT",
        "inter.broker.listener.name=EXTERNAL",
        "controller.listener.names=CONTROLLER"
    }
)
@TestPropertySource(
    locations = "classpath:application.yaml",
    properties = {
        "spring.kafka.topic.name=notifications",
        "spring.kafka.consumer.group-id=notification-group-it",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
    }
)
abstract class NotificationApplicationTests {

	@Test
	void contextLoads() {
	}

}
