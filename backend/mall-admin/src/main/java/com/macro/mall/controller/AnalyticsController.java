package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.*;
import com.macro.mall.service.AnalyticsService;
import com.macro.mall.service.UserAnalyticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 销售分析Controller
 */
@Controller
@Api(tags = "AnalyticsController", description = "销售分析管理")
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @ApiOperation("获取销售数据分析")
    @RequestMapping(value = "/sales", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<SalesAnalyticsDto> getSalesAnalytics(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @ApiParam("时间粒度：day(按日)/week(按周)/month(按月)") @RequestParam(required = true) String type) {
        SalesAnalyticsDto result = analyticsService.getSalesAnalytics(startDate, endDate, type);
        return CommonResult.success(result);
    }

    @ApiOperation("获取销售渠道分布")
    @RequestMapping(value = "/sales/channel", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Map<String, Object>> getSalesChannelDistribution(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> result = analyticsService.getSalesChannelDistribution(startDate, endDate);
        return CommonResult.success(result);
    }

    @ApiOperation("获取用户地域分布")
    @RequestMapping(value = "/user/region", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Map<String, Object>> getUserRegionDistribution(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> result = analyticsService.getUserRegionDistribution(startDate, endDate);
        return CommonResult.success(result);
    }

    @ApiOperation("获取支付方式分布")
    @RequestMapping(value = "/payment/distribution", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PaymentDistributionDto>> getPaymentDistribution(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<PaymentDistributionDto> result = analyticsService.getPaymentDistribution(startDate, endDate);
        return CommonResult.success(result);
    }

    @ApiOperation("获取订单状态分布")
    @RequestMapping(value = "/order/status", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<OrderStatusDistributionDto>> getOrderStatusDistribution(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<OrderStatusDistributionDto> result = analyticsService.getOrderStatusDistribution(startDate, endDate);
        return CommonResult.success(result);
    }

    @ApiOperation("获取用户画像数据")
    @RequestMapping(value = "/user/portrait", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UserPortraitDto> getUserPortrait(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 验证日期范围
        if (startDate != null && endDate != null) {
            if (startDate.after(endDate)) {
                return CommonResult.failed("请求参数错误：开始日期不能晚于结束日期");
            }

            // 检查时间范围不能超过1年
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, 1);
            if (endDate.after(calendar.getTime())) {
                return CommonResult.failed("请求参数错误：时间范围不能超过1年");
            }
        }

        UserPortraitDto result = analyticsService.getUserPortrait(startDate, endDate);
        return CommonResult.success(result);
    }

    // ============== 新增用户分析相关接口 ==============

    @Autowired
    private UserAnalyticsService userAnalyticsService;

    @ApiOperation("获取用户概览数据")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UserOverviewDto> getUserOverview(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @ApiParam("时间粒度：day(按日)/week(按周)/month(按月)") @RequestParam(required = true) String type) {

        try {
            // 验证日期范围
            if (startDate.after(endDate)) {
                return CommonResult.failed("请求参数错误：开始日期不能晚于结束日期");
            }

            // 检查时间范围不能超过1年
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, 1);
            if (endDate.after(calendar.getTime())) {
                return CommonResult.failed("请求参数错误：时间范围不能超过1年");
            }

            UserOverviewDto result = userAnalyticsService.getUserOverview(startDate, endDate, type);
            return CommonResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("获取用户概览数据失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户留存率数据")
    @RequestMapping(value = "/user/retention", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UserRetentionDto>> getUserRetention(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        try {
            // 验证日期范围
            if (startDate.after(endDate)) {
                return CommonResult.failed("请求参数错误：开始日期不能晚于结束日期");
            }

            List<UserRetentionDto> result = userAnalyticsService.getUserRetention(startDate, endDate);
            return CommonResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("获取用户留存率数据失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户活跃度分布数据")
    @RequestMapping(value = "/user/activity", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UserActivityDistributionDto>> getUserActivityDistribution(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        try {
            // 验证日期范围
            if (startDate.after(endDate)) {
                return CommonResult.failed("请求参数错误：开始日期不能晚于结束日期");
            }

            List<UserActivityDistributionDto> result = userAnalyticsService.getUserActivityDistribution(startDate, endDate);
            return CommonResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("获取用户活跃度分布数据失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户行为分析数据")
    @RequestMapping(value = "/behavior", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UserBehaviorDto> getUserBehavior(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        try {
            // 验证日期范围
            if (startDate.after(endDate)) {
                return CommonResult.failed("请求参数错误：开始日期不能晚于结束日期");
            }

            UserBehaviorDto result = userAnalyticsService.getUserBehavior(startDate, endDate);
            return CommonResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("获取用户行为分析数据失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取用户访问时段分布数据")
    @RequestMapping(value = "/user/visit/time", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UserVisitTimeDistributionDto>> getUserVisitTimeDistribution(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        try {
            // 如果没有提供日期参数，使用默认的最近30天
            if (startDate == null || endDate == null) {
                Calendar calendar = Calendar.getInstance();
                endDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, -30);
                startDate = calendar.getTime();
            }

            // 验证日期范围
            if (startDate.after(endDate)) {
                return CommonResult.failed("请求参数错误：开始日期不能晚于结束日期");
            }

            List<UserVisitTimeDistributionDto> result = userAnalyticsService.getUserVisitTimeDistribution(startDate, endDate);
            return CommonResult.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("获取用户访问时段分布数据失败：" + e.getMessage());
        }
    }
}
