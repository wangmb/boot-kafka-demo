package org.mb.kafka.config.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class Sender {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public void send(String topic,final String message) {
    // KafkaTemplate通过异步的方式发送数据到kafka，并返回ListenableFuture
    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

    //注册一个callback，用于监听消息发送结果 
    future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

      @Override
      public void onSuccess(SendResult<String, String> result) {
        LOGGER.debug("sent message='{}' with \n offset={}、partition={}", message,
        		result.getRecordMetadata().offset(),
        		result.getRecordMetadata().partition());
      }

      @Override
      public void onFailure(Throwable ex) {
        LOGGER.error("unable to send message='{}'", message, ex);
      }
    });

    // alternatively, to block the sending thread, to await the result,
    // invoke the future's get() method
  }
}
