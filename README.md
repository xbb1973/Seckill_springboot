# Seckill_springboot
## 目前实现有：
### 1、springboot+mybatis+mybatisPlus 基础功能实现：用户注册、登陆、商品增加、下单、促销发布、秒杀
### 2、nginx(openresty)+redis 分布式水平扩展、分布式会话管理(token or session)
### 3、redis+jvm cache(guava)+nginx_proxy_cache+nginx_lua 多级缓存优化查询 + CDN加速 静态页面化
### 4、redis+rocketMQ 缓存模型优化校验查询、行级锁保障数据库正确性、异步消息同步数据库和缓存库存优化行级锁串行竞争/等待、库存售罄最终一致性保证

## 需要继续完善的地方：
### 1、秒杀实现方式-秒杀接口
### 2、系统时间获取，从服务端获取时间
### 3、多商品、多库存、多活动模型的实现
