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
//@Node("Subject")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class SubjectNode {
//    @Id @GeneratedValue
//    private Long graphId;
//
//    @Property("subjectId")
////    @Indexed(unique = true)
//    private Long subjectId; // 业务ID，来自cms_subject.id
//
//    private String title;
//    private String description;
//    private String pic;
//
//    @Relationship(type = "FEATURES_PRODUCT", direction = Relationship.Direction.OUTGOING)
//    private Set<ProductNode> relatedProducts = new HashSet<>();
//}