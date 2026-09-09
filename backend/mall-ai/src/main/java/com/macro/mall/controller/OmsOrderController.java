package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.OmsOrderQueryParam;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.service.OmsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "OmsOrderController", description = "订单管理")
@RestController
@RequestMapping("/order")
public class OmsOrderController {

    @Autowired
    private OmsOrderService orderService;

    @ApiOperation("条件查询订单")
    @GetMapping("/list")
    public CommonResult<List<OmsOrder>> list(OmsOrderQueryParam queryParam) {
        List<OmsOrder> orders = orderService.list(queryParam);
        return CommonResult.success(orders);
    }

    @ApiOperation("根据ID获取订单")
    @GetMapping("/{id}")
    public CommonResult<OmsOrder> getById(@PathVariable Long id) {
        OmsOrder order = orderService.getById(id);
        return CommonResult.success(order);
    }

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody OmsOrder order) {
        int count = orderService.create(order);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("更新订单")
    @PutMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsOrder order) {
        order.setId(id);
        int count = orderService.update(order);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = orderService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}