import request from '@/utils/request'

// 获取销售数据分析
export function getSalesAnalytics(params) {
  console.log('警告：销售数据分析页面已移除，此API可能不再使用')
  console.log('请求销售数据分析API:', '/analytics/sales', params)
  return request({
    url: '/analytics/sales',
    method: 'get',
    params: params
  })
}

// 获取用户行为分析
export function getUserBehaviorAnalytics(params) {
  console.log('警告：分析总览页面已移除，此API可能不再使用')
  console.log('请求用户行为分析API:', '/user-behavior', params)
  return request({
    url: '/user-behavior',
    method: 'get',
    params: params
  })
}

// 获取商品分析数据
export function getProductAnalytics(params) {
  console.log('请求商品分析数据API:', '/analytics/product/overview', params)
  return request({
    url: '/analytics/product/overview',
    method: 'get',
    params: params
  })
}

// 获取用户画像数据
export function getUserPortrait(params) {
  console.log('请求用户画像数据API:', '/analytics/user/portrait', params)
  return request({
    url: '/analytics/user/portrait',
    method: 'get',
    params: params
  })
}

// 获取销售渠道分布
export function getSalesChannelDistribution() {
  console.log('请求销售渠道分布API:', '/analytics/sales/channel')
  return request({
    url: '/analytics/sales/channel',
    method: 'get'
  })
}

// 获取商品销售排行
export function getProductSalesRank(params) {
  console.log('请求商品销售排行API:', '/analytics/product/sales-rank', params)
  return request({
    url: '/analytics/product/sales-rank',
    method: 'get',
    params: params
  })
}

// 获取用户地域分布
export function getUserRegionDistribution() {
  console.log('请求用户地域分布API:', '/analytics/user/region')
  return request({
    url: '/analytics/user/region',
    method: 'get'
  })
}

// 获取用户增长统计
export function getUserGrowthStats(params) {
  console.log('请求用户增长统计API:', '/analytics/user/growth', params)
  return request({
    url: '/analytics/user/growth',
    method: 'get',
    params: params
  })
}

// 获取转化漏斗数据
export function getConversionFunnel(params) {
  console.log('警告：分析总览页面已移除，此API可能不再使用')
  console.log('请求转化漏斗数据API:', '/home/funnel', params)
  return request({
    url: '/home/funnel',
    method: 'get',
    params: params
  })
}

// 获取页面访问排行
export function getPageViewRank(params) {
  console.log('请求页面访问排行API:', '/analytics/page/rank', params)
  return request({
    url: '/analytics/page/rank',
    method: 'get',
    params: params
  })
} 

// 获取用户分析数据
export function getUserAnalytics(params) {
  console.log('请求用户分析数据API:', '/analytics/user', params)
  return request({
    url: '/analytics/user',
    method: 'get',
    params: params
  })
}

// 获取用户留存率
export function getUserRetentionRate(params) {
  console.log('请求用户留存率API:', '/analytics/user/retention', params)
  return request({
    url: '/analytics/user/retention',
    method: 'get',
    params: params
  })
}

// 获取用户活跃度分布
export function getUserActivityDistribution(params) {
  console.log('请求用户活跃度分布API:', '/analytics/user/activity', params)
  return request({
    url: '/analytics/user/activity',
    method: 'get',
    params: params
  })
}

// 获取商品浏览排行
export function getProductViewRank(params) {
  console.log('请求商品浏览排行API:', '/analytics/product/view-rank', params)
  return request({
    url: '/analytics/product/view-rank',
    method: 'get',
    params: params
  })
}

// 获取支付方式分布
export function getPaymentMethodDistribution() {
  console.log('请求支付方式分布API:', '/analytics/payment/distribution')
  return request({
    url: '/analytics/payment/distribution',
    method: 'get'
  })
}

// 获取用户访问时间分布
export function getUserVisitTimeDistribution() {
  console.log('请求用户访问时间分布API:', '/analytics/user/visit/time')
  return request({
    url: '/analytics/user/visit/time',
    method: 'get'
  })
}

// 获取商品转化率排行
export function getProductConversionRank(params) {
  console.log('请求商品转化率排行API:', '/analytics/product/conversion-rank', params)
  return request({
    url: '/analytics/product/conversion-rank',
    method: 'get',
    params: params
  })
}

// 获取订单状态分布
export function getOrderStatusDistribution() {
  console.log('请求订单状态分布API:', '/analytics/order/status')
  return request({
    url: '/analytics/order/status',
    method: 'get'
  })
}

// 获取商品价格分布
export function getProductPriceDistribution() {
  console.log('请求商品价格分布API:', '/analytics/product/price-distribution')
  return request({
    url: '/analytics/product/price-distribution',
    method: 'get'
  })
}

// 获取商品库存分布
export function getProductStockDistribution() {
  console.log('请求商品库存分布API:', '/analytics/product/stock-distribution')
  return request({
    url: '/analytics/product/stock-distribution',
    method: 'get'
  })
} 