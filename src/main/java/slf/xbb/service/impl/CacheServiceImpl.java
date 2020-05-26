package slf.xbb.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;
import slf.xbb.service.CacheService;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/26 5:51 下午
 * @description：实现缓存
 * @modifiedBy：
 * @version:
 */
@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String, Object> commonCache = null;

    @PostConstruct
    public void init() {
        commonCache = CacheBuilder.newBuilder()
                // 设置缓存容器的初始量
                .initialCapacity(10)
                // 设置最大KEY存储数值，超过按LRU策略移除
                .maximumSize(100)
                // 设置过期时间，按写时间开始计算，而不是按访问时间开始计算
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * @Description:
     * @Param: [key, value]
     * @return: void
     * @Date: 2020/5/26
     * @Author: xbb1973
     */
    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }

    /**
     * @Description:
     * @Param: [key]
     * @return: java.lang.Object
     * @Date: 2020/5/26
     * @Author: xbb1973
     */
    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
