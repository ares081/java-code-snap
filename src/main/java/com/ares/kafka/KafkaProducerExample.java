package com.ares.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerExample {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerExample.class);
  private static final String msg = "{\"userId\":27999,\"supplierId\":100001,\"activityId\":1001,\"storeId\":100001,\"skuId\":100001,\"orderId\":26914794388848640,\"payAmount\":177776,\"payLink\":null,\"address\":\"[1001, 2001, 3001]\"}";

  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    try (Producer<String, String> producer = new KafkaProducer<>(props)) {
      for (int i = 0; i < 100; i++) {
        ProducerRecord<String, String> record = new ProducerRecord<>("T-kafka-example", msg);
        producer.send(record);
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata recordMetadata = future.get();

        log.info("record metadata: {}", mapper.writeValueAsString(recordMetadata));

        producer.send(record, (metadata, exception) -> {
          if (exception != null) {
            log.error("exception", exception);
          } else {
            try {
              log.info("record metadata: {}", mapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
      producer.flush();
    } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
