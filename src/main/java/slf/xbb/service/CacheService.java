package slf.xbb.service;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/26 5:49 下午
 * @description：封装本地缓存操作类
 * @modifiedBy：
 * @version:
 */
public interface CacheService {
    void setCommonCache(String key, Object value);
    Object getFromCommonCache(String key);
}
