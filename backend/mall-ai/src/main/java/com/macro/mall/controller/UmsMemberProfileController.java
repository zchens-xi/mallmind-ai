package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMemberProfile;
import com.macro.mall.service.UmsMemberProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "UmsMemberProfileController", description = "会员资料管理")
@RestController
@RequestMapping("/profile")
public class UmsMemberProfileController {

    @Autowired
    private UmsMemberProfileService profileService;

    @ApiOperation("获取所有会员资料")
    @GetMapping("/list")
    public CommonResult<List<UmsMemberProfile>> listAll() {
        List<UmsMemberProfile> profiles = profileService.listAll();
        return CommonResult.success(profiles);
    }

    @ApiOperation("根据会员ID获取资料")
    @GetMapping("/{memberId}")
    public CommonResult<UmsMemberProfile> getByMemberId(@PathVariable Long memberId) {
        UmsMemberProfile profile = profileService.getByMemberId(memberId);
        return CommonResult.success(profile);
    }

    @ApiOperation("创建会员资料")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody UmsMemberProfile profile) {
        int count = profileService.create(profile);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("更新会员资料")
    @PutMapping("/update/{memberId}")
    public CommonResult<Integer> update(@PathVariable Long memberId, @RequestBody UmsMemberProfile profile) {
        profile.setMemberId(memberId);
        int count = profileService.update(profile);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除会员资料")
    @DeleteMapping("/delete/{memberId}")
    public CommonResult<Integer> delete(@PathVariable Long memberId) {
        int count = profileService.delete(memberId);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}