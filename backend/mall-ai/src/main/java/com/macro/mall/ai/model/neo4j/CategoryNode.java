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
//@Node("Category")
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class CategoryNode {
//
//    @Id @GeneratedValue
//    private Long graphId;
//
//    @Property("categoryId")
//    private Long categoryId; // 业务ID，来自pms_product_category.id
//
//    // 分类名称索引
//    private String name;
//
//    // 层级索引，用于查询特定层级的分类
//    private Integer level;
//
//    private String icon;
//
//    // --- 关系定义 ---
//    // 父子类目关系
//    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.OUTGOING)
//    private Set<CategoryNode> children = new HashSet<>();
//}