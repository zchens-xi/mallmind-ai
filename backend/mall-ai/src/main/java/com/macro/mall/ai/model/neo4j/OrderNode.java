//package com.macro.mall.ai.model.neo4j;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.neo4j.core.schema.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Node("Order")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class OrderNode {
//
//    @Id @GeneratedValue
//    private Long graphId;
//
//    @Property("orderId")
////    @Indexed(unique = true)
//    private Long orderId; // 业务ID，来自oms_order.id
//
//    private String orderSn;
//    private BigDecimal totalAmount;
//    private BigDecimal payAmount;
//    private Integer status;
//    private LocalDateTime createTime;
//    private String deliveryCompany;
//    private String deliverySn;
//    private String receiverName;
//
//    // --- 关系定义 ---
//
//    @Relationship(type = "PLACED_BY", direction = Relationship.Direction.OUTGOING)
//    private UserNode user;
//
//    // 使用关系实体来存储购买数量和价格
//    @Relationship(type = "CONTAINS")
//    private Set<OrderContainsSkuRelation> contains = new HashSet<>();
//}