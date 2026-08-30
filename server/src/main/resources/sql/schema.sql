-- 积分商城数据库 Schema
-- MySQL 8.x
CREATE DATABASE IF NOT EXISTS score_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE score_mall;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `points` INT NOT NULL DEFAULT 0 COMMENT '当前积分',
  `total_points` INT NOT NULL DEFAULT 0 COMMENT '累计积分(用于等级计算)',
  `level` TINYINT NOT NULL DEFAULT 0 COMMENT '会员等级 0普通 1银卡 2金卡 3钻石',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员表
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` VARCHAR(50) DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色 ADMIN/SUPER_ADMIN',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 商品分类
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `icon` VARCHAR(255) DEFAULT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `subtitle` VARCHAR(255) DEFAULT NULL,
  `cover_image` VARCHAR(500) NOT NULL,
  `images` TEXT COMMENT '详情图JSON数组',
  `detail` TEXT COMMENT '富文本详情',
  `original_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '原价',
  `points_price` INT NOT NULL DEFAULT 0 COMMENT '纯积分价',
  `cash_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '积分+现金模式的现金部分',
  `stock` INT NOT NULL DEFAULT 0,
  `sales` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
  `is_hot` TINYINT NOT NULL DEFAULT 0,
  `is_recommend` TINYINT NOT NULL DEFAULT 0,
  `sort` INT NOT NULL DEFAULT 0,
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 收货地址
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `province` VARCHAR(50) NOT NULL,
  `city` VARCHAR(50) NOT NULL,
  `district` VARCHAR(50) NOT NULL,
  `detail` VARCHAR(255) NOT NULL,
  `is_default` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- 订单表
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(100) NOT NULL,
  `product_image` VARCHAR(500),
  `quantity` INT NOT NULL DEFAULT 1,
  `points_used` INT NOT NULL DEFAULT 0 COMMENT '使用的积分',
  `cash_paid` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '支付的现金',
  `address_snapshot` VARCHAR(500) COMMENT '收货地址快照',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待发货 1已发货 2已完成 3已取消',
  `ship_no` VARCHAR(50) COMMENT '物流单号',
  `remark` VARCHAR(255),
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ship_time` DATETIME DEFAULT NULL,
  `complete_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 积分流水
DROP TABLE IF EXISTS `points_log`;
CREATE TABLE `points_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `change_points` INT NOT NULL COMMENT '变动积分(+/-)',
  `before_points` INT NOT NULL,
  `after_points` INT NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT 'SIGN/ORDER_EARN/EXCHANGE/REFUND/ADMIN',
  `related_id` BIGINT DEFAULT NULL,
  `remark` VARCHAR(255),
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

-- 签到记录
DROP TABLE IF EXISTS `sign_in`;
CREATE TABLE `sign_in` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `sign_date` DATE NOT NULL,
  `points` INT NOT NULL DEFAULT 5,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录';

-- 轮播图
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100),
  `image` VARCHAR(500) NOT NULL,
  `link` VARCHAR(500),
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图';

-- ============ 初始数据 ============
-- 管理员 (密码: admin123, BCrypt)
INSERT INTO `admin`(`username`,`password`,`nickname`,`role`) VALUES
('admin','$2b$10$S6sli84jfkuuG4vjyOAokuG3Sdp.FK2Wc5SV0obTcXB8eT/fpD9jW','超级管理员','SUPER_ADMIN');

-- 分类
INSERT INTO `category`(`name`,`icon`,`sort`,`status`) VALUES
('数码电器','digital',1,1),
('家居生活','home',2,1),
('美妆个护','beauty',3,1),
('食品饮料','food',4,1),
('服饰箱包','bag',5,1),
('虚拟商品','virtual',6,1);

-- 商品 (示例)
INSERT INTO `product`(`category_id`,`name`,`subtitle`,`cover_image`,`images`,`detail`,`original_price`,`points_price`,`cash_price`,`stock`,`sales`,`status`,`is_hot`,`is_recommend`,`sort`) VALUES
(1,'无线蓝牙耳机','降噪HiFi音质','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=wireless%20bluetooth%20earbuds%20product%20photo%20white%20background&image_size=square','[]','高品质无线蓝牙耳机，主动降噪，超长续航。',299.00,29900,0.00,100,12,1,1,1,1),
(1,'智能手表','运动健康监测','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20watch%20product%20photo%20white%20background&image_size=square','[]','多功能智能手表，心率监测，运动记录。',499.00,49900,0.00,50,8,1,1,1,2),
(2,'保温杯','316不锈钢真空','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=stainless%20steel%20thermos%20bottle%20product%20photo&image_size=square','[]','316不锈钢真空保温杯，长效保温。',89.00,8900,0.00,200,30,1,0,1,3),
(2,'四件套','纯棉床品','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cotton%20bedding%20set%20product%20photo&image_size=square','[]','100%纯棉四件套，亲肤透气。',399.00,30000,99.00,80,5,1,0,0,4),
(3,'面膜套装','补水保湿10片装','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=face%20mask%20skincare%20product%20photo&image_size=square','[]','玻尿酸补水面膜，深层保湿。',129.00,12900,0.00,150,20,1,1,1,5),
(4,'坚果礼盒','每日坚果30包','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=nuts%20gift%20box%20product%20photo&image_size=square','[]','混合坚果礼盒，健康零食。',158.00,15800,0.00,120,15,1,0,0,6),
(5,'帆布包','潮流单肩包','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=canvas%20tote%20bag%20product%20photo&image_size=square','[]','潮流帆布单肩包，大容量。',79.00,7900,0.00,300,45,1,0,1,7),
(6,'视频会员月卡','主流视频平台','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20streaming%20membership%20card%20product%20photo&image_size=square','[]','主流视频平台月卡会员。',25.00,2500,0.00,999,120,1,1,1,8);

-- 轮播图
INSERT INTO `banner`(`title`,`image`,`link`,`sort`,`status`) VALUES
('积分商城焕新上线','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=shopping%20mall%20banner%20colorful%20sale%20promotion&image_size=landscape_16_9','/category',1,1),
('签到赚积分','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=daily%20check%20in%20rewards%20banner%20illustration&image_size=landscape_16_9','/sign',2,1),
('数码专场','https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=digital%20electronics%20products%20banner&image_size=landscape_16_9','/category/1',3,1);
