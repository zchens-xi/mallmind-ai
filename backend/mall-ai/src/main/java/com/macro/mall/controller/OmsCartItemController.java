package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.service.OmsCartItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "OmsCartItemController", description = "购物车管理")
@RestController
@RequestMapping("/cart")
public class OmsCartItemController {

    @Autowired
    private OmsCartItemService cartItemService;

    @ApiOperation("获取会员购物车列表")
    @GetMapping("/list/{memberId}")
    public CommonResult<List<OmsCartItem>> listByMemberId(@PathVariable Long memberId) {
        List<OmsCartItem> cartItems = cartItemService.listByMemberId(memberId);
        return CommonResult.success(cartItems);
    }

    @ApiOperation("根据ID获取购物车项")
    @GetMapping("/{id}")
    public CommonResult<OmsCartItem> getById(@PathVariable Long id) {
        OmsCartItem cartItem = cartItemService.getById(id);
        return CommonResult.success(cartItem);
    }

    @ApiOperation("添加到购物车")
    @PostMapping("/add")
    public CommonResult<Integer> add(@RequestBody OmsCartItem cartItem) {
        int count = cartItemService.create(cartItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("更新购物车项")
    @PutMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody OmsCartItem cartItem) {
        cartItem.setId(id);
        int count = cartItemService.update(cartItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除购物车项")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = cartItemService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}