package com.c88.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static com.c88.common.core.constant.TopicConstants.BALANCE_CHANGE;
import static com.c88.common.core.constant.TopicConstants.MEMBER_FIRST_RECHARGE;
import static com.c88.common.core.constant.TopicConstants.VALID_BET;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // @Value("${spring.kafka.bootstrap-servers}")
    // private String bootstrapAddress;
    //
    // public ConsumerFactory<String, AddBalanceDTO> consumerFactory(String groupId) {
    //     Map<String, Object> props = new HashMap<>();
    //     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    //     props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    //     props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //     props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000);
    //     props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     return new DefaultKafkaConsumerFactory<>(props,
    //             new StringDeserializer(),
    //             new JsonDeserializer<>(AddBalanceDTO.class));
    // }
    //
    // public ConcurrentKafkaListenerContainerFactory<String, AddBalanceDTO> kafkaListenerContainerFactory(String groupId) {
    //     ConcurrentKafkaListenerContainerFactory<String, AddBalanceDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
    //     factory.setConsumerFactory(consumerFactory(groupId));
    //     factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    //     return factory;
    // }
    //
    // @Bean
    // public ConcurrentKafkaListenerContainerFactory<String, AddBalanceDTO> balanceChangeKafkaListenerContainerFactory() {
    //     return kafkaListenerContainerFactory(BALANCE_CHANGE);
    // }
    //
    //
    // public ConsumerFactory<String, BetRecord> betRecordConsumerFactory(String groupId) {
    //     Map<String, Object> props = new HashMap<>();
    //     props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    //     props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    //     props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //     props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000);
    //     props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
    //     props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    //     return new DefaultKafkaConsumerFactory<>(props,
    //             new StringDeserializer(),
    //             new JsonDeserializer<>(BetRecord.class));
    // }
    //
    // public ConcurrentKafkaListenerContainerFactory<String, BetRecord> betRecordKafkaListenerContainerFactory(String groupId) {
    //     ConcurrentKafkaListenerContainerFactory<String, BetRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
    //     factory.setConsumerFactory(betRecordConsumerFactory(groupId));
    //     factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    //     return factory;
    // }
    //
    // @Bean
    // public ConcurrentKafkaListenerContainerFactory<String, BetRecord> validBetKafkaListenerContainerFactory() {
    //     return betRecordKafkaListenerContainerFactory(VALID_BET);
    // }

    @Bean
    public NewTopic newTopic1() {
        return new NewTopic(MEMBER_FIRST_RECHARGE, 3, (short) 3);
    }

    @Bean
    public NewTopic newTopic2() {
        return new NewTopic(VALID_BET, 3, (short) 3);
    }

    @Bean
    public NewTopic newTopic3() {
        return new NewTopic(BALANCE_CHANGE, 3, (short) 3);
    }


}
