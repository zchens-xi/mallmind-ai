package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.UserBehaviorAnalysisDTO;
import com.macro.mall.service.UserAnalyticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户行为分析控制器
 * 单独处理 /analytics/behavior 路径
 */
@RestController
@Api(tags = "BehaviorAnalyticsController", description = "用户行为分析管理")
@RequestMapping("/user-behavior")
public class BehaviorAnalyticsController {

    @Autowired
    private UserAnalyticsService userAnalyticsService;

    @ApiOperation("获取用户行为分析数据")
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UserBehaviorAnalysisDTO> getUserBehavior(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        try {
            // 转换日期格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            
            // 将结束日期设置为当天的23:59:59
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            end = calendar.getTime();
            
            // 从服务层获取实际数据
            UserBehaviorAnalysisDTO result = userAnalyticsService.getUserBehavior(startDate, endDate);
            
            // 数据合理性检查
            Long totalUsers = userAnalyticsService.getTotalUserCount();
            if (result.getVisitors() > totalUsers) {
                result.setDataQualityWarning("警告：显示的访客数超过了系统注册用户总数，数据可能不准确");
            }
            
            return CommonResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("获取用户行为分析数据失败: " + e.getMessage());
        }
    }
}
