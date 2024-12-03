package com.sparta.msa_exam.product.config;

import com.sparta.msa_exam.product.dto.ProductResponseDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, ProductResponseDto> productTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ProductResponseDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        template.setValueSerializer(serializer);
        template.setKeySerializer(RedisSerializer.string());
        return template;
    }

}
