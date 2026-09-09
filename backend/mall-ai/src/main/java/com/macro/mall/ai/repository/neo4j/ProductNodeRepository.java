//package com.macro.mall.ai.repository.neo4j;
//
//import com.macro.mall.ai.model.neo4j.CategoryNode;
//import com.macro.mall.ai.model.neo4j.ProductNode;
//import org.springframework.data.neo4j.repository.Neo4jRepository;
//import org.springframework.data.neo4j.repository.query.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface ProductNodeRepository extends Neo4jRepository<ProductNode, Long> {
//
//    Optional<ProductNode> findByProductId(Long productId);
//
//    // 用于创建推理关系的复杂查询
//    @Query("MATCH (u:User)-[:PLACED_BY]->(:Order)-[c:CONTAINS]->(s1:Sku), " +
//            "(u:User)-[:PLACED_BY]->(:Order)-[:CONTAINS]->(s2:Sku) " +
//            "WHERE id(s1) < id(s2) " +
//            "WITH s1, s2, count(u) as userCount " +
//            "MERGE (s1)-[r:ALSO_BOUGHT_WITH]->(s2) " +
//            "SET r.weight = userCount")
//    void calculateAndCreateAlsoBought();
//
//    List<ProductNode> findProductsBygraphId(Long graphId);
//
////    List<ProductNode> findProductsByCategory(Long categoryId, int i);
//}