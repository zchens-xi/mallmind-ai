//package com.macro.mall.ai.model.neo4j;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.neo4j.core.schema.*;
//
//import java.math.BigDecimal;
//
//// 使用 @CompositeIndex 在类级别定义索引
//@Node("Sku")
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class SkuNode {
//
//    @Id @GeneratedValue
//    private Long graphId;
//
//    @Property("skuId")
//    private Long skuId; // 业务ID，来自pms_sku_stock.id
//
//    private String skuCode;
//    private BigDecimal price;
//    private Integer stock;
//    private String pic;
//
//    @Property("spData")
//    private String spData; // 存储原始的JSON规格数据，便于调试
//
//    // --- 关系定义 ---
//
//    // 属于哪个SPU (反向关系，由ProductNode维护)
//    // @Relationship(type = "IS_VARIANT_OF", direction = Relationship.Direction.OUTGOING)
//    // private ProductNode product;
//
//    // 拥有哪些具体规格
//    // 注释掉AttributeNode引用，避免编译错误
//    // @Relationship(type = "HAS_SPEC", direction = Relationship.Direction.OUTGOING)
//    // private Set<AttributeNode> specs = new HashSet<>();
//}