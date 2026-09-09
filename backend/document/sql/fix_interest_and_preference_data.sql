-- 修复用户兴趣标签和消费偏好数据

-- 1. 确保商品分类数据存在
INSERT IGNORE INTO `pms_product_category` VALUES
(100, 0, '服饰鞋包', 0, 100, '件', 1, 1, 1, 'fashion', '时尚服饰', '时尚服饰鞋包'),
(101, 0, '美妆护肤', 0, 100, '件', 1, 1, 1, 'beauty', '美妆护肤', '美妆护肤用品'),
(102, 0, '数码电器', 0, 100, '件', 1, 1, 1, 'electronics', '数码电器', '数码电器产品'),
(103, 0, '家居生活', 0, 100, '件', 1, 1, 1, 'home', '家居生活', '家居生活用品'),
(104, 0, '运动户外', 0, 100, '件', 1, 1, 1, 'sports', '运动户外', '运动户外用品'),
(105, 0, '母婴童装', 0, 100, '件', 1, 1, 1, 'baby', '母婴童装', '母婴童装用品'),
(106, 0, '食品饮料', 0, 100, '件', 1, 1, 1, 'food', '食品饮料', '食品饮料商品'),
(107, 0, '图书文具', 0, 100, '件', 1, 1, 1, 'books', '图书文具', '图书文具用品');

-- 2. 确保商品数据存在
INSERT IGNORE INTO `pms_product` (`id`, `brand_id`, `product_category_id`, `name`, `product_sn`, `delete_status`, `publish_status`, `new_status`, `recommand_status`, `verify_status`, `sort`, `sale`, `price`, `promotion_price`, `gift_growth`, `gift_point`, `sub_title`, `description`, `original_price`, `stock`, `low_stock`, `unit`, `weight`, `preview_status`, `service_ids`, `keywords`, `note`, `promotion_type`, `brand_name`, `product_category_name`) VALUES
(100, 1, 100, '时尚连衣裙', 'DRESS001', 0, 1, 1, 1, 1, 100, 150, 299.00, 199.00, 10, 10, '夏季新款连衣裙', '时尚舒适的夏季连衣裙', 399.00, 100, 10, '件', 0.50, 0, '1,2,3', '连衣裙', '时尚连衣裙', 0, '时尚品牌', '服饰鞋包'),
(101, 2, 101, '保湿面膜', 'MASK001', 0, 1, 1, 1, 1, 101, 200, 89.00, 59.00, 5, 5, '补水保湿面膜', '深层补水保湿面膜', 129.00, 200, 20, '盒', 0.30, 0, '1,2,3', '面膜', '保湿面膜', 0, '美妆品牌', '美妆护肤'),
(102, 3, 102, '无线蓝牙耳机', 'HEADPHONE001', 0, 1, 1, 1, 1, 102, 300, 399.00, 299.00, 20, 20, '高品质蓝牙耳机', '高品质无线蓝牙耳机', 599.00, 150, 15, '副', 0.20, 0, '1,2,3', '耳机', '蓝牙耳机', 0, '数码品牌', '数码电器'),
(103, 4, 103, '智能台灯', 'LAMP001', 0, 1, 1, 1, 1, 103, 80, 199.00, 149.00, 10, 10, '护眼智能台灯', '护眼智能调光台灯', 299.00, 80, 8, '个', 1.20, 0, '1,2,3', '台灯', '智能台灯', 0, '家居品牌', '家居生活'),
(104, 5, 104, '运动跑鞋', 'SHOES001', 0, 1, 1, 1, 1, 104, 120, 499.00, 399.00, 25, 25, '专业运动跑鞋', '轻便舒适运动跑鞋', 699.00, 120, 12, '双', 0.80, 0, '1,2,3', '跑鞋', '运动跑鞋', 0, '运动品牌', '运动户外'),
(105, 6, 105, '婴儿奶粉', 'MILK001', 0, 1, 1, 1, 1, 105, 250, 268.00, 238.00, 15, 15, '婴幼儿配方奶粉', '营养丰富婴儿奶粉', 328.00, 200, 20, '罐', 0.90, 0, '1,2,3', '奶粉', '婴儿奶粉', 0, '母婴品牌', '母婴童装'),
(106, 7, 106, '有机茶叶', 'TEA001', 0, 1, 1, 1, 1, 106, 90, 158.00, 128.00, 8, 8, '有机绿茶', '高品质有机绿茶', 206.00, 90, 9, '盒', 0.25, 0, '1,2,3', '茶叶', '有机茶叶', 0, '食品品牌', '食品饮料'),
(107, 8, 107, '学习笔记本', 'BOOK001', 0, 1, 1, 1, 1, 107, 60, 29.00, 19.00, 3, 3, '学生笔记本', '高质量学习笔记本', 39.00, 300, 30, '本', 0.15, 0, '1,2,3', '笔记本', '学习笔记本', 0, '文具品牌', '图书文具');

-- 3. 确保订单商品项数据存在并且real_amount字段有值
INSERT IGNORE INTO `oms_order_item` (`id`, `order_id`, `product_id`, `product_name`, `product_sn`, `product_price`, `product_quantity`, `product_category_id`, `promotion_amount`, `coupon_amount`, `integration_amount`, `real_amount`, `gift_integration`, `gift_growth`) VALUES
(100, 100, 100, '时尚连衣裙', 'DRESS001', 299.00, 2, 100, 0.00, 0.00, 0.00, 598.00, 0, 0),
(101, 101, 101, '保湿面膜', 'MASK001', 89.00, 2, 101, 0.00, 0.00, 0.00, 178.00, 0, 0),
(102, 101, 107, '学习笔记本', 'BOOK001', 29.00, 2, 107, 0.00, 0.00, 0.00, 58.00, 0, 0),
(103, 102, 102, '无线蓝牙耳机', 'HEADPHONE001', 399.00, 2, 102, 0.00, 0.00, 0.00, 798.00, 0, 0),
(104, 103, 104, '运动跑鞋', 'SHOES001', 499.00, 1, 104, 0.00, 0.00, 0.00, 499.00, 0, 0),
(105, 104, 103, '智能台灯', 'LAMP001', 199.00, 1, 103, 0.00, 0.00, 0.00, 199.00, 0, 0),
(106, 104, 104, '运动跑鞋', 'SHOES001', 499.00, 2, 104, 0.00, 0.00, 0.00, 998.00, 0, 0),
(107, 105, 101, '保湿面膜', 'MASK001', 89.00, 3, 101, 0.00, 0.00, 0.00, 267.00, 0, 0),
(108, 105, 100, '时尚连衣裙', 'DRESS001', 299.00, 1, 100, 0.00, 0.00, 0.00, 299.00, 0, 0),
(109, 106, 105, '婴儿奶粉', 'MILK001', 268.00, 2, 105, 0.00, 0.00, 0.00, 536.00, 0, 0),
(110, 106, 102, '无线蓝牙耳机', 'HEADPHONE001', 399.00, 1, 102, 0.00, 0.00, 0.00, 399.00, 0, 0),
(111, 107, 106, '有机茶叶', 'TEA001', 158.00, 1, 106, 0.00, 0.00, 0.00, 158.00, 0, 0),
(112, 107, 107, '学习笔记本', 'BOOK001', 29.00, 3, 107, 0.00, 0.00, 0.00, 87.00, 0, 0),
(113, 107, 103, '智能台灯', 'LAMP001', 199.00, 1, 103, 0.00, 0.00, 0.00, 199.00, 0, 0),
(114, 108, 100, '时尚连衣裙', 'DRESS001', 299.00, 2, 100, 0.00, 0.00, 0.00, 598.00, 0, 0),
(115, 108, 102, '无线蓝牙耳机', 'HEADPHONE001', 399.00, 1, 102, 0.00, 0.00, 0.00, 399.00, 0, 0),
(116, 108, 106, '有机茶叶', 'TEA001', 158.00, 2, 106, 0.00, 0.00, 0.00, 316.00, 0, 0);

-- 4. 更新订单商品项的real_amount字段（如果为空或0）
UPDATE oms_order_item 
SET real_amount = CASE 
    WHEN real_amount IS NULL OR real_amount = 0 THEN (product_price * product_quantity)
    ELSE real_amount 
END
WHERE order_id IN (100, 101, 102, 103, 104, 105, 106, 107, 108);

-- 5. 验证数据完整性
SELECT '验证结果' as step, '订单数据' as type, COUNT(*) as count 
FROM oms_order 
WHERE create_time BETWEEN '2025-06-21' AND '2025-07-21'
UNION ALL
SELECT '验证结果' as step, '订单商品项' as type, COUNT(*) as count 
FROM oms_order_item ooi
JOIN oms_order oo ON ooi.order_id = oo.id
WHERE oo.create_time BETWEEN '2025-06-21' AND '2025-07-21'
UNION ALL
SELECT '验证结果' as step, '商品分类' as type, COUNT(*) as count 
FROM pms_product_category 
WHERE id BETWEEN 100 AND 107
UNION ALL
SELECT '验证结果' as step, '商品信息' as type, COUNT(*) as count 
FROM pms_product 
WHERE id BETWEEN 100 AND 107;

-- 6. 测试查询（兴趣标签）
SELECT '测试兴趣标签' as test_name,
       pc.name as tag,
       COUNT(DISTINCT oo.member_id) as count
FROM oms_order oo
JOIN oms_order_item ooi ON oo.id = ooi.order_id
JOIN pms_product pp ON ooi.product_id = pp.id
JOIN pms_product_category pc ON pp.product_category_id = pc.id
WHERE oo.status != 4
    AND oo.create_time BETWEEN '2025-06-21' AND '2025-07-21'
GROUP BY pc.name
ORDER BY count DESC
LIMIT 20;

-- 7. 测试查询（消费偏好）
SELECT '测试消费偏好' as test_name,
       pc.name as category,
       COALESCE(SUM(ooi.real_amount), 0) as amount
FROM oms_order oo
JOIN oms_order_item ooi ON oo.id = ooi.order_id
JOIN pms_product pp ON ooi.product_id = pp.id
JOIN pms_product_category pc ON pp.product_category_id = pc.id
WHERE oo.status != 4
    AND oo.create_time BETWEEN '2025-06-21' AND '2025-07-21'
GROUP BY pc.name
ORDER BY amount DESC
LIMIT 10;
