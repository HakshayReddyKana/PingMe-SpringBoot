package com.hakshay.chat.config;

import com.hakshay.chat.service.RedisSubscriberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Use JSON for values instead of raw Java serialization
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("chat-topic");
    }

    @Bean
    public ChannelTopic receiptTopic() {
        return new ChannelTopic("receipt-topic");
    }

    @Bean
    public MessageListenerAdapter messageListener(RedisSubscriberService subscriberService) {
        // Tells Redis to trigger "handleMessage" in our Subscriber Service!
        return new MessageListenerAdapter(subscriberService, "handleMessage");
    }

    @Bean
    public MessageListenerAdapter receiptListener(RedisSubscriberService subscriberService) {
        return new MessageListenerAdapter(subscriberService, "handleReceipt");
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter messageListener,
                                                        ChannelTopic topic,
                                                        MessageListenerAdapter receiptListener, // NEW
                                                        ChannelTopic receiptTopic) {            // NEW
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(messageListener, topic);
        container.addMessageListener(receiptListener, receiptTopic); // Listen to receipts too!

        return container;
    }

}
