-- MySQL dump 10.13  Distrib 5.7.28, for macos10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: seckill
-- ------------------------------------------------------
-- Server version	5.7.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `price` decimal(64,2) NOT NULL DEFAULT '0.00',
  `description` varchar(500) COLLATE utf8_bin NOT NULL DEFAULT '',
  `sales` int(100) NOT NULL DEFAULT '0',
  `img_url` varchar(5000) COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` VALUES (1,'iphone99',800.00,'最好用的iphone',11,'https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1563645867825&di=65cbf1d86165f7185ce7772e2e8a4bca&imgtype=0&src=http%3A%2F%2Fp0.ifengimg.com%2Fpmop%2F2017%2F1127%2F753C746E59ACA849F681F4FC5B75ACD494092110_size15_w600_h400.jpeg'),(2,'iphone99',800.00,'最好用的iphone',1,'https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1563645867825&di=65cbf1d86165f7185ce7772e2e8a4bca&imgtype=0&src=http%3A%2F%2Fp0.ifengimg.com%2Fpmop%2F2017%2F1127%2F753C746E59ACA849F681F4FC5B75ACD494092110_size15_w600_h400.jpeg'),(3,'iphone8',200.00,'第二好用的iphone',3,'https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1563647556769&di=cc5c9241446eee425165e9b04a87768c&imgtype=0&src=http%3A%2F%2Fi9.hexun.com%2F2018-03-17%2F192644421.jpg'),(4,'iphone8',200.00,'第二好用的iphone',44021,'https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1563647556769&di=cc5c9241446eee425165e9b04a87768c&imgtype=0&src=http%3A%2F%2Fi9.hexun.com%2F2018-03-17%2F192644421.jpg');
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item_stock`
--

DROP TABLE IF EXISTS `item_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `item_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stock` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item_stock`
--

LOCK TABLES `item_stock` WRITE;
/*!40000 ALTER TABLE `item_stock` DISABLE KEYS */;
INSERT INTO `item_stock` VALUES (9,99999999,1),(10,99999999,2),(11,99999999,3),(12,99999999,4);
/*!40000 ALTER TABLE `item_stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_info`
--

DROP TABLE IF EXISTS `order_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_info` (
  `id` varchar(32) COLLATE utf8_bin NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `amount` int(11) NOT NULL DEFAULT '0',
  `order_price` decimal(40,2) NOT NULL DEFAULT '0.00',
  `promo_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_info`
--

LOCK TABLES `order_info` WRITE;
/*!40000 ALTER TABLE `order_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promo`
--

DROP TABLE IF EXISTS `promo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `promo` (
  `id` int(100) NOT NULL AUTO_INCREMENT,
  `promo_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '',
  `start_date` datetime NOT NULL DEFAULT '2020-04-01 00:00:00',
  `end_date` datetime NOT NULL DEFAULT '2020-04-01 00:00:00',
  `item_id` int(11) NOT NULL DEFAULT '0',
  `promo_item_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promo`
--

LOCK TABLES `promo` WRITE;
/*!40000 ALTER TABLE `promo` DISABLE KEYS */;
INSERT INTO `promo` VALUES (1,'iphone大减价','2020-04-05 20:25:10','2020-06-06 19:55:52',1,23.00),(2,'iphone8大减价','2020-04-07 20:17:17','2020-06-07 20:18:18',3,3.00);
/*!40000 ALTER TABLE `promo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seckill`
--

DROP TABLE IF EXISTS `seckill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `seckill` (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) NOT NULL COMMENT '商品名称',
  `number` int(11) NOT NULL COMMENT '库存数量',
  `start_time` timestamp NOT NULL DEFAULT '2018-05-04 16:00:00' COMMENT '开始时间',
  `end_time` timestamp NOT NULL DEFAULT '2018-05-04 16:00:00' COMMENT '结束时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1008 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seckill`
--

LOCK TABLES `seckill` WRITE;
/*!40000 ALTER TABLE `seckill` DISABLE KEYS */;
INSERT INTO `seckill` VALUES (1000,'1000秒杀iPhone6S',86,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 09:29:02'),(1001,'500秒杀MBP',199,'2020-04-06 02:04:22','2020-06-01 16:00:00','2020-03-13 09:29:02'),(1002,'300秒杀iPad',100,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 09:29:02'),(1003,'200秒杀小米MIX',300,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 09:29:02'),(1004,'1000秒杀iPhone6S',99,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 14:11:35'),(1005,'500秒杀MBP',199,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 14:11:35'),(1006,'300秒杀iPad',98,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 14:11:35'),(1007,'200秒杀小米MIX',293,'2020-04-30 16:00:00','2020-06-01 16:00:00','2020-03-13 14:11:35');
/*!40000 ALTER TABLE `seckill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence_info`
--

DROP TABLE IF EXISTS `sequence_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence_info` (
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  `current_value` int(11) NOT NULL DEFAULT '0',
  `step` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence_info`
--

LOCK TABLES `sequence_info` WRITE;
/*!40000 ALTER TABLE `sequence_info` DISABLE KEYS */;
INSERT INTO `sequence_info` VALUES ('order_info',44040,1);
/*!40000 ALTER TABLE `sequence_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `success_killed`
--

DROP TABLE IF EXISTS `success_killed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `success_killed` (
  `seckill_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `user_phone` bigint(20) NOT NULL COMMENT '用户手机号',
  `state` tinyint(4) NOT NULL DEFAULT '-1' COMMENT '状态标志，-1：无效 0：成功 1：已付款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间时间',
  PRIMARY KEY (`seckill_id`,`user_phone`),
  KEY `idx_create_tim` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1008 DEFAULT CHARSET=utf8 COMMENT='秒杀明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `success_killed`
--

LOCK TABLES `success_killed` WRITE;
/*!40000 ALTER TABLE `success_killed` DISABLE KEYS */;
INSERT INTO `success_killed` VALUES (1000,13066342239,0,'2020-03-17 02:53:05'),(1000,13476191876,0,'2020-03-13 21:26:11'),(1000,13476191877,0,'2020-03-13 15:02:18'),(1000,15931313894,0,'2020-03-14 05:18:59'),(1000,17671714717,0,'2020-03-13 21:46:36'),(1000,17699581406,0,'2020-03-14 15:02:22'),(1000,18232341418,0,'2020-03-14 14:48:33'),(1000,18424515452,0,'2020-03-13 21:46:50'),(1000,18831573271,0,'2020-03-14 15:48:56'),(1001,13222500061,-1,'2020-04-06 02:04:24'),(1004,13225000601,0,'2020-03-17 01:22:02'),(1005,13225000600,0,'2020-03-16 03:01:31'),(1006,13225000600,0,'2020-03-16 02:58:44'),(1006,13225000601,-1,'2020-03-17 04:40:46'),(1007,13158983099,-1,'2020-03-17 04:37:45'),(1007,13225000601,0,'2020-03-17 01:22:19'),(1007,13225000602,-1,'2020-03-17 03:50:47'),(1007,13225000603,-1,'2020-03-17 03:51:20'),(1007,13589081051,-1,'2020-03-17 04:39:59'),(1007,15544746864,0,'2020-03-17 03:02:11'),(1007,18674173075,-1,'2020-03-17 04:37:22');
/*!40000 ALTER TABLE `success_killed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `gender` tinyint(2) NOT NULL DEFAULT '0' COMMENT '1代表男性\r\n',
  `age` int(11) NOT NULL DEFAULT '0',
  `telphone` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '',
  `regisit_mode` varchar(64) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '//byphone,bywechat,byalipay,',
  `third_party_id` int(64) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `telphone_unique_index` (`telphone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_info`
--

LOCK TABLES `user_info` WRITE;
/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` VALUES (40,'齐白石',1,1,'123','byphone',0),(52,'王伟',1,1,'111111','byphone',0),(53,'王昌龄',1,11111111,'11','byphone',0),(55,'岳飞',1,1,'111','byphone',0),(72,'小斑斑1',1,102,'13225000601','byPhone',0),(74,'小斑斑2',1,23,'13225000602','byPhone',0);
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_password`
--

DROP TABLE IF EXISTS `user_password`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_password` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `encrpt_password` varchar(128) COLLATE utf8_bin NOT NULL DEFAULT '',
  `user_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `use_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_password`
--

LOCK TABLES `user_password` WRITE;
/*!40000 ALTER TABLE `user_password` DISABLE KEYS */;
INSERT INTO `user_password` VALUES (18,'ICy5YqxZB1uWSwcVLSNLcA==',40),(22,'xMpCOKC5I4INzFCab3WEmw==',52),(23,'xMpCOKC5I4INzFCab3WEmw==',53),(24,'xMpCOKC5I4INzFCab3WEmw==',55),(33,'MQ==',72),(34,'MQ==',74);
/*!40000 ALTER TABLE `user_password` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-05-27 21:53:59
