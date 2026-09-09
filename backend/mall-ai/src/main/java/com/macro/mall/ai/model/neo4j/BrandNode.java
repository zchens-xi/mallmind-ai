//package com.macro.mall.ai.model.neo4j;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.neo4j.core.schema.GeneratedValue;
//import org.springframework.data.neo4j.core.schema.*;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Node("Brand")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class BrandNode {
//
//    @Id @GeneratedValue
//    private Long graphId;
//
//    @Property("brandId")
//    private Long brandId; // 业务ID，来自pms_brand.id
//
//    private String name;
//    private String logo;
//    private String brandStory;
//}