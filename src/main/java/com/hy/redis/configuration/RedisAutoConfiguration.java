package com.hy.redis.configuration;

import com.hy.redis.properties.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * redis 的自动配置
 *
 * @author wyx
 */
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
@ConditionalOnClass({RedisTemplate.class, JedisConnectionFactory.class})
public class RedisAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(this.redisProperties.getMaxIdle());
        jedisPoolConfig.setMaxTotal(this.redisProperties.getMaxTotal());
        jedisPoolConfig.setMaxWaitMillis((long) this.redisProperties.getMaxWaitMillis());
        jedisPoolConfig.setTestOnReturn(this.redisProperties.isTestOnReturn());
        jedisPoolConfig.setTestOnBorrow(this.redisProperties.isTestOnBorrow());
        return jedisPoolConfig;
    }

    public RedisClusterConfiguration redisClusterConfiguration() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        Set<RedisNode> redisNodes = new HashSet();
        String[] hosts = this.redisProperties.getHostName().split(",");
        String[] var7 = hosts;
        int var6 = hosts.length;

        for (int var5 = 0; var5 < var6; ++var5) {
            String hostName = var7[var5];
            String[] host = hostName.split(":");
            redisNodes.add(new RedisClusterNode(host[0], Integer.valueOf(host[1]).intValue()));
        }

        redisClusterConfiguration.setClusterNodes(redisNodes);
        redisClusterConfiguration.setMaxRedirects(this.redisProperties.getMaxRedirects());
        return redisClusterConfiguration;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(this.redisClusterConfiguration(), this.jedisPoolConfig());
        jedisConnectionFactory.setTimeout(this.redisProperties.getTimeOut());
        if (!StringUtils.isEmpty(this.redisProperties.getPassword())) {
            jedisConnectionFactory.setPassword(this.redisProperties.getPassword());
        }

        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(this.jedisConnectionFactory());
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        return redisTemplate;
    }


}
