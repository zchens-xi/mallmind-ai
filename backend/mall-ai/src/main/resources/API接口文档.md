# FastAPI推荐算法微服务接口文档

## 服务概述
- **服务名称**: Mall AI Recommendation Service
- **版本**: 2.0.0
- **基础URL**: `http://localhost:8000`
- **文档地址**: `http://localhost:8000/docs`
- **架构**: 专注于推荐算法计算，数据IO由SpringBoot管理

---

## 1. 基础服务接口

### 1.1 根路径健康检查
**接口**: `GET /`

**功能**: 服务基础状态检查

**输入**: 无

**输出**:
```json
{
  "service": "Mall AI Recommendation Service",
  "status": "healthy",
  "version": "2.0.0",
  "description": "专注于推荐算法计算的微服务",
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 1.2 详细健康检查
**接口**: `GET /health`

**功能**: 详细的服务健康状态信息

**输入**: 无

**输出**:
```json
{
  "status": "healthy",
  "timestamp": "2025-07-17T10:30:00.123456",
  "service_info": {
    "algorithm_engine": "initialized",
    "dependencies": "minimal",
    "data_storage": "external_springboot"
  }
}
```

---

## 2. 动量计算接口

### 2.1 计算长期动量
**接口**: `POST /api/v1/calculate-long-term-momentum`

**功能**: 基于用户历史行为数据计算长期购买偏好动量

**输入**:
```json
{
  "user_id": 12345,
  "user_behavior_data": {
    "orders": [
      {
        "order_id": "ORD001",
        "product_id": "P001",
        "category": "电子产品",
        "price": 2999.0,
        "rating": 4.8,
        "quantity": 1,
        "order_date": "2025-06-01"
      }
    ],
    "cart": [
      {
        "product_id": "P002",
        "category": "服装",
        "price": 299.0,
        "rating": 4.2,
        "add_time": "2025-07-15"
      }
    ],
    "returns": [
      {
        "product_id": "P003",
        "category": "家居",
        "price": 150.0,
        "rating": 2.5,
        "return_reason": "质量问题",
        "return_date": "2025-06-20"
      }
    ]
  }
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "type": "long_term_momentum",
    "user_id": 12345,
    "category_preferences": {
      "电子产品": 0.8,
      "服装": 0.3,
      "家居": -0.2
    },
    "price_model": {
      "mean": 1850.5,
      "std": 780.2,
      "skewness": 0.15,
      "min": 299.0,
      "max": 2999.0,
      "median": 1500.0,
      "count": 15
    },
    "quality_model": {
      "mean": 4.2,
      "std": 0.6,
      "skewness": -0.3,
      "min": 3.5,
      "max": 4.9,
      "median": 4.3,
      "count": 15
    },
    "last_updated": "2025-07-17T10:30:00.123456",
    "data_source": "user_behavior_analysis"
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 2.2 计算短期动量
**接口**: `POST /api/v1/calculate-short-term-momentum`

**功能**: 基于单次或多次点击行为计算即时兴趣动量，生成与长期动量结构一致的偏好分布

**输入**:
```json
{
  "user_id": 12345,
  "clicked_products": [
    {
      "product_id": "P004",
      "category": "数码配件",
      "price": 199.0,
      "rating": 4.5,
      "stock": 100,
      "is_new": true,
      "brand": "Apple",
      "name": "AirPods Pro"
    },
    {
      "product_id": "P005",
      "category": "数码配件",
      "price": 299.0,
      "rating": 4.7,
      "stock": 50,
      "is_new": false,
      "brand": "Sony",
      "name": "WH-1000XM5"
    }
  ],
  "click_strengths": [0.8, 1.2]
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "type": "short_term",
    "category_preferences": {
      "数码配件": 1.0
    },
    "price_model": {
      "mean": 249.0,
      "std": 70.7,
      "skewness": 0.0,
      "min": 199.0,
      "max": 299.0,
      "median": 249.0,
      "count": 6
    },
    "quality_model": {
      "mean": 4.6,
      "std": 0.14,
      "skewness": 0.0,
      "min": 4.5,
      "max": 4.7,
      "median": 4.6,
      "count": 6
    },
    "total_clicks": 2,
    "total_strength": 2.0,
    "clicked_categories": ["数码配件"],
    "timestamp": "2025-07-17T10:30:00.123456",
    "data_source": "user_clicks_aggregated"
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 2.3 计算推送动量
**接口**: `POST /api/v1/calculate-push-momentum`

**功能**: 融合长期动量和短期动量，生成用于推荐的累积动量

**输入**:
```json
{
  "long_term_momentum": {
    "type": "long_term",
    "category_preferences": {
      "电子产品": 0.7,
      "数码配件": 0.5
    },
    "price_model": {
      "mean": 1200.0,
      "std": 500.0,
      "skewness": 0.2,
      "min": 100.0,
      "max": 3000.0,
      "median": 1000.0,
      "count": 20
    },
    "quality_model": {
      "mean": 4.3,
      "std": 0.6,
      "skewness": -0.1,
      "min": 3.5,
      "max": 4.9,
      "median": 4.4,
      "count": 20
    }
  },
  "short_term_momentum": {
    "type": "short_term",
    "category_preferences": {
      "数码配件": 1.0
    },
    "price_model": {
      "mean": 199.0,
      "std": 0.0,
      "skewness": 0.0,
      "min": 199.0,
      "max": 199.0,
      "median": 199.0,
      "count": 2
    },
    "quality_model": {
      "mean": 4.5,
      "std": 0.0,
      "skewness": 0.0,
      "min": 4.5,
      "max": 4.5,
      "median": 4.5,
      "count": 2
    },
    "total_clicks": 1,
    "total_strength": 0.8,
    "clicked_categories": ["数码配件"],
    "timestamp": "2025-07-17T10:30:00.123456",
    "data_source": "user_clicks_aggregated"
  },
  "current_push_momentum": {
    "type": "push_momentum",
    "cumulative_clicks": 5,
    "category_preferences": {
      "电子产品": 0.8,
      "数码配件": 0.6
    },
    "price_model": {
      "mean": 800.0,
      "std": 400.0,
      "skewness": 0.1,
      "min": 150.0,
      "max": 2000.0,
      "median": 750.0,
      "count": 12
    },
    "quality_model": {
      "mean": 4.4,
      "std": 0.5,
      "skewness": -0.2,
      "min": 3.8,
      "max": 4.8,
      "median": 4.5,
      "count": 12
    }
  }
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "type": "push_momentum",
    "cumulative_clicks": 6,
    "category_preferences": {
      "电子产品": 0.75,
      "数码配件": 0.85
    },
    "price_model": {
      "mean": 750.0,
      "std": 420.5,
      "skewness": 0.15,
      "min": 150.0,
      "max": 2000.0,
      "median": 700.0,
      "count": 13,
      "_chi_fit_info": {
        "method": "trend_based_chi_fitting",
        "goodness_of_fit": 0.15,
        "expansion_factor": 1.2,
        "data_points": 5,
        "cumulative_clicks": 6
      }
    },
    "quality_model": {
      "mean": 4.45,
      "std": 0.48,
      "skewness": -0.18,
      "min": 3.8,
      "max": 4.8,
      "median": 4.5,
      "count": 13,
      "_chi_fit_info": {
        "method": "trend_based_chi_fitting",
        "goodness_of_fit": 0.12,
        "expansion_factor": 1.5,
        "data_points": 4,
        "cumulative_clicks": 6
      }
    },
    "last_updated": "2025-07-17T10:30:00.123456",
    "data_source": "cumulative_push_momentum",
    "latest_update": {
      "long_term_base": "long_term",
      "update_timestamp": "2025-07-17T10:30:00.123456",
      "note": "推送动量本质上仍是用户偏好分布，与长期短期动量是同类数据结构"
    }
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

---

## 3. 推荐服务接口

### 3.1 基于动量推荐商品
**接口**: `POST /api/v1/recommend-products`

**功能**: 使用已有动量对候选商品进行推荐排序

**输入**:
```json
{
  "user_id": 12345,
  "products": [
    {
      "product_id": "P001",
      "category": "电子产品",
      "price": 2999.0,
      "rating": 4.8,
      "stock": 50,
      "is_new": false,
      "brand": "Samsung",
      "name": "Galaxy S24"
    },
    {
      "product_id": "P002",
      "category": "数码配件",
      "price": 199.0,
      "rating": 4.5,
      "stock": 100,
      "is_new": true,
      "brand": "Apple",
      "name": "AirPods Pro"
    }
  ],
  "user_momentum": {
    "type": "push_momentum",
    "category_preferences": {
      "电子产品": 0.8,
      "数码配件": 0.9
    },
    "price_model": {
      "mean": 1200.0,
      "std": 600.0,
      "skewness": 0.1,
      "min": 199.0,
      "max": 2999.0,
      "median": 1000.0,
      "count": 12
    },
    "quality_model": {
      "mean": 4.5,
      "std": 0.4,
      "skewness": -0.2,
      "min": 4.0,
      "max": 4.9,
      "median": 4.6,
      "count": 12
    }
  },
  "top_k": 10,
  "include_scores": true
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "recommendations": [
      {
        "product_id": "P002",
        "category": "数码配件",
        "price": 199.0,
        "rating": 4.5,
        "brand": "Apple",
        "name": "AirPods Pro",
        "recommendation_score": 0.92,
        "rank": 1
      },
      {
        "product_id": "P001",
        "category": "电子产品",
        "price": 2999.0,
        "rating": 4.8,
        "brand": "Samsung",
        "name": "Galaxy S24",
        "recommendation_score": 0.85,
        "rank": 2
      }
    ],
    "total_count": 2,
    "momentum_type": "push_momentum",
    "algorithm_version": "2.0.0"
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 3.2 三动量系统推荐
**接口**: `POST /api/v1/three-momentum-recommendation`

**功能**: 完整的三动量系统，实时计算并更新推送动量，生成个性化推荐

**输入**:
```json
{
  "user_id": 12345,
  "products": [
    {
      "product_id": "P001",
      "category": "电子产品",
      "price": 2999.0,
      "rating": 4.8,
      "stock": 50,
      "is_new": false,
      "brand": "Samsung",
      "name": "Galaxy S24"
    }
  ],
  "long_term_momentum": {
    "type": "long_term_momentum",
    "category_preferences": {
      "电子产品": 0.8
    },
    "price_model": {
      "mean": 2000.0,
      "std": 800.0,
      "skewness": 0.3,
      "min": 800.0,
      "max": 3500.0,
      "median": 1800.0,
      "count": 10
    },
    "quality_model": {
      "mean": 4.3,
      "std": 0.5,
      "skewness": -0.1,
      "min": 3.8,
      "max": 4.9,
      "median": 4.4,
      "count": 10
    }
  },
  "current_push_momentum": {
    "type": "push_momentum",
    "cumulative_clicks": 5,
    "category_preferences": {
      "电子产品": 0.8,
      "数码配件": 0.6
    }
  },
  "clicked_product": {
    "product_id": "P004",
    "category": "数码配件",
    "price": 199.0,
    "rating": 4.5,
    "stock": 100,
    "is_new": true,
    "brand": "Apple",
    "name": "AirPods Pro"
  },
  "top_k": 10
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "recommendations": [
      {
        "product_id": "P001",
        "category": "电子产品",
        "price": 2999.0,
        "rating": 4.8,
        "brand": "Samsung",
        "name": "Galaxy S24",
        "rank": 1
      }
    ],
    "updated_momentum": {
      "type": "push_momentum",
      "user_id": 12345,
      "cumulative_clicks": 6,
      "category_preferences": {
        "电子产品": 0.8,
        "数码配件": 0.85
      },
      "last_updated": "2025-07-17T10:30:00"
    },
    "momentum_summary": {
      "type": "push_momentum",
      "cumulative_clicks": 6,
      "last_updated": "2025-07-17T10:30:00",
      "categories_count": 2
    },
    "algorithm_info": {
      "version": "2.0.0",
      "momentum_formula": "推送动量 += 长期动量 + 短期动量"
    }
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

---

## 4. 辅助工具接口

### 4.1 计算单个商品得分
**接口**: `POST /api/v1/calculate-product-score`

**功能**: 计算特定商品在给定用户动量下的推荐得分

**输入**:
```json
{
  "user_id": 12345,
  "product": {
    "product_id": "P001",
    "category": "电子产品",
    "price": 2999.0,
    "rating": 4.8,
    "stock": 50,
    "is_new": false,
    "brand": "Samsung",
    "name": "Galaxy S24"
  },
  "momentum": {
    "type": "push_momentum",
    "category_preferences": {
      "电子产品": 0.8
    },
    "price_model": {
      "mean": 2200.0,
      "std": 900.0,
      "skewness": 0.2,
      "min": 500.0,
      "max": 4000.0,
      "median": 2000.0,
      "count": 8
    },
    "quality_model": {
      "mean": 4.4,
      "std": 0.3,
      "skewness": -0.4,
      "min": 4.0,
      "max": 4.8,
      "median": 4.5,
      "count": 8
    }
  }
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "product_id": "P001",
    "recommendation_score": 0.85,
    "momentum_type": "push_momentum",
    "calculation_details": {
      "category_match": 0.8,
      "price_compatibility": "calculated",
      "quality_preference": "calculated"
    }
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 4.2 获取默认动量配置
**接口**: `GET /api/v1/get-default-momentum`

**功能**: 获取系统默认的长期动量配置，用于新用户或无历史数据用户

**输入**: 无

**输出**:
```json
{
  "success": true,
  "data": {
    "type": "long_term_momentum",
    "category_preferences": {
      "electronics": 0.2,
      "clothing": 0.2,
      "books": 0.15,
      "home": 0.15,
      "sports": 0.1,
      "food": 0.1,
      "beauty": 0.1
    },
    "price_model": {
      "mean": 100.0,
      "std": 50.0,
      "skewness": 0.0,
      "min": 0.0,
      "max": 0.0,
      "median": 0.0,
      "count": 0
    },
    "quality_model": {
      "mean": 4.0,
      "std": 1.0,
      "skewness": 0.0,
      "min": 0.0,
      "max": 0.0,
      "median": 0.0,
      "count": 0
    },
    "last_updated": "2025-07-17T10:30:00.123456",
    "is_default": true,
    "data_source": "config_default"
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 4.3 验证商品数据
**接口**: `POST /api/v1/validate-product-data`

**功能**: 验证商品数据的完整性和有效性

**输入**:
```json
{
  "products": [
    {
      "product_id": "P001",
      "category": "电子产品",
      "price": 2999.0,
      "rating": 4.8,
      "stock": 50,
      "is_new": false,
      "brand": "Samsung",
      "name": "Galaxy S24"
    },
    {
      "product_id": "",
      "category": "服装",
      "price": -100,
      "rating": 6.0,
      "stock": -5
    }
  ]
}
```

**输出**:
```json
{
  "success": true,
  "data": {
    "total_products": 2,
    "valid_products": 1,
    "invalid_products": 1,
    "validation_details": [
      {
        "product_id": "P001",
        "is_valid": true,
        "issues": []
      },
      {
        "product_id": "",
        "is_valid": false,
        "issues": ["数据验证失败"]
      }
    ]
  },
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

---

## 5. 卡方分布拟合算法说明

### 5.1 算法概述
本推荐系统使用**卡方分布线性变换**来处理用户偏好的偏态分布特征，解决了传统正态分布假设的局限性。

### 5.2 数学模型
```
X = scale * Chi²(df) + location
```
- **df**: 自由度，控制分布的偏度
- **scale**: 尺度参数，控制分布的方差  
- **location**: 位置参数，控制分布的均值位置

### 5.3 拟合步骤
1. **参数估计**: 使用矩估计法估算df、scale、location
2. **拟合检验**: 使用Kolmogorov-Smirnov检验评估拟合优度
3. **动态调整**: 根据p值计算expansion_factor调整更新强度

### 5.4 扩展因子机制
```
if p_value < 0.05:    expansion_factor = 2.0  # 分布变化大，强力调整
elif p_value < 0.1:   expansion_factor = 1.5  # 中等调整
elif p_value < 0.2:   expansion_factor = 1.2  # 轻微调整  
else:                 expansion_factor = 1.0  # 保持现状
```

### 5.5 趋势数据重建
当真实行为数据不足时，系统会基于长期动量和当前推送动量的差异，智能重建用户偏好变化轨迹：
```python
# 重建从长期偏好到当前偏好的演变路径
for i in range(cumulative_clicks - 1):
    progress = (i + 1) / cumulative_clicks
    interpolated_point = long_term_mean + (current_mean - long_term_mean) * progress
```

---

## 6. 数据模型说明

### 6.1 商品模型 (ProductModel)
```json
{
  "product_id": "string (必填)",
  "category": "string (必填)",
  "price": "float >= 0 (必填)",
  "rating": "float 0-5 (默认0.0)",
  "stock": "int >= 0 (默认0)",
  "is_new": "boolean (默认false)",
  "brand": "string (可选)",
  "name": "string (可选)"
}
```

### 6.2 用户行为模型 (UserBehaviorModel)
```json
{
  "orders": [
    {
      "order_id": "string",
      "product_id": "string (必填)",
      "category": "string (必填)",
      "price": "float >= 0 (必填)",
      "rating": "float 0-5 (必填)",
      "quantity": "int >= 1 (默认1)",
      "order_date": "string (日期)"
    }
  ],
  "cart": [
    {
      "product_id": "string (必填)",
      "category": "string (必填)",
      "price": "float >= 0 (必填)",
      "rating": "float 0-5 (必填)",
      "add_time": "string (日期)"
    }
  ],
  "returns": [
    {
      "product_id": "string (必填)",
      "category": "string (必填)",
      "price": "float >= 0 (必填)",
      "rating": "float 0-5 (必填)",
      "return_reason": "string",
      "return_date": "string (日期)"
    }
  ]
}
```

### 6.3 动量数据结构
```json
{
  "type": "long_term | short_term | push_momentum",
  "category_preferences": "object (类别偏好权重，Softmax归一化)",
  "price_model": {
    "mean": "float (价格均值)",
    "std": "float (价格标准差)", 
    "skewness": "float (价格偏度)",
    "min": "float (最小价格)",
    "max": "float (最大价格)",
    "median": "float (价格中位数)",
    "count": "int (数据点数量)",
    "_chi_fit_info": {
      "method": "string (拟合方法: short_term_distribution_chi_fitting | distribution_based_traditional_update)",
      "goodness_of_fit": "float (KS检验p值，0-1)",
      "expansion_factor": "float (动态调整因子，1.0-2.0)",
      "data_points": "int (用于拟合的数据点数量)",
      "cumulative_clicks": "int (累积点击数，仅推送动量)"
    }
  },
  "quality_model": {
    "mean": "float (质量均值)",
    "std": "float (质量标准差)",
    "skewness": "float (质量偏度)", 
    "min": "float (最小质量)",
    "max": "float (最大质量)",
    "median": "float (质量中位数)",
    "count": "int (数据点数量)",
    "_chi_fit_info": {
      "method": "string (拟合方法)",
      "goodness_of_fit": "float (KS检验p值)",
      "expansion_factor": "float (动态调整因子)",
      "data_points": "int (数据点数量)",
      "cumulative_clicks": "int (累积点击数)"
    }
  },
  "last_updated": "datetime (最后更新时间)",
  "cumulative_clicks": "int (累积点击数，仅推送动量)",
  "data_source": "string (数据来源)",
  "total_clicks": "int (总点击数，仅短期动量)",
  "total_strength": "float (总点击强度，仅短期动量)",
  "clicked_categories": "array (点击的类别列表，仅短期动量)",
  "latest_update": {
    "long_term_base": "string (长期动量类型)",
    "update_timestamp": "datetime (更新时间戳)",
    "note": "string (更新说明)"
  }
}
```

---

## 7. 错误响应格式

### 标准错误响应
```json
{
  "success": false,
  "error": "错误描述信息",
  "request_path": "/api/v1/xxx",
  "timestamp": "2025-07-17T10:30:00.123456"
}
```

### 常见错误码
- **400**: 请求参数错误
- **422**: 数据验证失败
- **500**: 服务器内部错误

---

## 8. 接口调用示例

### Python调用示例
```python
import requests
import json

# 计算长期动量
url = "http://localhost:8000/api/v1/calculate-long-term-momentum"
data = {
    "user_id": 12345,
    "user_behavior_data": {
        "orders": [{"order_id": "ORD001", "products": [...]}],
        "cart": [],
        "returns": []
    }
}
response = requests.post(url, json=data)
print(response.json())
```

### Java Spring调用示例
```java
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8000/api/v1/recommend-products";

RecommendationRequest request = new RecommendationRequest();
request.setUserId(12345);
request.setProducts(productList);
request.setTopK(10);

ResponseEntity<RecommendationResponse> response = 
    restTemplate.postForEntity(url, request, RecommendationResponse.class);
```

---

## 9. 性能和限制

### 请求限制
- **推荐数量**: top_k 参数范围 1-100
- **商品列表**: 建议单次请求不超过1000个商品
- **点击强度**: click_strength 范围 0.1-2.0

### 响应时间
- **动量计算**: < 100ms
- **商品推荐**: < 200ms (100个商品以内)
- **三动量推荐**: < 300ms

### 并发支持
- **最大并发**: 1000 QPS
- **推荐缓存**: 支持动量缓存以提升性能
