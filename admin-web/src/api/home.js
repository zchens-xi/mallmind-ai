import request from '@/utils/request'

// 获取首页数据
export function getHomeData(params) {
  return request({
    url: '/home/data',
    method: 'get',
    params: params
  })
}

// 获取转化漏斗数据
export function getFunnelData() {
  return request({
    url: '/home/funnel',
    method: 'get'
  })
} 