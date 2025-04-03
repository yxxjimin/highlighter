package org.ybigta.highlighter.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {

    @Bean
    public NewTopic videoTopic() {
        return TopicBuilder.name("livestream")
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name("chats")
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }
}
