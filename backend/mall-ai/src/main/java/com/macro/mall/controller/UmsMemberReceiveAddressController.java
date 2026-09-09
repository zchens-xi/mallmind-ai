package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMemberReceiveAddress;
import com.macro.mall.service.UmsMemberReceiveAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "UmsMemberReceiveAddressController", description = "会员收货地址管理")
@RestController
@RequestMapping("/address")
public class UmsMemberReceiveAddressController {

    @Autowired
    private UmsMemberReceiveAddressService addressService;

    @ApiOperation("获取会员所有收货地址")
    @GetMapping("/list/{memberId}")
    public CommonResult<List<UmsMemberReceiveAddress>> listByMemberId(@PathVariable Long memberId) {
        List<UmsMemberReceiveAddress> addresses = addressService.listByMemberId(memberId);
        return CommonResult.success(addresses);
    }

    @ApiOperation("根据ID获取收货地址")
    @GetMapping("/{id}")
    public CommonResult<UmsMemberReceiveAddress> getById(@PathVariable Long id) {
        UmsMemberReceiveAddress address = addressService.getById(id);
        return CommonResult.success(address);
    }

    @ApiOperation("创建收货地址")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsMemberReceiveAddress address) {
        int count = addressService.create(address);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("更新收货地址")
    @PutMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsMemberReceiveAddress address) {
        address.setId(id);
        int count = addressService.update(address);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除收货地址")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = addressService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}