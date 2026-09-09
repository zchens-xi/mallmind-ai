package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMember;
import com.macro.mall.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "UmsMemberController", description = "会员管理")
@RestController
@RequestMapping("/member")
public class UmsMemberController {

    @Autowired
    private UmsMemberService memberService;

    @ApiOperation("获取所有会员")
    @GetMapping("/list")
    public CommonResult<List<UmsMember>> listAll() {
        List<UmsMember> members = memberService.listAll();
        return CommonResult.success(members);
    }

    @ApiOperation("根据ID获取会员")
    @GetMapping("/{id}")
    public CommonResult<UmsMember> getById(@PathVariable Long id) {
        UmsMember member = memberService.getById(id);
        return CommonResult.success(member);
    }

    @ApiOperation("创建会员")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsMember member) {
        int count = memberService.create(member);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("更新会员信息")
    @PutMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsMember member) {
        member.setId(id);
        int count = memberService.update(member);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除会员")
    @DeleteMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = memberService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}