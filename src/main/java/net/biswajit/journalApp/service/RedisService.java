package net.biswajit.journalApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T get(String key, Class<T> entityClass){
        try{
            String value = (String) redisTemplate.opsForValue().get(key); // returns Object not String so Use Casting
            return objectMapper.readValue(value, entityClass);
        }catch(Exception e){
            log.error("Exception ",e);
            return null;
        }
    }

    public void set(String key, Object o, long ttl){
        try{
            String jsonFormat = objectMapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key, jsonFormat, ttl, TimeUnit.SECONDS);
        }catch(Exception e){
            log.error("Exception ", e);
        }
    }
}
