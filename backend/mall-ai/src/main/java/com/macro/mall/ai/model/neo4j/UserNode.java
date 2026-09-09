//package com.macro.mall.ai.model.neo4j;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.neo4j.core.schema.*;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Node("User")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserNode {
//
//    @Id @GeneratedValue
//    private Long graphId; // Neo4j内部ID
//
//    @Property("userId")
////    @Indexed(unique = true)
//    private Long userId; // 业务ID，来自ums_member.id
//
//    private String username;
//    private String nickname;
//    private String icon;
//    private Integer gender;
//    private String city;
//
//    // --- 关系定义 ---
//
//    // 用户购买过的SKU (通过订单间接建立)
//    @Relationship(type = "BOUGHT", direction = Relationship.Direction.OUTGOING)
//    private Set<SkuNode> boughtSkus = new HashSet<>();
//
//    // 用户收藏的商品(SPU)
//    @Relationship(type = "FAVORITED", direction = Relationship.Direction.OUTGOING)
//    private Set<ProductNode> favoritedProducts = new HashSet<>();
//
//    // 用户浏览过的商品(SPU)
//    @Relationship(type = "VIEWED", direction = Relationship.Direction.OUTGOING)
//    private Set<ProductNode> viewedProducts = new HashSet<>();
//
//    // 用户关注的品牌
//    @Relationship(type = "ATTENDS_TO", direction = Relationship.Direction.OUTGOING)
//    private Set<BrandNode> attendedBrands = new HashSet<>();
//}