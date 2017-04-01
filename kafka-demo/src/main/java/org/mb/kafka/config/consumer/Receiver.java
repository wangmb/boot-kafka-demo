package org.mb.kafka.config.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

public class Receiver{

  private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

  @KafkaListener(topics = "${kafka.consumer.topic}",group="test-group")
  public void receive(String message,@Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
    LOGGER.debug("received partition={} message='{}'",partition ,message);
  }
  
  @KafkaListener(topics = "${kafka.consumer.topic}",group="test-group")
  public void receive2(String message,
		  @Header(name=KafkaHeaders.RECEIVED_MESSAGE_KEY,required=false) String key,
		  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
    LOGGER.debug("received2 partition={} received_key={} message='{}'",partition,key, message);
  }

}
