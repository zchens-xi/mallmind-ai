//package com.macro.mall.ai.model.neo4j;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
////import org.neo4j.ogm.annotation.Index;
//import org.springframework.data.neo4j.core.schema.*;
//
//import java.math.BigDecimal;
//import java.util.HashSet;
//import java.util.Set;
//
//@Node("Product") // 代表SPU
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ProductNode {
//
//    @Id @GeneratedValue
//    private Long graphId;
//
//    @Property("productId")
////    @Indexed(unique = true)
//    private Long productId; // 业务ID，来自pms_product.id
//
//    private String name;
//    private String subTitle;
//    private String description;
//    private String pic;
//    private String productSn; // 货号
//    private Integer sale; // 销量
//    private BigDecimal price; // SPU的基准价
//
//    // --- 关系定义 ---
//
//    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
//    private CategoryNode category;
//
//    @Relationship(type = "PRODUCED_BY", direction = Relationship.Direction.OUTGOING)
//    private BrandNode brand;
//
//    // 一个SPU下有多个SKU
//    @Relationship(type = "HAS_SKU", direction = Relationship.Direction.OUTGOING)
//    private Set<SkuNode> skus = new HashSet<>();
//}