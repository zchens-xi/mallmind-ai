package com.macro.mall.portal.service.impl;

import com.macro.mall.ai.model.mongo.ConversationHistory;
import com.macro.mall.ai.service.DeepSeekService;
import com.macro.mall.ai.service.impl.AICustomerServiceImpl;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.MemberDetails;
import com.macro.mall.portal.domain.PmsPortalProductDetail;
import com.macro.mall.portal.service.AiService;
import com.macro.mall.portal.service.PmsPortalProductService;
import com.macro.mall.portal.service.UmsMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private UmsMemberService memberService;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private AICustomerServiceImpl aiCustomerService;

    @Override
    public Long getCurrentUserId() {
        try {
            UmsMember currentMember = memberService.getCurrentMember();
            if (currentMember != null) {
                return currentMember.getId();
            }
        } catch (Exception e) {
            log.error("获取当前用户失败: {}", e.getMessage());
        }

        // 备用方案：从SecurityContext获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof MemberDetails) {
                MemberDetails memberDetails = (MemberDetails) principal;
                if (memberDetails.getUmsMember() != null) {
                    return memberDetails.getUmsMember().getId();
                }
            }
        }
        return null;
    }

    @Override
    public String askDeepSeek(String question) throws Exception {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("问题内容不能为空");
        }

        String answer = deepSeekService.askQuestion(question);
        if (answer == null || answer.isEmpty()) {
            throw new Exception("AI 未返回有效回答");
        }

        Long userId = getCurrentUserId();
        if (userId == null) {
            // 在实际应用中，这里可能需要抛出特定的异常，如未授权访问
            throw new IllegalStateException("用户未登录");
        }

        String sessionId = "sess_" + System.currentTimeMillis();

        ConversationHistory history = aiCustomerService.buildConversationHistory(userId, sessionId, question, answer);
        aiCustomerService.saveConversation(history);

        return answer;
    }

    @Override
    public String askCustomer(String question, Long productId) throws Exception{
        String prompt = prompt(question, productId);

        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("问题内容不能为空");
        }

        String answer = deepSeekService.askQuestion(prompt);
        if (answer == null || answer.isEmpty()) {
            throw new Exception("AI 未返回有效回答");
        }

        Long userId = getCurrentUserId();
        if (userId == null) {
            // 在实际应用中，这里可能需要抛出特定的异常，如未授权访问
            throw new IllegalStateException("用户未登录");
        }

        String sessionId = "sess_" + System.currentTimeMillis();

        // 使用带productId参数的方法构建对话历史
        ConversationHistory history = aiCustomerService.buildConversationHistory(userId, sessionId, question, answer, productId);
        aiCustomerService.saveConversation(history);

        return answer;
    }


    @Override
    public List<ConversationHistory> hello() {
        ConversationHistory history = new ConversationHistory();
        history.setAiResponse("你好，我是mall商城的AI客服助手，很高兴为您服务！");

        List<ConversationHistory> result = new ArrayList<>();
        result.add(history);
        return result;
    }

    @Autowired
    private PmsPortalProductService productDetailService;

    @Override
    public String getProductInfo(Long productId) {
        PmsPortalProductDetail detail = productDetailService.detail(productId);
        if (detail == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("商品名称：").append(detail.getProduct().getName()).append("\n");
        sb.append("商品品牌：").append(detail.getBrand().getName()).append("\n");
        sb.append("商品描述：").append(detail.getProduct().getDescription()).append("\n");
        sb.append("价格：").append(detail.getProduct().getPrice()).append("\n");

        if (detail.getProductAttributeList() != null && !detail.getProductAttributeList().isEmpty()) {
            sb.append("商品属性：\n");
            detail.getProductAttributeList().forEach(attr -> {
                detail.getProductAttributeValueList().stream()
                        .filter(attrValue -> attrValue.getProductAttributeId().equals(attr.getId()))
                        .findFirst()
                        .ifPresent(attrValue -> sb.append("- ").append(attr.getName()).append(": ").append(attrValue.getValue()).append("\n"));
            });
        }

        if (detail.getSkuStockList() != null && !detail.getSkuStockList().isEmpty()) {
            sb.append("库存信息：\n");
            detail.getSkuStockList().forEach(sku -> {
                sb.append("- ").append(sku.getSpData()).append("，价格：").append(sku.getPrice()).append("，库存：").append(sku.getStock()).append("\n");
            });
        }

        if (detail.getProductLadderList() != null && !detail.getProductLadderList().isEmpty()) {
            sb.append("阶梯价格优惠：\n");
            detail.getProductLadderList().forEach(ladder -> {
                sb.append("- 购买").append(ladder.getCount()).append("件及以上，每件可享受").append(ladder.getDiscount()).append("折扣\n");
            });
        }

        if (detail.getProductFullReductionList() != null && !detail.getProductFullReductionList().isEmpty()) {
            sb.append("满减优惠：\n");
            detail.getProductFullReductionList().forEach(fullReduction -> {
                sb.append("- 满").append(fullReduction.getFullPrice()).append("元减").append(fullReduction.getReducePrice()).append("元\n");
            });
        }

        if (detail.getCouponList() != null && !detail.getCouponList().isEmpty()) {
            sb.append("可用优惠券：\n");
            detail.getCouponList().forEach(coupon -> {
                sb.append("- ").append(coupon.getName()).append("\n");
            });
        }

        return sb.toString();
    }


    @Override
    public String prompt(String question, Long productId) {
        String promptTemplate = "[最高指令]你是mall商城的ai客服，请你严格根据以下商品信息和历史对话回答用户的问题：\n" +
                "商品信息：\n%s\n" +
                "历史对话：\n%s\n" +
                "问题：%s\n";
//                "[最高指令]：当用户提问与商品不相关时，请回答“抱歉，我无法回答与商品无关的问题。”\n"

        List<ConversationHistory> historyList = aiCustomerService.getHistoryByProductId(getCurrentUserId(), productId);
        StringBuilder historyStr = new StringBuilder();
        for (ConversationHistory history : historyList) {
            historyStr.append("用户：").append(history.getUserMessage()).append("\n");
            historyStr.append("AI：").append(history.getAiResponse()).append("\n");
        }

        String productInfo = getProductInfo(productId);

        return String.format(promptTemplate, productInfo, historyStr.toString(), question);
    }

    @Override
    public List<ConversationHistory> getHistory(Long productId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
        List<ConversationHistory> history = aiCustomerService.getHistoryByProductId(userId, productId);
        if (history == null || history.isEmpty()) {
            return hello();
        }
        return history;
    }
}
