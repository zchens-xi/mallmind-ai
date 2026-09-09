package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberReadHistory;
import com.macro.mall.portal.repository.MemberReadHistoryRepository;
import com.macro.mall.portal.service.MemberReadHistoryService;
import com.macro.mall.portal.service.PushService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.recommendation.dto.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会员浏览记录管理Service实现类
 * Created by macro on 2018/8/3.
 */
@Service
public class MemberReadHistoryServiceImpl implements MemberReadHistoryService {

    @Value("${mongo.insert.sqlEnable}")
    private Boolean sqlEnable;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private MemberReadHistoryRepository memberReadHistoryRepository;
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private PushService pushService;
    
    @Override
    public int create(MemberReadHistory memberReadHistory) {
        if (memberReadHistory.getProductId() == null) {
            return 0;
        }
        UmsMember member = memberService.getCurrentMember();
        memberReadHistory.setMemberId(member.getId());
        memberReadHistory.setMemberNickname(member.getNickname());
        memberReadHistory.setMemberIcon(member.getIcon());
        memberReadHistory.setId(null);
        memberReadHistory.setCreateTime(new Date());
        
        PmsProduct product = null;
        if (sqlEnable) {
            product = productMapper.selectByPrimaryKey(memberReadHistory.getProductId());
            if (product == null || product.getDeleteStatus() == 1) {
                return 0;
            }
            memberReadHistory.setProductName(product.getName());
            memberReadHistory.setProductSubTitle(product.getSubTitle());
            memberReadHistory.setProductPrice(product.getPrice() + "");
            memberReadHistory.setProductPic(product.getPic());
        }
        
        // 保存浏览记录
        memberReadHistoryRepository.save(memberReadHistory);
        
        // 添加触发点：更新短期动量，浏览行为点击强度设为0.5（弱于直接点击）
        try {
            if (product != null) {
                ProductInfo productInfo = convertToProductInfo(product);
                // 更新短期动量
                pushService.renewShortTermMomentum(member.getId(), productInfo, 0.5);
            }
        } catch (Exception e) {
            // 记录异常但不影响正常返回
            System.err.println("更新用户浏览动量失败: " + e.getMessage());
        }
        
        return 1;
    }

    @Override
    public int delete(List<String> ids) {
        List<MemberReadHistory> deleteList = new ArrayList<>();
        for(String id:ids){
            MemberReadHistory memberReadHistory = new MemberReadHistory();
            memberReadHistory.setId(id);
            deleteList.add(memberReadHistory);
        }
        memberReadHistoryRepository.deleteAll(deleteList);
        return ids.size();
    }

    @Override
    public Page<MemberReadHistory> list(Integer pageNum, Integer pageSize) {
        UmsMember member = memberService.getCurrentMember();
        Pageable pageable = PageRequest.of(pageNum-1, pageSize);
        return memberReadHistoryRepository.findByMemberIdOrderByCreateTimeDesc(member.getId(),pageable);
    }

    @Override
    public void clear() {
        UmsMember member = memberService.getCurrentMember();
        memberReadHistoryRepository.deleteAllByMemberId(member.getId());
    }
    
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
