-- 数据一致性检查脚本
-- 用于检查MongoDB浏览记录和MySQL订单记录的一致性

-- 1. 检查购买用户但没有浏览记录的用户
-- 这些用户在订单表中存在，但在MongoDB浏览历史中不存在
SELECT DISTINCT 
    o.member_id,
    m.username,
    COUNT(o.id) as order_count,
    MIN(o.create_time) as first_order_time,
    MAX(o.create_time) as last_order_time
FROM oms_order o
LEFT JOIN ums_member m ON o.member_id = m.id
WHERE o.status IN (2, 3, 4) -- 已支付订单
AND o.member_id IS NOT NULL
AND o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY o.member_id, m.username
ORDER BY order_count DESC;

-- 2. 检查订单中涉及的商品
SELECT DISTINCT
    oi.product_id,
    p.name as product_name,
    COUNT(DISTINCT o.member_id) as buy_user_count,
    COUNT(oi.id) as total_orders,
    SUM(oi.quantity) as total_quantity,
    SUM(oi.real_amount) as total_amount
FROM oms_order o
JOIN oms_order_item oi ON o.id = oi.order_id
LEFT JOIN pms_product p ON oi.product_id = p.id
WHERE o.status IN (2, 3, 4)
AND o.member_id IS NOT NULL
AND o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY oi.product_id, p.name
ORDER BY buy_user_count DESC;

-- 3. 检查用户购买统计
SELECT 
    COUNT(DISTINCT o.member_id) as total_buy_users,
    COUNT(o.id) as total_orders,
    DATE(o.create_time) as order_date
FROM oms_order o
WHERE o.status IN (2, 3, 4)
AND o.member_id IS NOT NULL
AND o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(o.create_time)
ORDER BY order_date DESC;

-- 4. 生成MongoDB浏览记录脚本（用于修复数据）
-- 为每个购买用户创建对应的浏览记录
SELECT CONCAT(
    'db.memberReadHistory.insertOne({',
    '"memberId": ', o.member_id, ',',
    '"productId": ', oi.product_id, ',',
    '"productName": "', COALESCE(p.name, '商品'), '",',
    '"categoryId": ', COALESCE(p.product_category_id, 0), ',',
    '"createTime": new Date("', DATE_FORMAT(DATE_SUB(o.create_time, INTERVAL 1 HOUR), '%Y-%m-%dT%H:%i:%s.000Z'), '")});'
) as mongo_insert_script
FROM oms_order o
JOIN oms_order_item oi ON o.id = oi.order_id
LEFT JOIN pms_product p ON oi.product_id = p.id
WHERE o.status IN (2, 3, 4)
AND o.member_id IS NOT NULL
AND o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY o.create_time;
