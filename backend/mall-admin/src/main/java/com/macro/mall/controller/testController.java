//package com.macro.mall.controller;
//
//import com.macro.mall.ai.model.mongo.UserBehavior;
//import com.macro.mall.ai.repository.mongo.UserBehaviorRepository;
//import com.macro.mall.common.api.CommonResult;
//import com.macro.mall.model.CmsPrefrenceArea;
//import com.macro.mall.service.CmsPrefrenceAreaService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
///**
// * 商品优选管理Controller
// * Created by macro on 2018/6/1.
// */
//@Controller
//@Api(tags = "testDB")
//@Tag(name = "testDB", description = "测试")
//@RequestMapping("/testDataBase")
//public class testController {
//    @Autowired
//    private CmsPrefrenceAreaService prefrenceAreaService;
//
//    @Autowired
//    private UserBehaviorRepository userBehaviorRepository;
//
//    @ApiOperation("获取所有商品优选")
//    @RequestMapping(value = "/listAll-mysql", method = RequestMethod.GET)
//    @ResponseBody
//    public CommonResult<List<CmsPrefrenceArea>> listAll() {
//        List<CmsPrefrenceArea> prefrenceAreaList = prefrenceAreaService.listAll();
//        return CommonResult.success(prefrenceAreaList);
//    }
//
//    // MongoDB测试接口
//
//    @ApiOperation("添加用户行为数据")
//    @RequestMapping(value = "/mongo/add", method = RequestMethod.POST)
//    @ResponseBody
//    public CommonResult<UserBehavior> addUserBehavior(@RequestBody UserBehavior userBehavior) {
//        userBehavior.setTimestamp(LocalDateTime.now());
//        UserBehavior result = userBehaviorRepository.save(userBehavior);
//        return CommonResult.success(result);
//    }
//
//    @ApiOperation("获取用户行为列表")
//    @RequestMapping(value = "/mongo/list", method = RequestMethod.GET)
//    @ResponseBody
//    public CommonResult<List<UserBehavior>> listUserBehaviors() {
//        List<UserBehavior> list = userBehaviorRepository.findAll();
//        return CommonResult.success(list);
//    }
//
//    @ApiOperation("根据ID获取用户行为")
//    @RequestMapping(value = "/mongo/{id}", method = RequestMethod.GET)
//    @ResponseBody
//    public CommonResult<UserBehavior> getUserBehavior(@PathVariable String id) {
//        Optional<UserBehavior> userBehavior = userBehaviorRepository.findById(id);
//        return userBehavior.map(CommonResult::success).orElse(CommonResult.failed("未找到指定记录"));
//    }
//
//    @ApiOperation("修改用户行为数据")
//    @RequestMapping(value = "/mongo/update", method = RequestMethod.PUT)
//    @ResponseBody
//    public CommonResult<UserBehavior> updateUserBehavior(@RequestBody UserBehavior userBehavior) {
//        if (userBehavior.getId() == null) {
//            return CommonResult.failed("ID不能为空");
//        }
//        boolean exists = userBehaviorRepository.existsById(userBehavior.getId());
//        if (!exists) {
//            return CommonResult.failed("要更新的记录不存在");
//        }
//        userBehavior.setTimestamp(LocalDateTime.now()); // 更新时间戳
//        UserBehavior updatedBehavior = userBehaviorRepository.save(userBehavior);
//        return CommonResult.success(updatedBehavior);
//    }
//
//    @ApiOperation("删除用户行为数据")
//    @RequestMapping(value = "/mongo/{id}", method = RequestMethod.DELETE)
//    @ResponseBody
//    public CommonResult<String> deleteUserBehavior(@PathVariable String id) {
//        userBehaviorRepository.deleteById(id);
//        return CommonResult.success("删除成功");
//    }
//
////    @ApiOperation("获取所有商品优选")
////    @RequestMapping(value = "/listAll-mysql", method = RequestMethod.GET)
////    @ResponseBody
////    public CommonResult<List<CmsPrefrenceArea>> listAll() {
////        List<CmsPrefrenceArea> prefrenceAreaList = prefrenceAreaService.listAll();
////        return CommonResult.success(prefrenceAreaList);
////    }
//}