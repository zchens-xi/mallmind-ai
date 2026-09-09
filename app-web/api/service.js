import Request from '@/js_sdk/luch-request/request.js'
import { API_BASE_URL } from '@/utils/appConfig.js'

// Create and configure HTTP client instance
const http = new Request()

http.setConfig((config) => {
	config.baseUrl = API_BASE_URL
	config.header = {
		'Content-Type': 'application/json',
		...config.header
	}
	return config
})

// Request interceptor to add authentication token
http.interceptor.request((config, cancel) => {
	const token = uni.getStorageSync('token');
	if(token){
		config.header = {
			'Authorization': token,
			...config.header
		}
	}
	return config
})

// Response interceptor to handle errors
http.interceptor.response((response) => {
	const res = response.data;
	if (res.code !== 200) {
		uni.showToast({
			title: res.message,
			duration: 1500
		})
		if (res.code === 401) {
			uni.showModal({
				title: '提示',
				content: '你已被登出，可以取消继续留在该页面，或者重新登录',
				confirmText: '重新登录',
				cancelText: '取消',
				success: function(res) {
					if (res.confirm) {
						uni.navigateTo({
							url: '/pages/public/login'
						})
					}
				}
			});
		}
		return Promise.reject(response);
	} else {
		return response.data;
	}
}, (response) => {
	console.log('response error', response);
	uni.showToast({
		title: response.errMsg,
		duration: 1500
	})
	return Promise.reject(response);
})

/**
 * DeepSeek AI客服相关接口
 * 基础路径: /ai/deepseek
 */

/**
 * 通用AI问答接口
 * @param {String} message 用户提问内容
 * @returns {Promise} AI回答内容
 */
export function askAI(message) {
	return http.post('/ai/deepseek/ask', null, {
		params: {
			message: message
		},
		header: {
			'Content-Type': 'application/json'
		}
	})
}

/**
 * 商品客服问答接口
 * @param {String} question 用户关于商品的问题
 * @param {Number} productId 商品ID
 * @returns {Promise} 基于商品信息的AI回答内容
 */
export function askCustomerService(question, productId) {
	return http.post('/ai/deepseek/ask/customer', null, {
		params: {
			question: question,
			productId: productId
		},
		header: {
			'Content-Type': 'application/json'
		}
	})
}


/**
 * 获取对话历史接口
 * @param {Number} productId 商品ID（作为查询条件）
 * @returns {Promise} 对话历史记录列表
 */
export function getChatHistory(productId) {
	return http.post('/ai/deepseek/history', null, {
		params: {
			productId: productId
		},
		header: {
			'Content-Type': 'application/json'
		}
	})
}

// ========== 以下为保留的扩展接口，可在未来使用 ==========

// 转接人工客服
export function transferToHuman(data) {
	return http.post('/chat/transfer', data)
}

// 获取常见问题
export function getCommonQuestions() {
	return http.get('/chat/common-questions')
}

// 客服满意度评价
export function submitRating(data) {
	return http.post('/chat/rating', data)
}
