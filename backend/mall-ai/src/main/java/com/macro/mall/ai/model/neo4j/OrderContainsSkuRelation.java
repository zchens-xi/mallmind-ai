//package com.macro.mall.ai.model.neo4j;
//
//import lombok.Data;
//import org.springframework.data.neo4j.core.schema.RelationshipId;
//import org.springframework.data.neo4j.core.schema.RelationshipProperties;
//import org.springframework.data.neo4j.core.schema.TargetNode;
//
//import java.math.BigDecimal;
//
//@RelationshipProperties
//@Data
//public class OrderContainsSkuRelation {
//
//    @RelationshipId
//    private Long graphId;
//
//    private Integer quantity;
//    private BigDecimal price;
//
//    @TargetNode
//    private final SkuNode sku;
//
//    public OrderContainsSkuRelation(SkuNode sku) {
//        this.sku = sku;
//    }
//}