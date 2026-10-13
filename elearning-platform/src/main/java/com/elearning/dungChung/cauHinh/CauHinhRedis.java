package com.elearning.dungChung.cauHinh;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cấu hình RedisTemplate<String, Object> dùng cho:
 * - Distributed Lock chống ghi danh trùng (Double-Lock Validation, module LopHocLMS)
 * - Distributed Lock chống spam đánh giá (module ChoKhoaHoc)
 * - Cache tạm thời nếu cần (không bắt buộc cho luận văn)
 *
 * Đặt ở dungChung vì RedisTemplate là tài nguyên kỹ thuật chung,
 * không thuộc domain nghiệp vụ nào cụ thể.
 */
@Configuration
public class CauHinhRedis {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        template.afterPropertiesSet();
        return template;
    }
}
