# Java数据格式修复指南

## 问题描述

通过日志分析，我们发现推荐系统在计算用户偏好时无法获取有效的类别、价格和质量数据，导致推荐结果基本上是随机的。具体问题表现为：

1. 类别偏好为空：`WARNING: ⚠️ 警告: 动量中的类别偏好为空，这可能导致所有商品得分相同!`
2. 价格模型无效：`- 价格得分: 0.5000 (默认值, 无有效价格模型)`
3. 质量模型无效：`- 质量得分: 0.5000 (默认值, 无有效质量模型)`

## 根本原因

Java后端向Python服务发送的用户行为数据缺少必要的字段映射，导致Python服务无法提取有效的类别、价格和评分信息。

## 解决方案

### 1. 修改Java后端数据映射

在发送用户行为数据到Python服务之前，确保以下字段正确映射：

```java
// 订单数据映射示例
List<Map<String, Object>> ordersData = new ArrayList<>();
for (OmsOrder order : orders) {
    Map<String, Object> orderMap = new HashMap<>();
    
    // 添加必要字段
    orderMap.put("category", order.getProductCategoryName()); // 商品类别名称
    orderMap.put("price", order.getProductPrice());          // 商品价格
    orderMap.put("rating", order.getProductRating());        // 商品评分
    
    // 添加备用字段（以防主字段不存在）
    orderMap.put("product_category_name", order.getProductCategoryName());
    orderMap.put("productCategoryName", order.getProductCategoryName());
    orderMap.put("product_price", order.getProductPrice());
    orderMap.put("productPrice", order.getProductPrice());
    
    ordersData.add(orderMap);
}
```

### 2. 确保类别名称一致性

确保Java后端发送的类别名称与`config.py`中定义的默认类别名称一致：

```python
'default_category_preferences': {
    '手机通讯': 0.15,
    '笔记本': 0.15,
    '家用电器': 0.1,
    '服装': 0.1,
    '休闲裤': 0.05,
    'T恤': 0.05,
    // ...其他类别
}
```

### 3. 检查数据结构

确保Java后端发送的数据结构符合Python服务的期望：

```json
{
  "user_id": 1,
  "user_behavior_data": {
    "orders": [
      {
        "category": "手机通讯",
        "price": 3999.0,
        "rating": 4.8,
        "product_id": "123",
        "product_name": "iPhone 13"
      }
    ],
    "cart": [
      {
        "category": "笔记本",
        "price": 5999.0,
        "product_id": "456",
        "product_name": "MacBook Air"
      }
    ],
    "returns": []
  }
}
```

### 4. 检查日志输出

修改后，检查Python服务日志中的以下关键信息：

- `📊 原始行为数据统计: 订单=X, 购物车=Y, 退货=Z`
- `📋 订单数据样例: {...}`
- `📊 有效类别数据: X/Y (Z%)`
- `📊 不同类别数量: X`
- `📊 类别偏好数量: X`

如果类别偏好数量大于0，表示修复成功。

## 测试步骤

1. 修改Java后端数据映射
2. 重启服务
3. 执行用户推荐请求
4. 检查Python服务日志，确认类别偏好、价格模型和质量模型是否正确生成
5. 验证推荐结果是否不再全部得分相同

## 备注

如果修改Java代码不方便，也可以在Python服务端进一步增强数据处理能力，尝试从更多可能的字段中提取类别、价格和评分信息。我们已经在Python服务中添加了这些增强功能，但最好的解决方案仍然是确保Java端发送正确格式的数据。 