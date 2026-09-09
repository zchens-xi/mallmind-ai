package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.*;
import com.macro.mall.service.ProductAnalyticsService;
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

/**
 * 商品分析Controller
 */
@Controller
@Api(tags = "ProductAnalyticsController", description = "商品分析管理")
@RequestMapping("/analytics/product")
public class ProductAnalyticsController {

    @Autowired
    private ProductAnalyticsService productAnalyticsService;

    @ApiOperation("获取商品概览数据")
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<ProductOverviewDto> getProductOverview(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // 参数验证
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            return CommonResult.failed("开始日期不能大于结束日期");
        }

        // 检查时间范围不能超过1年
        if (startDate != null && endDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, 1);
            if (endDate.after(calendar.getTime())) {
                return CommonResult.failed("时间范围不能超过1年");
            }
        }

        try {
            ProductOverviewDto result = productAnalyticsService.getProductOverview(startDate, endDate);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取商品概览数据失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取商品销售排行")
    @RequestMapping(value = "/sales-rank", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<ProductSalesRankDto>> getProductSalesRank(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @ApiParam("返回条数，默认10，最大100") @RequestParam(required = false, defaultValue = "10") Integer limit) {

        // 参数验证
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            return CommonResult.failed("开始日期不能大于结束日期");
        }

        if (limit != null && (limit <= 0 || limit > 100)) {
            return CommonResult.failed("limit参数范围应在1-100之间");
        }

        try {
            List<ProductSalesRankDto> result = productAnalyticsService.getProductSalesRank(startDate, endDate, limit);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取商品销售排行失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取商品浏览排行")
    @RequestMapping(value = "/view-rank", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<ProductViewRankDto>> getProductViewRank(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @ApiParam("返回条数，默认10，最大100") @RequestParam(required = false, defaultValue = "10") Integer limit) {

        // 参数验证
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            return CommonResult.failed("开始日期不能大于结束日期");
        }

        if (limit != null && (limit <= 0 || limit > 100)) {
            return CommonResult.failed("limit参数范围应在1-100之间");
        }

        try {
            List<ProductViewRankDto> result = productAnalyticsService.getProductViewRank(startDate, endDate, limit);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取商品浏览排行失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取商品转化率排行")
    @RequestMapping(value = "/conversion-rank", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<ProductConversionRankDto>> getProductConversionRank(
            @ApiParam("开始日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期，格式：YYYY-MM-DD") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @ApiParam("返回条数，默认10，最大100") @RequestParam(required = false, defaultValue = "10") Integer limit) {

        // 参数验证
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            return CommonResult.failed("开始日期不能大于结束日期");
        }

        if (limit != null && (limit <= 0 || limit > 100)) {
            return CommonResult.failed("limit参数范围应在1-100之间");
        }

        try {
            List<ProductConversionRankDto> result = productAnalyticsService.getProductConversionRank(startDate, endDate, limit);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取商品转化率排行失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取商品价格区间分布")
    @RequestMapping(value = "/price-distribution", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<ProductPriceDistributionDto>> getProductPriceDistribution() {
        try {
            List<ProductPriceDistributionDto> result = productAnalyticsService.getProductPriceDistribution();
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取商品价格区间分布失败：" + e.getMessage());
        }
    }

    @ApiOperation("获取商品库存分布")
    @RequestMapping(value = "/stock-distribution", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<ProductStockDistributionDto>> getProductStockDistribution() {
        try {
            List<ProductStockDistributionDto> result = productAnalyticsService.getProductStockDistribution();
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed("获取商品库存分布失败：" + e.getMessage());
        }
    }
}
