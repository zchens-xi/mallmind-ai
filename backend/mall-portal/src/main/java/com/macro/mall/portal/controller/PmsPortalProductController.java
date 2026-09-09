package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.portal.domain.PmsPortalProductDetail;
import com.macro.mall.portal.domain.PmsProductCategoryNode;
import com.macro.mall.portal.service.PmsPortalProductService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.PushService;
import com.macro.mall.model.UmsMember;
import com.macro.mall.recommendation.dto.ProductInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台商品管理Controller
 * Created by macro on 2020/4/6.
 */
@Controller
@Api(tags = "PmsPortalProductController")
@Tag(name = "PmsPortalProductController", description = "前台商品管理")
@RequestMapping("/product")
public class PmsPortalProductController {

    @Autowired
    private PmsPortalProductService portalProductService;
    
    @Autowired
    private UmsMemberService memberService;
    
    @Autowired
    private PushService pushService;

    @ApiOperation(value = "综合搜索、筛选、排序")
    @ApiImplicitParam(name = "sort", value = "排序字段:0->按相关度；1->按新品；2->按销量；3->价格从低到高；4->价格从高到低",
            defaultValue = "0", allowableValues = "0,1,2,3,4", paramType = "query", dataType = "integer")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<PmsProduct>> search(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Long brandId,
                                                       @RequestParam(required = false) Long productCategoryId,
                                                       @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                       @RequestParam(required = false, defaultValue = "5") Integer pageSize,
                                                       @RequestParam(required = false, defaultValue = "0") Integer sort) {
        List<PmsProduct> productList = portalProductService.search(keyword, brandId, productCategoryId, pageNum, pageSize, sort);
        return CommonResult.success(CommonPage.restPage(productList));
    }

    @ApiOperation("以树形结构获取所有商品分类")
    @RequestMapping(value = "/categoryTreeList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProductCategoryNode>> categoryTreeList() {
        List<PmsProductCategoryNode> list = portalProductService.categoryTreeList();
        return CommonResult.success(list);
    }

    @ApiOperation("获取前台商品详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<PmsPortalProductDetail> detail(@PathVariable Long id) {
        PmsPortalProductDetail productDetail = portalProductService.detail(id);
        return CommonResult.success(productDetail);
    }

//    @ApiOperation("获取前台商品详情")
//    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
//    @ResponseBody
//    public CommonResult<PmsPortalProductDetail> detail(@PathVariable Long id) {
//        PmsPortalProductDetail productDetail = portalProductService.detail(id);
//
//        // 添加触发点：更新短期动量和推送动量
//        try {
//            UmsMember currentMember = memberService.getCurrentMember();
//            if (currentMember != null) {
//                ProductInfo productInfo = convertToProductInfo(productDetail.getProduct());
//                // 更新短期动量
//                pushService.renewShortTermMomentum(currentMember.getId(), productInfo, 1.0);
//                // 同时更新推送动量
//                pushService.renewPushMomentum(currentMember.getId(), productInfo, 1.0);
//            }
//        } catch (Exception e) {
//            // 记录异常但不影响正常返回
//            System.err.println("更新用户动量失败: " + e.getMessage());
//        }
//
//        return CommonResult.success(productDetail);
//    }
//
    /**
     * 将商品信息转换为ProductInfo对象
     */
    private ProductInfo convertToProductInfo(PmsProduct product) {
        ProductInfo info = new ProductInfo();
        info.setProductId(String.valueOf(product.getId()));
        info.setName(product.getName());
        info.setPrice(product.getPrice().doubleValue());
        info.setCategory(product.getProductCategoryName());
        info.setBrand(product.getBrandName());
        info.setRating(product.getRecommandStatus().doubleValue());
        info.setStock(product.getStock());
        info.setIsNew(product.getNewStatus() == 1);
        return info;
    }
}
