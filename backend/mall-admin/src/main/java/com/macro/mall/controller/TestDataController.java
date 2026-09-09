package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 测试数据控制器 - 用于创建MongoDB测试数据
 */
@Api(tags = "TestDataController", description = "测试数据管理")
@RestController
@RequestMapping("/test")
public class TestDataController {

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @ApiOperation("创建商品浏览历史测试数据")
    @PostMapping("/createViewHistory")
    public CommonResult<String> createViewHistoryTestData() {
        if (mongoTemplate == null) {
            return CommonResult.failed("MongoDB未连接");
        }

        try {
            // 清空现有数据
            mongoTemplate.dropCollection("memberReadHistory");

            // 创建测试数据
            List<Map<String, Object>> testData = new ArrayList<>();
            Random random = new Random();
            
            // 商品ID和名称对应关系（使用实际存在的商品）
            Map<Long, String> products = new HashMap<>();
            products.put(39L, "小米 Xiaomi Book Pro 14 2022 锐龙版 2.8K超清大师屏 高端轻薄笔记本电脑");
            products.put(40L, "小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充");
            products.put(26L, "华为 HUAWEI P20");
            products.put(111L, "闪迪 9公斤波轮洗衣机");
            products.put(110L, "松下 87英寸电视");
            products.put(102L, "松下 家用按摩器");
            products.put(104L, "惠普 暗影精灵 21");
            products.put(113L, "小米 1254");
            products.put(103L, "联想 ThinkPad 13 Pro");
            products.put(116L, "希捷 13升燃气热水器");

            // 生成最近30天的测试数据
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date startDate = calendar.getTime();

            // 为每个商品生成浏览记录
            for (Map.Entry<Long, String> product : products.entrySet()) {
                Long productId = product.getKey();
                String productName = product.getValue();

                // 每个商品生成10-50条浏览记录
                int viewCount = 10 + random.nextInt(40);
                
                for (int i = 0; i < viewCount; i++) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("memberId", (long)(1 + random.nextInt(10))); // 会员ID 1-10
                    record.put("memberNickname", "testUser" + (1 + random.nextInt(10)));
                    record.put("memberIcon", "https://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/icon/github_icon_03.png");
                    record.put("productId", productId);
                    record.put("productName", productName);
                    record.put("productPic", "https://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/test.jpg");
                    record.put("productSubTitle", "测试商品副标题");
                    record.put("productPrice", "¥" + (100 + random.nextInt(5000)));
                    
                    // 生成随机时间（最近30天内）
                    long timeRange = endDate.getTime() - startDate.getTime();
                    long randomTime = startDate.getTime() + (long)(random.nextDouble() * timeRange);
                    record.put("createTime", new Date(randomTime));

                    testData.add(record);
                }
            }

            // 插入测试数据
            mongoTemplate.insert(testData, "memberReadHistory");

            return CommonResult.success("成功创建 " + testData.size() + " 条测试数据");

        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("创建测试数据失败: " + e.getMessage());
        }
    }

    @ApiOperation("清理测试数据")
    @PostMapping("/clearViewHistory")
    public CommonResult<String> clearViewHistoryTestData() {
        if (mongoTemplate == null) {
            return CommonResult.failed("MongoDB未连接");
        }

        try {
            mongoTemplate.dropCollection("memberReadHistory");
            return CommonResult.success("测试数据已清理");
        } catch (Exception e) {
            return CommonResult.failed("清理测试数据失败: " + e.getMessage());
        }
    }
}
