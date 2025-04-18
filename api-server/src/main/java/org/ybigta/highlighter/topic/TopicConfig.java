package org.ybigta.highlighter.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {

    public static final String VIDEO_TOPIC = "livestream";
    public static final String CHAT_TOPIC = "chats";

    @Bean
    public NewTopic videoTopic() {
        return TopicBuilder.name(VIDEO_TOPIC)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name(CHAT_TOPIC)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }
}
