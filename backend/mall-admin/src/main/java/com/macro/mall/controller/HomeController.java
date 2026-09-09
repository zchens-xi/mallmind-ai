package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.service.HomeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 首页内容管理Controller
 */
@Controller
@Api(tags = "HomeController", description = "首页内容管理")
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private HomeService homeService;

    @ApiOperation("获取首页数据")
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Map<String, Object>> getHomeData(
            @ApiParam(value = "时间范围：week-近一周，month-近一月，year-近一年", defaultValue = "week")
            @RequestParam(value = "timeRange", defaultValue = "week") String timeRange) {
        Map<String, Object> data = homeService.getHomeData(timeRange);
        return CommonResult.success(data);
    }

    @ApiOperation("获取转化漏斗数据")
    @RequestMapping(value = "/funnel", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<Map<String, Object>>> getFunnelData() {
        List<Map<String, Object>> data = homeService.getFunnelData();
        return CommonResult.success(data);
    }
} 