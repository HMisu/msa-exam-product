package com.sparta.msa_exam.product.config;

import com.sparta.msa_exam.product.dto.ProductSearchDto;
import io.micrometer.common.util.StringUtils;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory
    ) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofSeconds(60))
                .computePrefixWith(CacheKeyPrefix.simple())
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.java())
                );

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(configuration)
                .build();
    }

    @Bean
    public KeyGenerator customCacheKeyGenerator() {
        return getKeyGenerator();
    }

    static KeyGenerator getKeyGenerator() {
        return (target, method, params) -> {
            ProductSearchDto searchDto = params.length > 0 && params[0] instanceof ProductSearchDto ? (ProductSearchDto) params[0] : new ProductSearchDto();
            Pageable pageable = params.length > 1 && params[1] instanceof Pageable ? (Pageable) params[1] : Pageable.unpaged();

            StringBuilder sb = new StringBuilder();

            if (StringUtils.isNotEmpty(searchDto.getName())) {
                sb.append("name_").append(searchDto.getName());
            }
            if (StringUtils.isNotEmpty(searchDto.getDescription())) {
                sb.append("desc_").append(searchDto.getDescription());
            }
            if (searchDto.getMinPrice() != null) {
                sb.append("minPrice_").append(searchDto.getMinPrice());
            }
            if (searchDto.getMaxPrice() != null) {
                sb.append("maxPrice_").append(searchDto.getMaxPrice());
            }
            if (searchDto.getMinQuantity() != null) {
                sb.append("minQuantity_").append(searchDto.getMinQuantity());
            }
            if (searchDto.getMaxQuantity() != null) {
                sb.append("maxQuantity_").append(searchDto.getMaxQuantity());
            }

            sb.append("pageNum").append(pageable.getPageNumber());
            sb.append("pageSize").append(pageable.getPageSize());

            return sb.toString();
        };
    }
}
