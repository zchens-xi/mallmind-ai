//package com.macro.mall.ai.model.neo4j;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.neo4j.core.schema.GeneratedValue;
//import org.springframework.data.neo4j.core.schema.*;
//@Node("Attribute")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class AttributeNode {
//
//    @Id @GeneratedValue
//    private Long graphId;
//
//    // 联合唯一，一个type下value是唯一的
//    @Property("type")
////    @Indexed
//    private String type; // e.g., "颜色", "容量"
//
//    @Property("value")
////    @Indexed
//    private String value; // e.g., "红色", "128G"
//}