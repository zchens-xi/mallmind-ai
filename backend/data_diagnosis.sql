-- 数据诊断查询脚本
-- 用于检查系统中实际有多少数据

-- 1. 检查订单表数据
SELECT 
    '订单表总数' as table_name,
    COUNT(*) as total_count
FROM oms_order
UNION ALL
SELECT 
    '有效订单数(所有状态)',
    COUNT(*) 
FROM oms_order 
WHERE member_id IS NOT NULL
UNION ALL
SELECT 
    '已支付订单数(状态2,3,4)',
    COUNT(*) 
FROM oms_order 
WHERE member_id IS NOT NULL 
AND status IN (2, 3, 4)
UNION ALL
SELECT 
    '最近30天订单数',
    COUNT(*) 
FROM oms_order 
WHERE member_id IS NOT NULL 
AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
UNION ALL
SELECT 
    '最近30天已支付订单数',
    COUNT(*) 
FROM oms_order 
WHERE member_id IS NOT NULL 
AND status IN (2, 3, 4)
AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 2. 按状态分组查看订单数据
SELECT 
    status,
    CASE 
        WHEN status = 0 THEN '待付款'
        WHEN status = 1 THEN '待发货'
        WHEN status = 2 THEN '已发货'
        WHEN status = 3 THEN '已完成'
        WHEN status = 4 THEN '已关闭'
        WHEN status = 5 THEN '无效订单'
        ELSE '未知状态'
    END as status_name,
    COUNT(*) as count,
    COUNT(DISTINCT member_id) as unique_members
FROM oms_order 
WHERE member_id IS NOT NULL
GROUP BY status 
ORDER BY status;

-- 3. 查看最近的订单数据样本
SELECT 
    id,
    member_id,
    status,
    total_amount,
    pay_amount,
    create_time,
    DATE_FORMAT(create_time, '%Y-%m-%d') as create_date
FROM oms_order 
WHERE member_id IS NOT NULL 
ORDER BY create_time DESC 
LIMIT 10;

-- 4. 检查用户表数据
SELECT 
    '用户表总数' as table_name,
    COUNT(*) as total_count
FROM ums_member;

-- 5. 检查商品表数据
SELECT 
    '商品表总数' as table_name,
    COUNT(*) as total_count
FROM pms_product
UNION ALL
SELECT 
    '在售商品数',
    COUNT(*) 
FROM pms_product 
WHERE publish_status = 1 
AND delete_status = 0;

-- 6. 检查订单项表数据
SELECT 
    '订单项总数' as table_name,
    COUNT(*) as total_count
FROM oms_order_item;

-- 7. 查看时间范围内的具体数据
SELECT 
    DATE_FORMAT(create_time, '%Y-%m-%d') as order_date,
    COUNT(*) as order_count,
    COUNT(DISTINCT member_id) as unique_members,
    SUM(total_amount) as total_amount,
    GROUP_CONCAT(DISTINCT status) as statuses
FROM oms_order 
WHERE member_id IS NOT NULL
AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
ORDER BY order_date DESC;
