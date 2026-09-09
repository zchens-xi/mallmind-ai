/**
 * Java端数据映射修复示例
 * 
 * 问题：Python服务无法从Java发送的数据中提取有效的类别、价格和评分信息
 * 解决方案：确保正确映射字段名称
 */

import java.util.*;
import org.springframework.stereotype.Service;
import com.macro.mall.model.*;
import com.macro.mall.portal.domain.*;

@Service
public class UserBehaviorDataService {

    /**
     * 修复：为长期动量计算准备用户行为数据
     * 确保包含正确的字段名称
     */
    public Map<String, Object> prepareUserBehaviorData(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取订单数据
        List<OmsOrder> orders = orderService.getOrdersByUserId(userId);
        List<Map<String, Object>> ordersData = new ArrayList<>();
        
        for (OmsOrder order : orders) {
            // 获取订单项
            List<OmsOrderItem> orderItems = orderItemService.getByOrderId(order.getId());
            
            for (OmsOrderItem item : orderItems) {
                Map<String, Object> orderData = new HashMap<>();
                
                // 添加必要字段 - 确保包含"category"字段
                orderData.put("category", item.getProductCategoryName());  // 关键修复点：添加category字段
                orderData.put("price", item.getProductPrice());
                orderData.put("rating", item.getProductRating() != null ? item.getProductRating() : 4.0);
                
                // 添加备用字段，以防Python服务查找不同的字段名
                orderData.put("product_category_name", item.getProductCategoryName());
                orderData.put("productCategoryName", item.getProductCategoryName());
                orderData.put("product_price", item.getProductPrice());
                orderData.put("productPrice", item.getProductPrice());
                
                // 添加其他有用的信息
                orderData.put("product_id", item.getProductId());
                orderData.put("product_name", item.getProductName());
                orderData.put("quantity", item.getProductQuantity());
                
                ordersData.add(orderData);
            }
        }
        
        // 获取购物车数据
        List<OmsCartItem> cartItems = cartItemService.getCartByUserId(userId);
        List<Map<String, Object>> cartData = new ArrayList<>();
        
        for (OmsCartItem item : cartItems) {
            Map<String, Object> cartItemData = new HashMap<>();
            
            // 获取商品信息以补充类别
            PmsProduct product = productService.getById(item.getProductId());
            String categoryName = product != null ? product.getProductCategoryName() : "";
            
            // 添加必要字段 - 确保包含"category"字段
            cartItemData.put("category", categoryName);  // 关键修复点：添加category字段
            cartItemData.put("price", item.getPrice());
            
            // 添加备用字段
            cartItemData.put("product_category_name", categoryName);
            cartItemData.put("productCategoryName", categoryName);
            cartItemData.put("product_price", item.getPrice());
            cartItemData.put("productPrice", item.getPrice());
            
            // 添加其他有用的信息
            cartItemData.put("product_id", item.getProductId());
            cartItemData.put("quantity", item.getQuantity());
            
            cartData.add(cartItemData);
        }
        
        // 获取退货数据
        List<OmsOrderReturnApply> returnApplies = returnApplyService.getByUsername(username);
        List<Map<String, Object>> returnsData = new ArrayList<>();
        
        for (OmsOrderReturnApply returnApply : returnApplies) {
            Map<String, Object> returnData = new HashMap<>();
            
            // 添加必要字段 - 确保包含"category"字段
            // 通常需要从商品服务获取类别信息
            String categoryName = getCategoryNameByProductId(returnApply.getProductId());
            
            returnData.put("category", categoryName);  // 关键修复点：添加category字段
            returnData.put("price", returnApply.getProductPrice());
            
            // 添加备用字段
            returnData.put("product_category_name", categoryName);
            returnData.put("productCategoryName", categoryName);
            returnData.put("product_price", returnApply.getProductPrice());
            returnData.put("productPrice", returnApply.getProductPrice());
            
            // 添加其他有用的信息
            returnData.put("product_id", returnApply.getProductId());
            returnData.put("product_name", returnApply.getProductName());
            
            returnsData.add(returnData);
        }
        
        // 组装最终数据
        result.put("orders", ordersData);
        result.put("cart", cartData);
        result.put("returns", returnsData);
        
        return result;
    }
    
    /**
     * 辅助方法：根据商品ID获取类别名称
     */
    private String getCategoryNameByProductId(Long productId) {
        PmsProduct product = productService.getById(productId);
        return product != null ? product.getProductCategoryName() : "";
    }
} 