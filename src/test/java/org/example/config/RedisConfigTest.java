package org.example.config;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@SpringBootTest
class RedisConfigTest {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试 RedisTemplate 是否成功注入且不为 null
     * 期望结果: RedisTemplate 实例存在
     */
    @Test
    void testRedisTemplateNotNull() {
        assertNotNull(redisTemplate, "RedisTemplate 应该被正确配置并注入");
    }

    /**
     * 测试 Key 序列化器是否配置为 StringRedisSerializer
     * 期望结果: Key 序列化器是 StringRedisSerializer 的实例
     */
    @Test
    void testKeySerializer() {
        RedisSerializer<?> keySerializer = redisTemplate.getKeySerializer();
        assertNotNull(keySerializer, "Key 序列化器不应为 null");
        assertTrue(keySerializer instanceof org.springframework.data.redis.serializer.StringRedisSerializer, 
                "Key 序列化器应该是 StringRedisSerializer");
    }

    /**
     * 测试 Value 序列化器是否配置为 GenericJackson2JsonRedisSerializer (或预期的 JSON 序列化器)
     * 期望结果: Value 序列化器是 GenericJackson2JsonRedisSerializer 的实例，支持 JSON 序列化
     */
    @Test
    void testValueSerializer() {
        RedisSerializer<?> valueSerializer = redisTemplate.getValueSerializer();
        assertNotNull(valueSerializer, "Value 序列化器不应为 null");
        assertTrue(valueSerializer instanceof org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer, 
                "Value 序列化器应该是 GenericJackson2JsonRedisSerializer 以支持 JSON 序列化");
    }

    /**
     * 测试 HashKey 序列化器是否配置正确
     * 期望结果: HashKey 序列化器是 StringRedisSerializer 的实例
     */
    @Test
    void testHashKeySerializer() {
        RedisSerializer<?> hashKeySerializer = redisTemplate.getHashKeySerializer();
        assertNotNull(hashKeySerializer, "HashKey 序列化器不应为 null");
        assertTrue(hashKeySerializer instanceof org.springframework.data.redis.serializer.StringRedisSerializer, 
                "HashKey 序列化器应该是 StringRedisSerializer");
    }

    /**
     * 测试 HashValue 序列化器是否配置正确
     * 期望结果: HashValue 序列化器是 GenericJackson2JsonRedisSerializer 的实例
     */
    @Test
    void testHashValueSerializer() {
        RedisSerializer<?> hashValueSerializer = redisTemplate.getHashValueSerializer();
        assertNotNull(hashValueSerializer, "HashValue 序列化器不应为 null");
        assertTrue(hashValueSerializer instanceof org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer, 
                "HashValue 序列化器应该是 GenericJackson2JsonRedisSerializer");
    }

    /**
     * 测试 Redis 数据的写入和读取，验证序列化配置是否正确
     * 期望结果: 能够成功写入对象，并正确读取且反序列化为原始对象
     */
    @Test
    void testRedisSetAndGet() {
        // 准备测试数据
        String key = "test:user:1";
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setEmail("zhangsan@example.com");

        // 写入数据到 Redis
        redisTemplate.opsForValue().set(key, user);

        // 从 Redis 读取数据
        Object storedValue = redisTemplate.opsForValue().get(key);

        // 验证读取到的数据不为 null
        assertNotNull(storedValue, "从 Redis 读取的数据不应为 null");

        // 验证数据类型是否正确 (应该是 User 类型，因为配置了 GenericJackson2JsonRedisSerializer)
        assertTrue(storedValue instanceof User, "读取到的数据应该是 User 类型");

        // 验证数据内容是否正确
        User retrievedUser = (User) storedValue;
        assertEquals(user.getId(), retrievedUser.getId(), "用户 ID 应该匹配");
        assertEquals(user.getName(), retrievedUser.getName(), "用户名应该匹配");
        assertEquals(user.getEmail(), retrievedUser.getEmail(), "用户邮箱应该匹配");

        // 清理测试数据
        redisTemplate.delete(key);
    }

    // 内部静态类用于测试序列化
    static class User implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String name;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
