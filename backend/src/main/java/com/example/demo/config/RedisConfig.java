package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

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

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis Cache GET Error - Cache: {}, Key: {}, Message: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis Cache PUT Error - Cache: {}, Key: {}, Message: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis Cache EVICT Error - Cache: {}, Key: {}, Message: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis Cache CLEAR Error - Cache: {}, Message: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}