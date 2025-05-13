package com.ares.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaTransactionalProducerExample {

  private static final Logger log = LoggerFactory.getLogger(KafkaTransactionalProducerExample.class);
  private static final String msg = "{\"userId\":27999,\"supplierId\":100001,\"activityId\":1001,\"storeId\":100001,\"skuId\":100001,\"orderId\":26914794388848640,\"payAmount\":177776,\"payLink\":null,\"address\":\"[1001, 2001, 3001]\"}";

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 5);

    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "t-transactional-id");

    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    Producer<String, String> producer = new KafkaProducer<>(props);
    producer.initTransactions();
    try {
      producer.beginTransaction();
      for (int i = 0; i < 10; i++) {
        producer.send(new ProducerRecord<>("T-transaction-example", msg));
      }
      producer.commitTransaction();
    } catch (Exception e) {
      log.error(e.getMessage());
      producer.abortTransaction();
    } finally {
      producer.close();
    }
  }
}
