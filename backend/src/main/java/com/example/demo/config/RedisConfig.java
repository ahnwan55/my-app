package com.example.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // 도서관 정보 - 24시간
        RedisCacheConfiguration libraryConfig = defaultConfig()
                .entryTtl(Duration.ofHours(24));

        // 인기도서 Top10 - 1시간
        RedisCacheConfiguration popularBooksConfig = defaultConfig()
                .entryTtl(Duration.ofHours(1));

        // 페르소나 타입 - 7일
        RedisCacheConfiguration personaConfig = defaultConfig()
                .entryTtl(Duration.ofDays(7));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("library", libraryConfig)
                .withCacheConfiguration("popularBooks", popularBooksConfig)
                .withCacheConfiguration("persona", personaConfig)
                .build();
    }

    private RedisCacheConfiguration defaultConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
    }
}