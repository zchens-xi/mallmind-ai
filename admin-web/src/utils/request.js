import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import store from '../store'
import { getToken } from '@/utils/auth'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.BASE_API, // api的base_url
  timeout: 15000 // 请求超时时间
})

// request拦截器
service.interceptors.request.use(config => {
  console.log('API请求配置:', {
    url: config.url,
    method: config.method,
    baseURL: config.baseURL,
    params: config.params,
    data: config.data,
    headers: config.headers
  })
  
  // 检查请求参数是否合法
  if (config.params) {
    // 检查日期格式是否正确
    if (config.params.startDate && !/^\d{4}-\d{2}-\d{2}$/.test(config.params.startDate)) {
      console.error('请求参数startDate格式错误:', config.params.startDate)
    }
    if (config.params.endDate && !/^\d{4}-\d{2}-\d{2}$/.test(config.params.endDate)) {
      console.error('请求参数endDate格式错误:', config.params.endDate)
    }
  }
  
  if (store.getters.token) {
    config.headers['Authorization'] = getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
  }
  return config
}, error => {
  // Do something with request error
  console.log(error) // for debug
  Promise.reject(error)
})

// respone拦截器
service.interceptors.response.use(
  response => {
  /**
  * code为非200是抛错 可结合自己业务进行修改
  */
    console.log('API响应数据:', response.data)
    
    const res = response.data
    if (res.code !== 200) {
      Message({
        message: res.message,
        type: 'error',
        duration: 3 * 1000
      })

      // 401:未登录;
      if (res.code === 401) {
        MessageBox.confirm('你已被登出，可以取消继续留在该页面，或者重新登录', '确定登出', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          store.dispatch('FedLogOut').then(() => {
            location.reload()// 为了重新实例化vue-router对象 避免bug
          })
        })
      }
      return Promise.reject(res)
    } else {
      return response.data
    }
  },
  error => {
    console.log('API请求错误:', error.config)
    if (error.response) {
      console.log('错误响应数据:', error.response.data)
      console.log('错误响应状态:', error.response.status)
      
      // 根据状态码给出更具体的错误提示
      let errorMessage = '请求失败';
      if (error.response.status === 404) {
        errorMessage = '请求的资源不存在';
      } else if (error.response.status === 500) {
        errorMessage = '服务器内部错误';
      } else if (error.response.status === 502) {
        errorMessage = '网关错误';
      } else if (error.response.status === 503) {
        errorMessage = '服务不可用';
      } else if (error.response.status === 504) {
        errorMessage = '网关超时';
      }
      
      Message({
        message: errorMessage,
        type: 'error',
        duration: 5 * 1000
      })
    } else if (error.request) {
      console.log('请求未收到响应:', error.request)
      Message({
        message: '服务器未响应，请检查网络连接',
        type: 'error',
        duration: 5 * 1000
      })
    } else {
      console.log('请求配置错误:', error.message)
      Message({
        message: '请求配置错误: ' + error.message,
        type: 'error',
        duration: 5 * 1000
      })
    }
    return Promise.reject(error)
  }
)

export default service
