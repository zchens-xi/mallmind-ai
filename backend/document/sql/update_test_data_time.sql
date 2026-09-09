-- 更新测试数据时间到当前查询范围内

-- 更新订单数据时间 (从6月初改为6月下旬到7月)
UPDATE oms_order SET create_time = '2025-06-25 10:30:00' WHERE id = 100;
UPDATE oms_order SET create_time = '2025-06-26 11:20:00' WHERE id = 101;
UPDATE oms_order SET create_time = '2025-06-27 14:15:00' WHERE id = 102;
UPDATE oms_order SET create_time = '2025-06-28 09:45:00' WHERE id = 103;
UPDATE oms_order SET create_time = '2025-06-29 16:30:00' WHERE id = 104;
UPDATE oms_order SET create_time = '2025-06-30 12:10:00' WHERE id = 105;
UPDATE oms_order SET create_time = '2025-07-01 18:20:00' WHERE id = 106;
UPDATE oms_order SET create_time = '2025-07-02 08:50:00' WHERE id = 107;
UPDATE oms_order SET create_time = '2025-07-03 20:40:00' WHERE id = 108;

-- 更新登录日志时间
UPDATE ums_member_login_log SET create_time = '2025-06-25 07:15:00' WHERE id = 16;
UPDATE ums_member_login_log SET create_time = '2025-06-25 08:30:00' WHERE id = 17;
UPDATE ums_member_login_log SET create_time = '2025-06-26 06:45:00' WHERE id = 18;
UPDATE ums_member_login_log SET create_time = '2025-06-26 07:20:00' WHERE id = 19;
UPDATE ums_member_login_log SET create_time = '2025-06-27 09:15:00' WHERE id = 20;
UPDATE ums_member_login_log SET create_time = '2025-06-27 10:30:00' WHERE id = 21;
UPDATE ums_member_login_log SET create_time = '2025-06-28 11:45:00' WHERE id = 22;
UPDATE ums_member_login_log SET create_time = '2025-06-28 10:20:00' WHERE id = 23;
UPDATE ums_member_login_log SET create_time = '2025-06-29 11:15:00' WHERE id = 24;
UPDATE ums_member_login_log SET create_time = '2025-06-29 12:50:00' WHERE id = 25;
UPDATE ums_member_login_log SET create_time = '2025-06-30 12:30:00' WHERE id = 26;
UPDATE ums_member_login_log SET create_time = '2025-06-30 13:15:00' WHERE id = 27;
UPDATE ums_member_login_log SET create_time = '2025-07-01 14:45:00' WHERE id = 28;
UPDATE ums_member_login_log SET create_time = '2025-07-01 15:30:00' WHERE id = 29;
UPDATE ums_member_login_log SET create_time = '2025-07-02 15:20:00' WHERE id = 30;
UPDATE ums_member_login_log SET create_time = '2025-07-02 16:40:00' WHERE id = 31;
UPDATE ums_member_login_log SET create_time = '2025-07-03 14:30:00' WHERE id = 32;
UPDATE ums_member_login_log SET create_time = '2025-07-03 17:15:00' WHERE id = 33;
UPDATE ums_member_login_log SET create_time = '2025-07-04 18:45:00' WHERE id = 34;
UPDATE ums_member_login_log SET create_time = '2025-07-04 19:30:00' WHERE id = 35;
UPDATE ums_member_login_log SET create_time = '2025-07-05 20:15:00' WHERE id = 36;
UPDATE ums_member_login_log SET create_time = '2025-07-05 21:45:00' WHERE id = 37;
UPDATE ums_member_login_log SET create_time = '2025-07-06 18:20:00' WHERE id = 38;
UPDATE ums_member_login_log SET create_time = '2025-07-06 22:30:00' WHERE id = 39;
UPDATE ums_member_login_log SET create_time = '2025-07-07 19:50:00' WHERE id = 40;
UPDATE ums_member_login_log SET create_time = '2025-07-07 23:15:00' WHERE id = 41;
UPDATE ums_member_login_log SET create_time = '2025-07-08 00:45:00' WHERE id = 42;
UPDATE ums_member_login_log SET create_time = '2025-07-08 01:30:00' WHERE id = 43;
UPDATE ums_member_login_log SET create_time = '2025-07-09 02:20:00' WHERE id = 44;
UPDATE ums_member_login_log SET create_time = '2025-07-09 03:10:00' WHERE id = 45;

-- 添加更多登录日志数据以增加设备分布样本
INSERT INTO ums_member_login_log VALUES
(46, 3, '2025-06-25 14:30:00', '192.168.1.101', '上海', 0, '上海'),
(47, 4, '2025-06-26 15:20:00', '192.168.1.102', '深圳', 1, '广东'),
(48, 5, '2025-06-27 16:15:00', '192.168.1.103', '广州', 2, '广东'),
(49, 6, '2025-06-28 17:45:00', '192.168.1.104', '杭州', 0, '浙江'),
(50, 7, '2025-06-29 18:30:00', '192.168.1.105', '成都', 1, '四川'),
(51, 8, '2025-06-30 19:20:00', '192.168.1.106', '武汉', 2, '湖北'),
(52, 9, '2025-07-01 20:15:00', '192.168.1.107', '西安', 0, '陕西'),
(53, 10, '2025-07-02 21:10:00', '192.168.1.108', '南京', 1, '江苏'),
(54, 12, '2025-07-03 22:05:00', '192.168.1.109', '北京', 2, '北京'),
(55, 13, '2025-07-04 09:30:00', '192.168.1.110', '上海', 0, '上海'),
(56, 14, '2025-07-05 10:25:00', '192.168.1.111', '深圳', 1, '广东'),
(57, 15, '2025-07-06 11:20:00', '192.168.1.112', '广州', 2, '广东'),
(58, 16, '2025-07-07 12:15:00', '192.168.1.113', '杭州', 0, '浙江'),
(59, 17, '2025-07-08 13:10:00', '192.168.1.114', '成都', 1, '四川'),
(60, 18, '2025-07-09 14:05:00', '192.168.1.115', '武汉', 2, '湖北');

-- 验证更新结果
SELECT '订单数据时间范围' as table_name, 
       MIN(create_time) as min_time, 
       MAX(create_time) as max_time, 
       COUNT(*) as total_count
FROM oms_order 
WHERE create_time BETWEEN '2025-06-21' AND '2025-07-21'
UNION ALL
SELECT '登录日志时间范围' as table_name, 
       MIN(create_time) as min_time, 
       MAX(create_time) as max_time, 
       COUNT(*) as total_count
FROM ums_member_login_log 
WHERE create_time BETWEEN '2025-06-21' AND '2025-07-21';
