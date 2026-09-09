-- 商品分析功能测试SQL
-- 用于验证商品分析接口的数据查询功能

-- 1. 查看商品总数和在售商品数
SELECT
    (SELECT COUNT(*) FROM pms_product WHERE delete_status = 0) as total_products,
    (SELECT COUNT(*) FROM pms_product WHERE delete_status = 0 AND publish_status = 1) as on_sale_products;

-- 2. 查看商品分类销售统计（最近30天）
SELECT
    pc.id as category_id,
    pc.name as category_name,
    COALESCE(SUM(oi.product_price * oi.product_quantity), 0) as sales_amount,
    COUNT(DISTINCT oi.product_id) as product_count
FROM pms_product_category pc
LEFT JOIN pms_product p ON pc.id = p.product_category_id
LEFT JOIN oms_order_item oi ON p.id = oi.product_id
LEFT JOIN oms_order o ON oi.order_id = o.id
WHERE o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
AND o.status IN (2, 3, 4)
GROUP BY pc.id, pc.name
HAVING sales_amount > 0
ORDER BY sales_amount DESC
LIMIT 10;

-- 3. 查看商品销售排行（最近30天）
SELECT
    p.id as product_id,
    p.name as product_name,
    COALESCE(SUM(oi.product_price * oi.product_quantity), 0) as sales_amount,
    COALESCE(SUM(oi.product_quantity), 0) as sales_quantity
FROM pms_product p
LEFT JOIN oms_order_item oi ON p.id = oi.product_id
LEFT JOIN oms_order o ON oi.order_id = o.id
WHERE o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
AND o.status IN (2, 3, 4)
GROUP BY p.id, p.name
HAVING sales_amount > 0
ORDER BY sales_amount DESC
LIMIT 10;

-- 4. 查看商品价格区间分布
SELECT
    CASE
        WHEN price BETWEEN 0 AND 100 THEN '0-100'
        WHEN price BETWEEN 100 AND 500 THEN '100-500'
        WHEN price BETWEEN 500 AND 1000 THEN '500-1000'
        WHEN price BETWEEN 1000 AND 3000 THEN '1000-3000'
        WHEN price >= 3000 THEN '3000+'
        ELSE '其他'
    END as price_range,
    COUNT(*) as product_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM pms_product WHERE delete_status = 0), 2) as percentage
FROM pms_product
WHERE delete_status = 0
GROUP BY
    CASE
        WHEN price BETWEEN 0 AND 100 THEN '0-100'
        WHEN price BETWEEN 100 AND 500 THEN '100-500'
        WHEN price BETWEEN 500 AND 1000 THEN '500-1000'
        WHEN price BETWEEN 1000 AND 3000 THEN '1000-3000'
        WHEN price >= 3000 THEN '3000+'
        ELSE '其他'
    END
ORDER BY product_count DESC;

-- 5. 查看商品库存分布
SELECT
    CASE
        WHEN total_stock BETWEEN 0 AND 10 THEN '0-10'
        WHEN total_stock BETWEEN 10 AND 50 THEN '10-50'
        WHEN total_stock BETWEEN 50 AND 100 THEN '50-100'
        WHEN total_stock BETWEEN 100 AND 500 THEN '100-500'
        WHEN total_stock >= 500 THEN '500+'
        ELSE '其他'
    END as stock_range,
    COUNT(*) as product_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM (
        SELECT p.id FROM pms_product p WHERE p.delete_status = 0
    ) temp), 2) as percentage
FROM (
    SELECT
        p.id,
        p.name,
        COALESCE(SUM(ss.stock), 0) as total_stock
    FROM pms_product p
    LEFT JOIN pms_sku_stock ss ON p.id = ss.product_id
    WHERE p.delete_status = 0
    GROUP BY p.id, p.name
) stock_summary
GROUP BY
    CASE
        WHEN total_stock BETWEEN 0 AND 10 THEN '0-10'
        WHEN total_stock BETWEEN 10 AND 50 THEN '10-50'
        WHEN total_stock BETWEEN 50 AND 100 THEN '50-100'
        WHEN total_stock BETWEEN 100 AND 500 THEN '100-500'
        WHEN total_stock >= 500 THEN '500+'
        ELSE '其他'
    END
ORDER BY product_count DESC;

-- 6. 查看购买用户统计（最近30天）
SELECT
    COUNT(DISTINCT o.member_id) as unique_buyers,
    COUNT(*) as total_orders,
    ROUND(AVG(o.total_amount), 2) as avg_order_amount
FROM oms_order o
WHERE o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
AND o.status IN (2, 3, 4)
AND o.member_id IS NOT NULL;

-- 7. 查看商品购买用户数统计（最近30天，按商品分组）
SELECT
    oi.product_id,
    p.name as product_name,
    COUNT(DISTINCT o.member_id) as unique_buyers,
    SUM(oi.product_quantity) as total_quantity,
    SUM(oi.product_price * oi.product_quantity) as total_sales
FROM oms_order_item oi
JOIN oms_order o ON oi.order_id = o.id
JOIN pms_product p ON oi.product_id = p.id
WHERE o.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
AND o.status IN (2, 3, 4)
AND o.member_id IS NOT NULL
GROUP BY oi.product_id, p.name
ORDER BY unique_buyers DESC
LIMIT 10;

-- 8. 检查购物车数据
SELECT
    COUNT(*) as total_cart_items,
    COUNT(DISTINCT member_id) as unique_members_with_cart,
    COUNT(DISTINCT product_id) as unique_products_in_cart
FROM oms_cart_item
WHERE create_date >= DATE_SUB(NOW(), INTERVAL 30 DAY);
