-- 用户兴趣标签和消费偏好测试数据（2025年数据）
-- 修正时间范围以匹配当前接口查询

-- 1. 插入2025年的测试订单数据
INSERT IGNORE INTO `oms_order` (`id`, `member_id`, `order_sn`, `create_time`, `member_username`, `total_amount`, `pay_amount`, `freight_amount`, `promotion_amount`, `integration_amount`, `coupon_amount`, `discount_amount`, `pay_type`, `source_type`, `status`, `order_type`, `delivery_company`, `delivery_sn`, `auto_confirm_day`, `integration`, `growth`, `promotion_info`, `bill_type`, `bill_header`, `bill_content`, `bill_receiver_phone`, `bill_receiver_email`, `receiver_name`, `receiver_phone`, `receiver_post_code`, `receiver_province`, `receiver_city`, `receiver_region`, `receiver_detail_address`, `note`, `confirm_status`, `delete_status`, `use_integration`, `payment_time`, `delivery_time`, `receive_time`, `comment_time`, `modify_time`) VALUES
-- 时尚达人购买服饰、美妆（2025年6月-7月）
(30001, 1, '202506150001', '2025-06-15 14:30:00', 'test', 1200.00, 1200.00, 0.00, 0.00, 0.00, 0.00, 0.00, 1, 1, 3, 0, NULL, NULL, 15, 120, 120, NULL, 0, NULL, NULL, NULL, NULL, '测试用户', '13800001001', NULL, '北京', '朝阳区', '三里屯', '时尚大厦A座1001', NULL, 1, 0, 0, '2025-06-15 14:35:00', '2025-06-16 10:00:00', '2025-06-17 16:20:00', NULL, NULL),
(30002, 3, '202506200002', '2025-06-20 10:15:00', 'admin', 890.00, 890.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 3, 0, NULL, NULL, 15, 89, 89, NULL, 0, NULL, NULL, NULL, NULL, '管理员', '13800001002', NULL, '北京', '朝阳区', '三里屯', '时尚大厦A座1001', NULL, 1, 0, 0, '2025-06-20 10:20:00', '2025-06-21 09:30:00', '2025-06-22 14:15:00', NULL, NULL),
-- 数码产品订单
(30003, 1, '202507050003', '2025-07-05 16:45:00', 'test', 2399.00, 2399.00, 0.00, 0.00, 0.00, 0.00, 0.00, 1, 0, 3, 0, NULL, NULL, 15, 239, 239, NULL, 0, NULL, NULL, NULL, NULL, '测试用户', '13800001002', NULL, '上海', '浦东新区', '陆家嘴', '科技园区B座2001', NULL, 1, 0, 0, '2025-07-05 16:50:00', '2025-07-06 11:20:00', '2025-07-07 15:30:00', NULL, NULL),
(30004, 3, '202507100004', '2025-07-10 11:20:00', 'admin', 1599.00, 1599.00, 0.00, 0.00, 0.00, 0.00, 0.00, 1, 0, 3, 0, NULL, NULL, 15, 159, 159, NULL, 0, NULL, NULL, NULL, NULL, '管理员', '13800001003', NULL, '上海', '浦东新区', '陆家嘴', '科技园区B座2001', NULL, 1, 0, 0, '2025-07-10 11:25:00', '2025-07-11 08:45:00', '2025-07-12 16:10:00', NULL, NULL),
-- 家居生活订单
(30005, 1, '202507150005', '2025-07-15 13:10:00', 'test', 799.00, 799.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 2, 3, 0, NULL, NULL, 15, 79, 79, NULL, 0, NULL, NULL, NULL, NULL, '测试用户', '13800001003', NULL, '广州', '天河区', '珠江新城', '幸福家园3栋1801', NULL, 1, 0, 0, '2025-07-15 13:15:00', '2025-07-16 14:20:00', '2025-07-17 17:45:00', NULL, NULL),
-- 运动户外订单
(30006, 3, '202507180006', '2025-07-18 15:30:00', 'admin', 1199.00, 1199.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 2, 3, 0, NULL, NULL, 15, 119, 119, NULL, 0, NULL, NULL, NULL, NULL, '管理员', '13800001004', NULL, '深圳', '南山区', '科技园', '健身中心C座601', NULL, 1, 0, 0, '2025-07-18 15:35:00', '2025-07-19 10:15:00', '2025-07-20 19:20:00', NULL, NULL);

-- 2. 插入对应的订单商品项数据（2025年数据）
INSERT IGNORE INTO `oms_order_item` (`id`, `order_id`, `product_id`, `product_name`, `product_sn`, `product_price`, `product_quantity`, `product_category_id`, `promotion_amount`, `coupon_amount`, `integration_amount`, `real_amount`, `gift_integration`, `gift_growth`) VALUES
-- 时尚达人订单商品（服饰、美妆）
(40001, 30001, 100, '时尚连衣裙', 'DRESS001', 299.00, 2, 100, 0.00, 0.00, 0.00, 598.00, 0, 0),
(40002, 30001, 101, '保湿面膜', 'MASK001', 89.00, 4, 101, 0.00, 0.00, 0.00, 356.00, 0, 0),
(40003, 30001, 108, '口红套装', 'LIPSTICK001', 123.00, 2, 101, 0.00, 0.00, 0.00, 246.00, 0, 0),
(40004, 30002, 109, '高跟鞋', 'HEELS001', 445.00, 2, 100, 0.00, 0.00, 0.00, 890.00, 0, 0),

-- 数码产品订单商品
(40005, 30003, 102, '无线蓝牙耳机', 'HEADPHONE001', 399.00, 3, 102, 0.00, 0.00, 0.00, 1197.00, 0, 0),
(40006, 30003, 110, '智能手表', 'WATCH001', 1202.00, 1, 102, 0.00, 0.00, 0.00, 1202.00, 0, 0),
(40007, 30004, 111, '移动硬盘', 'HDD001', 799.00, 2, 102, 0.00, 0.00, 0.00, 1598.00, 0, 0),
(40008, 30004, 112, '无线鼠标', 'MOUSE001', 1.00, 1, 102, 0.00, 0.00, 0.00, 1.00, 0, 0),

-- 家居生活订单商品
(40009, 30005, 103, '智能台灯', 'LAMP001', 199.00, 2, 103, 0.00, 0.00, 0.00, 398.00, 0, 0),
(40010, 30005, 113, '收纳盒套装', 'BOX001', 134.00, 3, 103, 0.00, 0.00, 0.00, 402.00, 0, 0),

-- 运动户外订单商品
(40011, 30006, 104, '运动跑鞋', 'SHOES001', 499.00, 2, 104, 0.00, 0.00, 0.00, 998.00, 0, 0),
(40012, 30006, 116, '健身器材', 'FITNESS001', 201.00, 1, 104, 0.00, 0.00, 0.00, 201.00, 0, 0);

-- 3. 验证查询 - 测试用户兴趣标签（使用2025年时间范围）
SELECT '用户兴趣标签测试' as test_type,
       pc.name as tag,
       COUNT(DISTINCT oo.member_id) as count
FROM oms_order oo
JOIN oms_order_item ooi ON oo.id = ooi.order_id
JOIN pms_product pp ON ooi.product_id = pp.id
JOIN pms_product_category pc ON pp.product_category_id = pc.id
WHERE oo.status != 4
    AND oo.create_time BETWEEN '2025-05-01' AND '2025-07-31'
GROUP BY pc.name
ORDER BY count DESC
LIMIT 20;

-- 4. 验证查询 - 测试用户消费偏好（使用2025年时间范围）
SELECT '用户消费偏好测试' as test_type,
       pc.name as category,
       COALESCE(SUM(ooi.real_amount), 0) as amount
FROM oms_order oo
JOIN oms_order_item ooi ON oo.id = ooi.order_id
JOIN pms_product pp ON ooi.product_id = pp.id
JOIN pms_product_category pc ON pp.product_category_id = pc.id
WHERE oo.status != 4
    AND oo.create_time BETWEEN '2025-05-01' AND '2025-07-31'
GROUP BY pc.name
ORDER BY amount DESC
LIMIT 10;
