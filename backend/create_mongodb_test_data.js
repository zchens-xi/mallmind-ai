// MongoDB数据修复脚本 - Node.js版本
const { MongoClient } = require('mongodb');

// 连接URL
const url = 'mongodb://localhost:27017';
const dbName = 'mall-port';

async function main() {
  // 连接到MongoDB
  const client = await MongoClient.connect(url);
  console.log('已连接到MongoDB');
  
  const db = client.db(dbName);
  const memberReadHistory = db.collection('memberReadHistory');
  
  // 获取当前时间
  const now = new Date();
  const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);
  const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
  
  // 用户1的浏览记录
  await memberReadHistory.insertMany([
    {
      memberId: 1,
      productId: 26,
      productName: "华为 HUAWEI P30",
      categoryId: 19,
      createTime: oneMonthAgo
    },
    {
        memberId: 1,
        productId: 27,
        productName: "小米8",
        categoryId: 19,
        createTime: new Date(oneMonthAgo.getTime() + 60 * 60 * 1000) // 1小时后
    },
    {
        memberId: 1,
        productId: 28,
        productName: "红米5A",
        categoryId: 19,
        createTime: new Date(oneMonthAgo.getTime() + 2 * 60 * 60 * 1000) // 2小时后
    }
  ]);

  // 用户2的浏览记录
  await memberReadHistory.insertMany([
    {
        memberId: 2,
        productId: 26,
        productName: "华为 HUAWEI P30",
        categoryId: 19,
        createTime: oneWeekAgo
    },
    {
        memberId: 2,
        productId: 29,
        productName: "Apple iPhone 8 Plus",
        categoryId: 19,
        createTime: new Date(oneWeekAgo.getTime() + 30 * 60 * 1000) // 30分钟后
    }
  ]);

  // 用户3的浏览记录
  await memberReadHistory.insertMany([
    {
        memberId: 3,
        productId: 27,
        productName: "小米8",
        categoryId: 19,
        createTime: oneDayAgo
    },
    {
        memberId: 3,
        productId: 28,
        productName: "红米5A", 
        categoryId: 19,
        createTime: new Date(oneDayAgo.getTime() + 45 * 60 * 1000) // 45分钟后
    },
    {
        memberId: 3,
        productId: 30,
        productName: "Samsung Galaxy S10+",
        categoryId: 19,
        createTime: new Date(oneDayAgo.getTime() + 90 * 60 * 1000) // 90分钟后
    }
  ]);

  // 额外添加一些未购买用户的浏览记录，使数据更真实
  // 用户4-10（假设他们没有购买）
  for (let userId = 4; userId <= 10; userId++) {
    const userViewTime = new Date(oneWeekAgo.getTime() + Math.random() * 7 * 24 * 60 * 60 * 1000);
    
    await memberReadHistory.insertMany([
        {
            memberId: userId,
            productId: 26,
            productName: "华为 HUAWEI P30",
            categoryId: 19,
            createTime: userViewTime
        },
        {
            memberId: userId,
            productId: 27,
            productName: "小米8",
            categoryId: 19,
            createTime: new Date(userViewTime.getTime() + Math.random() * 60 * 60 * 1000)
        }
    ]);
  }
  
  // 验证数据
  const totalCount = await memberReadHistory.countDocuments();
  console.log("=== 数据修复完成 ===");
  console.log("总浏览记录数:", totalCount);
  
  // 关闭连接
  client.close();
  console.log("MongoDB连接已关闭");
}

main().catch(console.error);