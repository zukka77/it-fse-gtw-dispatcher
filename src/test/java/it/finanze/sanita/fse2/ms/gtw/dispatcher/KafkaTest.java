/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=test")
@ActiveProfiles(Constants.Profile.TEST)
class KafkaTest {

    @Autowired
    private IKafkaSRV kafkaSRV;

    @Test
    @DisplayName("Producer send")
    void kafkaProducerSend() {
        String key = "1";
        String topic = "transactionEvents";

        /***************** TOPIC **********************/
        for (int i = 0; i < 10; i++) {
            String message = "Messaggio numero : " + 1;
            RecordMetadata output = kafkaSRV.sendMessage(topic, key, message, true);
            assertEquals(message.length(), output.serializedValueSize(), "Il value non coincide");
            assertEquals(topic, output.topic(), "Il topic non coincide");
        }

    }

    @Data
    @NoArgsConstructor
    class KafkaMessageDTO {
        String message;
    }
}
