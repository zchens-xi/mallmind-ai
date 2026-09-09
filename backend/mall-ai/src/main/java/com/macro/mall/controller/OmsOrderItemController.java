package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.service.OmsOrderItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "OmsOrderItemController", description = "订单项管理")
@RestController
@RequestMapping("/orderItem")
public class OmsOrderItemController {

    @Autowired
    private OmsOrderItemService orderItemService;

    @ApiOperation("根据订单ID获取订单项")
    @GetMapping("/list/{orderId}")
    public CommonResult<List<OmsOrderItem>> listByOrderId(@PathVariable Long orderId) {
        List<OmsOrderItem> orderItems = orderItemService.listByOrderId(orderId);
        return CommonResult.success(orderItems);
    }

    @ApiOperation("根据ID获取订单项")
    @GetMapping("/{id}")
    public CommonResult<OmsOrderItem> getById(@PathVariable Long id) {
        OmsOrderItem orderItem = orderItemService.getById(id);
        return CommonResult.success(orderItem);
    }

    @ApiOperation("创建订单项")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody OmsOrderItem orderItem) {
        int count = orderItemService.create(orderItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("更新订单项")
    @PutMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsOrderItem orderItem) {
        orderItem.setId(id);
        int count = orderItemService.update(orderItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除订单项")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = orderItemService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}