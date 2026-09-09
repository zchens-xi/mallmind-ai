-- 商品分析模块测试数据
-- 创建商品浏览记录表和商品收藏表，并插入示例数据

-- 1. 创建商品浏览记录表
CREATE TABLE IF NOT EXISTS `pms_product_view_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
    `member_id` BIGINT(20) NULL DEFAULT NULL COMMENT '用户ID（登录用户）',
    `ip` VARCHAR(50) NULL DEFAULT NULL COMMENT 'IP地址（未登录用户标识）',
    `user_agent` VARCHAR(500) NULL DEFAULT NULL COMMENT '用户代理信息',
    `view_time` DATETIME NOT NULL COMMENT '浏览时间',
    `session_id` VARCHAR(100) NULL DEFAULT NULL COMMENT '会话ID',
    `referer` VARCHAR(500) NULL DEFAULT NULL COMMENT '来源页面',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_product_time` (`product_id`, `view_time`) USING BTREE,
    INDEX `idx_member_time` (`member_id`, `view_time`) USING BTREE,
    INDEX `idx_ip_time` (`ip`, `view_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品浏览记录表' ROW_FORMAT = DYNAMIC;

-- 2. 创建商品收藏表（如果不存在）
CREATE TABLE IF NOT EXISTS `pms_member_product_collection` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
    `member_nickname` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
    `product_name` VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品名称',
    `product_pic` VARCHAR(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品图片',
    `product_sub_title` VARCHAR(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品副标题',
    `product_price` DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '商品价格',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_member_product` (`member_id`, `product_id`) USING BTREE,
    INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品收藏表' ROW_FORMAT = DYNAMIC;

-- 3. 插入更多商品数据（补充不同价格区间和库存的商品）
INSERT IGNORE INTO `pms_product` (`id`, `brand_id`, `product_category_id`, `name`, `product_sn`, `delete_status`, `publish_status`, `new_status`, `recommand_status`, `verify_status`, `sort`, `sale`, `price`, `promotion_price`, `gift_growth`, `gift_point`, `sub_title`, `description`, `original_price`, `stock`, `low_stock`, `unit`, `weight`, `preview_status`, `service_ids`, `keywords`, `note`, `promotion_type`, `brand_name`, `product_category_name`) VALUES
-- 不同价格区间的商品
-- 0-100价格区间
(200, 1, 107, '笔记本套装', 'NOTE001', 0, 1, 0, 0, 1, 200, 85, 45.00, 39.00, 2, 2, '学生必备笔记本', '高质量纸张笔记本套装', 55.00, 500, 50, '套', 0.30, 0, '1,2', '笔记本', '学生用品', 0, '文具品牌', '图书文具'),
(201, 2, 101, '面膜单片', 'MASK002', 0, 1, 1, 0, 1, 201, 320, 15.00, 12.00, 1, 1, '补水单片面膜', '便携补水面膜', 20.00, 800, 80, '片', 0.05, 0, '1,2', '面膜', '护肤品', 0, '美妆品牌', '美妆护肤'),
(202, 3, 104, '运动毛巾', 'TOWEL001', 0, 1, 0, 1, 1, 202, 180, 35.00, 28.00, 1, 1, '运动专用毛巾', '吸汗透气运动毛巾', 45.00, 300, 30, '条', 0.20, 0, '1,2', '毛巾', '运动配件', 0, '运动品牌', '运动户外'),
(203, 4, 103, '收纳盒小号', 'BOX002', 0, 1, 0, 0, 1, 203, 95, 25.00, 20.00, 1, 1, '小型收纳盒', '桌面整理收纳盒', 35.00, 400, 40, '个', 0.15, 0, '1,2', '收纳', '家居收纳', 0, '家居品牌', '家居生活'),

-- 100-500价格区间
(204, 1, 100, '休闲衬衫', 'SHIRT001', 0, 1, 1, 1, 1, 204, 150, 189.00, 159.00, 8, 8, '商务休闲衬衫', '舒适透气商务衬衫', 239.00, 120, 12, '件', 0.35, 0, '1,2,3', '衬衫', '商务服装', 0, '时尚品牌', '服饰鞋包'),
(205, 2, 101, '护肤套装', 'SKINCARE001', 0, 1, 1, 1, 1, 205, 220, 288.00, 248.00, 15, 15, '基础护肤三件套', '洁面+爽肤+乳液套装', 358.00, 80, 8, '套', 0.80, 0, '1,2,3', '护肤', '套装', 0, '美妆品牌', '美妆护肤'),
(206, 3, 102, '蓝牙音箱', 'SPEAKER001', 0, 1, 1, 0, 1, 206, 280, 199.00, 169.00, 10, 10, '便携蓝牙音箱', '高保真音质音箱', 259.00, 150, 15, '个', 0.60, 0, '1,2,3', '音箱', '音响设备', 0, '数码品牌', '数码电器'),

-- 500-1000价格区间
(207, 4, 103, '空气净化器', 'PURIFIER001', 0, 1, 1, 1, 1, 207, 120, 799.00, 699.00, 40, 40, '家用空气净化器', 'HEPA过滤空气净化器', 999.00, 60, 6, '台', 5.50, 0, '1,2,3', '净化器', '家电', 0, '家居品牌', '家居生活'),
(208, 5, 104, '跑步机家用', 'TREADMILL001', 0, 1, 0, 1, 1, 208, 45, 699.00, 599.00, 35, 35, '家用折叠跑步机', '静音折叠跑步机', 899.00, 25, 3, '台', 35.00, 0, '1,2,3', '跑步机', '健身器材', 0, '运动品牌', '运动户外'),

-- 1000-3000价格区间
(209, 3, 102, '平板电脑', 'TABLET001', 0, 1, 1, 1, 1, 209, 85, 1599.00, 1399.00, 80, 80, '高性能平板电脑', '办公娱乐平板', 1899.00, 40, 4, '台', 0.80, 0, '1,2,3', '平板', '数码产品', 0, '数码品牌', '数码电器'),
(210, 1, 100, '羽绒服', 'DOWN001', 0, 1, 1, 1, 1, 210, 65, 1299.00, 1099.00, 65, 65, '加厚羽绒服', '保暖防风羽绒服', 1599.00, 30, 3, '件', 1.20, 0, '1,2,3', '羽绒服', '冬装', 0, '时尚品牌', '服饰鞋包'),

-- 3000+价格区间
(211, 3, 102, '笔记本电脑', 'LAPTOP001', 0, 1, 1, 1, 1, 211, 25, 5999.00, 5499.00, 300, 300, '商务办公笔记本', '高性能办公笔记本', 6999.00, 15, 2, '台', 2.20, 0, '1,2,3', '笔记本', '电脑', 0, '数码品牌', '数码电器'),
(212, 1, 100, '奢华手表', 'WATCH002', 0, 1, 1, 1, 1, 212, 8, 8999.00, 7999.00, 450, 450, '瑞士机械表', '精工制作机械手表', 12999.00, 5, 1, '块', 0.25, 0, '1,2,3', '手表', '奢侈品', 0, '时尚品牌', '服饰鞋包');

-- 4. 插入商品浏览记录（2025年数据）
INSERT INTO `pms_product_view_log` (`id`, `product_id`, `member_id`, `ip`, `user_agent`, `view_time`, `session_id`, `referer`) VALUES
-- 高浏览量商品
(1001, 100, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-10 09:15:00', 'sess_001', 'http://localhost:3000/'),
(1002, 100, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-10 10:20:00', 'sess_002', 'http://localhost:3000/category'),
(1003, 100, NULL, '192.168.1.101', 'Mozilla/5.0 (Android 11)', '2025-06-10 11:30:00', 'sess_003', 'http://localhost:3000/search'),
(1004, 100, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-11 14:25:00', 'sess_004', 'http://localhost:3000/'),
(1005, 100, NULL, '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X)', '2025-06-11 16:40:00', 'sess_005', 'http://localhost:3000/hot'),

(1006, 102, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-12 08:30:00', 'sess_006', 'http://localhost:3000/'),
(1007, 102, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-12 09:45:00', 'sess_007', 'http://localhost:3000/category'),
(1008, 102, NULL, '192.168.1.103', 'Mozilla/5.0 (Android 11)', '2025-06-12 13:20:00', 'sess_008', 'http://localhost:3000/'),
(1009, 102, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-13 10:15:00', 'sess_009', 'http://localhost:3000/recommend'),

(1010, 101, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-13 11:30:00', 'sess_010', 'http://localhost:3000/'),
(1011, 101, NULL, '192.168.1.104', 'Mozilla/5.0 (Android 11)', '2025-06-13 15:20:00', 'sess_011', 'http://localhost:3000/beauty'),
(1012, 101, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-14 09:40:00', 'sess_012', 'http://localhost:3000/'),

(1013, 103, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-14 12:15:00', 'sess_013', 'http://localhost:3000/home'),
(1014, 103, NULL, '192.168.1.105', 'Mozilla/5.0 (Android 11)', '2025-06-14 14:30:00', 'sess_014', 'http://localhost:3000/'),
(1015, 103, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-15 08:20:00', 'sess_015', 'http://localhost:3000/category'),

-- 中等浏览量商品
(1016, 104, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-15 10:45:00', 'sess_016', 'http://localhost:3000/sports'),
(1017, 104, NULL, '192.168.1.106', 'Mozilla/5.0 (Android 11)', '2025-06-15 13:25:00', 'sess_017', 'http://localhost:3000/'),
(1018, 104, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-16 11:10:00', 'sess_018', 'http://localhost:3000/fitness'),

(1019, 105, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-16 14:20:00', 'sess_019', 'http://localhost:3000/baby'),
(1020, 105, NULL, '192.168.1.107', 'Mozilla/5.0 (Android 11)', '2025-06-16 16:35:00', 'sess_020', 'http://localhost:3000/'),
(1021, 105, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-17 09:15:00', 'sess_021', 'http://localhost:3000/mother'),

-- 新增商品浏览记录
(1022, 200, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-17 10:30:00', 'sess_022', 'http://localhost:3000/books'),
(1023, 201, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-17 11:45:00', 'sess_023', 'http://localhost:3000/beauty'),
(1024, 204, NULL, '192.168.1.108', 'Mozilla/5.0 (Android 11)', '2025-06-17 14:20:00', 'sess_024', 'http://localhost:3000/fashion'),
(1025, 206, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-06-18 08:15:00', 'sess_025', 'http://localhost:3000/electronics'),
(1026, 209, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-06-18 09:30:00', 'sess_026', 'http://localhost:3000/digital'),
(1027, 211, NULL, '192.168.1.109', 'Mozilla/5.0 (Android 11)', '2025-06-18 13:45:00', 'sess_027', 'http://localhost:3000/computers'),

-- 7月份浏览记录
(1028, 100, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-07-01 09:20:00', 'sess_028', 'http://localhost:3000/'),
(1029, 102, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-07-01 11:35:00', 'sess_029', 'http://localhost:3000/tech'),
(1030, 204, NULL, '192.168.1.110', 'Mozilla/5.0 (Android 11)', '2025-07-02 14:20:00', 'sess_030', 'http://localhost:3000/fashion'),
(1031, 206, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-07-02 16:45:00', 'sess_031', 'http://localhost:3000/audio'),
(1032, 209, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-07-03 10:30:00', 'sess_032', 'http://localhost:3000/tablet'),
(1033, 211, NULL, '192.168.1.111', 'Mozilla/5.0 (Android 11)', '2025-07-03 13:15:00', 'sess_033', 'http://localhost:3000/laptop'),

-- 高转化率商品的更多浏览记录
(1034, 105, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-07-04 08:40:00', 'sess_034', 'http://localhost:3000/baby'),
(1035, 105, 3, NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', '2025-07-04 12:25:00', 'sess_035', 'http://localhost:3000/milk'),
(1036, 103, NULL, '192.168.1.112', 'Mozilla/5.0 (Android 11)', '2025-07-05 09:50:00', 'sess_036', 'http://localhost:3000/home'),
(1037, 103, 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2025-07-05 15:30:00', 'sess_037', 'http://localhost:3000/lighting');

-- 5. 插入商品收藏记录（2025年数据）
INSERT INTO `pms_member_product_collection` (`id`, `member_id`, `product_id`, `member_nickname`, `product_name`, `product_pic`, `product_sub_title`, `product_price`, `create_time`) VALUES
(2001, 1, 100, 'test', '时尚连衣裙', NULL, '夏季新款连衣裙', 299.00, '2025-06-10 10:00:00'),
(2002, 3, 100, 'admin', '时尚连衣裙', NULL, '夏季新款连衣裙', 299.00, '2025-06-10 11:30:00'),
(2003, 1, 102, 'test', '无线蓝牙耳机', NULL, '高品质蓝牙耳机', 399.00, '2025-06-12 09:15:00'),
(2004, 3, 101, 'admin', '保湿面膜', NULL, '补水保湿面膜', 89.00, '2025-06-13 12:20:00'),
(2005, 1, 103, 'test', '智能台灯', NULL, '护眼智能台灯', 199.00, '2025-06-14 15:45:00'),
(2006, 3, 105, 'admin', '婴儿奶粉', NULL, '婴幼儿配方奶粉', 268.00, '2025-06-16 08:30:00'),
(2007, 1, 104, 'test', '运动跑鞋', NULL, '专业运动跑鞋', 499.00, '2025-06-16 14:20:00'),
(2008, 3, 204, 'admin', '休闲衬衫', NULL, '商务休闲衬衫', 189.00, '2025-06-17 16:10:00'),
(2009, 1, 206, 'test', '蓝牙音箱', NULL, '便携蓝牙音箱', 199.00, '2025-06-18 09:45:00'),
(2010, 3, 209, 'admin', '平板电脑', NULL, '高性能平板电脑', 1599.00, '2025-06-18 11:20:00'),

-- 7月收藏记录
(2011, 1, 211, 'test', '笔记本电脑', NULL, '商务办公笔记本', 5999.00, '2025-07-01 10:30:00'),
(2012, 3, 207, 'admin', '空气净化器', NULL, '家用空气净化器', 799.00, '2025-07-02 13:45:00'),
(2013, 1, 201, 'test', '面膜单片', NULL, '补水单片面膜', 15.00, '2025-07-03 08:15:00'),
(2014, 3, 205, 'admin', '护肤套装', NULL, '基础护肤三件套', 288.00, '2025-07-04 14:50:00'),
(2015, 1, 208, 'test', '跑步机家用', NULL, '家用折叠跑步机', 699.00, '2025-07-05 11:25:00');

-- 6. 验证商品分析数据
-- 验证商品概览统计
SELECT '商品概览验证' as test_type, '商品总数' as metric, COUNT(*) as value FROM pms_product WHERE delete_status = 0
UNION ALL
SELECT '商品概览验证' as test_type, '在售商品数' as metric, COUNT(*) as value FROM pms_product WHERE delete_status = 0 AND publish_status = 1
UNION ALL
SELECT '商品概览验证' as test_type, '总浏览量' as metric, COUNT(*) as value FROM pms_product_view_log WHERE view_time BETWEEN '2025-06-01' AND '2025-07-31'
UNION ALL
SELECT '商品概览验证' as test_type, '总收藏量' as metric, COUNT(*) as value FROM pms_member_product_collection WHERE create_time BETWEEN '2025-06-01' AND '2025-07-31';

-- 验证商品销售排行
SELECT '销售排行验证' as test_type,
       pp.name as product_name,
       COALESCE(SUM(ooi.real_amount), 0) as sales_amount,
       COALESCE(SUM(ooi.product_quantity), 0) as sales_quantity
FROM pms_product pp
JOIN oms_order_item ooi ON pp.id = ooi.product_id
JOIN oms_order oo ON ooi.order_id = oo.id
WHERE oo.status != 4
    AND oo.create_time BETWEEN '2025-06-01' AND '2025-07-31'
GROUP BY pp.id, pp.name
ORDER BY sales_amount DESC
LIMIT 5;

-- 验证商品浏览排行
SELECT '浏览排行验证' as test_type,
       pp.name as product_name,
       COUNT(*) as view_count,
       COUNT(DISTINCT COALESCE(pvl.member_id, pvl.ip)) as unique_visitors
FROM pms_product pp
JOIN pms_product_view_log pvl ON pp.id = pvl.product_id
WHERE pvl.view_time BETWEEN '2025-06-01' AND '2025-07-31'
GROUP BY pp.id, pp.name
ORDER BY view_count DESC
LIMIT 5;

-- 验证价格区间分布
SELECT '价格分布验证' as test_type,
       CASE
           WHEN price BETWEEN 0 AND 100 THEN '0-100'
           WHEN price BETWEEN 100 AND 500 THEN '100-500'
           WHEN price BETWEEN 500 AND 1000 THEN '500-1000'
           WHEN price BETWEEN 1000 AND 3000 THEN '1000-3000'
           WHEN price > 3000 THEN '3000+'
           ELSE '未知'
       END as price_range,
       COUNT(*) as count
FROM pms_product
WHERE delete_status = 0 AND price IS NOT NULL
GROUP BY price_range
ORDER BY CASE price_range
    WHEN '0-100' THEN 1
    WHEN '100-500' THEN 2
    WHEN '500-1000' THEN 3
    WHEN '1000-3000' THEN 4
    WHEN '3000+' THEN 5
    ELSE 6
END;
