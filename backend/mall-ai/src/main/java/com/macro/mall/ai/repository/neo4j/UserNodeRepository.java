//package com.macro.mall.ai.repository.neo4j;
//
//import com.macro.mall.ai.model.neo4j.UserNode;
//import com.macro.mall.ai.model.neo4j.ProductNode;
//import org.springframework.data.neo4j.repository.Neo4jRepository;
//import org.springframework.data.neo4j.repository.query.Query;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//
///**
// * 用户节点数据访问层
// * 用于图谱中的用户节点操作
// */
//@Repository
//public interface UserNodeRepository extends Neo4jRepository<UserNode, Long> {
//    // Spring Data Neo4j会根据方法名自动生成查询
//    Optional<UserNode> findByUserId(Long userId);
//}