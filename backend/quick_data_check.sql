-- 快速数据检查脚本
-- 检查为什么只有这么少的数据

-- 1. 检查订单表基本情况
SELECT '订单表检查' as check_type, '总订单数' as item, COUNT(*) as count FROM oms_order
UNION ALL
SELECT '订单表检查', '有用户ID的订单', COUNT(*) FROM oms_order WHERE member_id IS NOT NULL
UNION ALL
SELECT '订单表检查', '最近30天订单', COUNT(*) FROM oms_order WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
UNION ALL
SELECT '订单表检查', '最近30天有用户ID', COUNT(*) FROM oms_order WHERE member_id IS NOT NULL AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 2. 按状态查看订单分布
SELECT 
    CONCAT('状态', status, '订单数') as item,
    COUNT(*) as count,
    COUNT(DISTINCT member_id) as unique_users
FROM oms_order 
WHERE member_id IS NOT NULL 
AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY status
ORDER BY status;

-- 3. 查看具体的订单数据
SELECT 
    '具体订单数据' as title,
    id,
    member_id,
    status,
    total_amount,
    create_time
FROM oms_order 
WHERE member_id IS NOT NULL 
AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY create_time DESC;

-- 4. 检查用户表
SELECT '用户表检查' as check_type, '总用户数' as item, COUNT(*) as count FROM ums_member
UNION ALL
SELECT '用户表检查', '启用用户数', COUNT(*) FROM ums_member WHERE status = 1;

-- 5. 检查是否有测试数据
SELECT 
    '数据分析' as title,
    '建议' as suggestion
WHERE 1=0
UNION ALL
SELECT '数据量评估', CASE 
    WHEN (SELECT COUNT(*) FROM oms_order WHERE member_id IS NOT NULL AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)) < 10 
    THEN '测试数据不足，建议添加更多测试订单'
    ELSE '数据量正常'
END;
