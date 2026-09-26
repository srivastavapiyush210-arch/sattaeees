package com.sattaees.sattaees.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Production Redis cache configuration with Cache-Aside support,
 * custom JSON serialization, TTL settings, and a graceful error handler
 * for resilience if Redis is temporarily unreachable.
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    public static final String CACHE_WORKER_PROFILE = "worker_profile";
    public static final String CACHE_WORKER_CATALOG = "worker_catalog";
    public static final String CACHE_WORKER_SEARCH = "worker_search";

    @Value("${app.cache.redis.time-to-live-millis:900000}")
    private long defaultTtlMillis;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(defaultTtlMillis))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration(CACHE_WORKER_PROFILE, cacheConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration(CACHE_WORKER_CATALOG, cacheConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_WORKER_SEARCH, cacheConfig.entryTtl(Duration.ofMinutes(5)))
                .build();
    }

    /**
     * Fault-tolerant cache error handler:
     * If Redis is unreachable, log warning and let application degrade gracefully to DB.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis unavailable during GET for cache '{}' and key '{}'. Degrading to database. Error: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis unavailable during PUT for cache '{}' and key '{}'. Error: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis unavailable during EVICT for cache '{}' and key '{}'. Error: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis unavailable during CLEAR for cache '{}'. Error: {}",
                        cache.getName(), exception.getMessage());
            }
        };
    }
}
