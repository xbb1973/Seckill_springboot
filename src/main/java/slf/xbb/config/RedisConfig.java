package slf.xbb.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/23 12:26 下午
 * @description：redisConfig
 * @modifiedBy：
 * @version:
 */
@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class RedisConfig {

}
