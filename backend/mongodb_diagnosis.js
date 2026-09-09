// MongoDB数据诊断脚本
// 用于检查MongoDB中的数据情况

// 连接到数据库
use mall;

print("=== MongoDB数据诊断报告 ===");

// 1. 检查所有集合
print("\n1. 数据库中的所有集合:");
db.getCollectionNames().forEach(function(collection) {
    var count = db.getCollection(collection).count();
    print("  - " + collection + ": " + count + " 条记录");
});

// 2. 检查memberReadHistory集合详情
print("\n2. memberReadHistory集合详细信息:");
var historyCount = db.memberReadHistory.count();
print("  总记录数: " + historyCount);

if (historyCount > 0) {
    // 查看样本数据
    print("\n  样本数据 (最新5条):");
    db.memberReadHistory.find().sort({createTime: -1}).limit(5).forEach(function(doc) {
        print("    " + JSON.stringify(doc));
    });
    
    // 按用户分组统计
    print("\n  按用户统计浏览记录:");
    db.memberReadHistory.aggregate([
        {$group: {_id: "$memberId", count: {$sum: 1}, products: {$addToSet: "$productId"}}},
        {$project: {memberId: "$_id", count: 1, productCount: {$size: "$products"}, _id: 0}},
        {$sort: {count: -1}},
        {$limit: 10}
    ]).forEach(function(doc) {
        print("    用户" + doc.memberId + ": " + doc.count + "次浏览, " + doc.productCount + "个商品");
    });
    
    // 按商品分组统计
    print("\n  按商品统计浏览记录:");
    db.memberReadHistory.aggregate([
        {$group: {_id: "$productId", productName: {$first: "$productName"}, count: {$sum: 1}, users: {$addToSet: "$memberId"}}},
        {$project: {productId: "$_id", productName: 1, count: 1, userCount: {$size: "$users"}, _id: 0}},
        {$sort: {count: -1}},
        {$limit: 10}
    ]).forEach(function(doc) {
        print("    商品" + doc.productId + "(" + doc.productName + "): " + doc.count + "次浏览, " + doc.userCount + "个用户");
    });
    
    // 时间范围统计
    print("\n  时间范围统计:");
    var now = new Date();
    var thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    var sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    var oneDayAgo = new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000);
    
    print("    最近30天: " + db.memberReadHistory.count({createTime: {$gte: thirtyDaysAgo}}) + " 条");
    print("    最近7天: " + db.memberReadHistory.count({createTime: {$gte: sevenDaysAgo}}) + " 条");
    print("    最近1天: " + db.memberReadHistory.count({createTime: {$gte: oneDayAgo}}) + " 条");
    
    // 检查时间字段类型
    print("\n  时间字段类型检查:");
    var sampleDoc = db.memberReadHistory.findOne();
    if (sampleDoc && sampleDoc.createTime) {
        print("    createTime类型: " + typeof sampleDoc.createTime);
        print("    createTime值: " + sampleDoc.createTime);
        print("    是否为Date: " + (sampleDoc.createTime instanceof Date));
    }
    
} else {
    print("  ⚠️ memberReadHistory集合为空!");
}

// 3. 检查其他可能的浏览记录集合
print("\n3. 检查其他可能的浏览记录集合:");
var possibleCollections = ["member_read_history", "memberReadHistory", "readHistory", "userHistory", "viewHistory"];
possibleCollections.forEach(function(collectionName) {
    try {
        var count = db.getCollection(collectionName).count();
        if (count > 0) {
            print("  发现数据: " + collectionName + " (" + count + " 条记录)");
            var sample = db.getCollection(collectionName).findOne();
            print("    样本数据: " + JSON.stringify(sample));
        }
    } catch (e) {
        // 集合不存在，忽略
    }
});

// 4. 检查商品收藏集合
print("\n4. memberProductCollection集合信息:");
var collectionCount = db.memberProductCollection.count();
print("  总收藏数: " + collectionCount);
if (collectionCount > 0) {
    print("  样本数据:");
    db.memberProductCollection.find().limit(3).forEach(function(doc) {
        print("    " + JSON.stringify(doc));
    });
}

// 5. 生成测试数据建议
print("\n5. 数据问题分析和建议:");
if (historyCount < 10) {
    print("  ⚠️ 浏览记录数据过少 (" + historyCount + " 条)");
    print("  建议:");
    print("    1. 检查前端是否正确记录用户浏览行为");
    print("    2. 检查MongoDB连接配置是否正确");
    print("    3. 执行数据修复脚本添加测试数据");
}

print("\n✅ MongoDB数据诊断完成!");
