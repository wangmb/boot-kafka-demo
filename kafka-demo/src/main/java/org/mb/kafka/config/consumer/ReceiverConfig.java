package org.mb.kafka.config.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@EnableKafka
public class ReceiverConfig {

  @Value("${kafka.bootstrap.servers}")
  private String bootstrapServers;

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<String, Object>();
    // Kakfa cluster信息配置
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    
    /**
     * 【At-most-once Kafka】
     * 1.Set 'ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG' to true.
     * 2.Set 'ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG' to a lower timeframe.
     * 3.
     * [At-least-once ]
     * 1.Set 'ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG' to true.
     * 2.Set 'ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG' to a higher number.
     * 3.Consumer should then take control of the message offset commits to Kafka by making the following call consumer.commitSync()
     * []
     * 
     * */
    
    //自动commit
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    //自动提交时间间隔
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"101");
    
    // consumer groups allow a pool of processes to divide the work of consuming and processing records
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");

    return props;
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<String, String>(consumerConfigs());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory());
    factory.setBatchListener(true); 

    return factory;
  }

  @Bean
  public Receiver receiver() {
    return new Receiver();
  }
}
