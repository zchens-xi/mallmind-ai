<template>
	<view class="chat-container">
		<!-- 头部 -->
		<view class="chat-header">
			<view class="header-content">
				<view class="service-icon">
					<text class="yticon icon-xiaoxi"></text>
				</view>
				<view class="header-text">
					<text class="header-title">AI智能客服</text>
					<text class="header-subtitle">24小时在线为您服务</text>
				</view>
				<view class="status-dot"></view>
			</view>
		</view>
		
		<!-- 聊天消息区域 -->
		<scroll-view class="chat-messages" scroll-y="true" :scroll-top="scrollTop" scroll-with-animation>
			<view v-for="(message, index) in messageList" :key="index" class="message-item">
				<!-- AI消息 -->
				<view v-if="message.type === 'ai'" class="message-ai">
					<view class="avatar-wrapper">
						<view class="ai-avatar">
							<text class="yticon icon-tuijian"></text>
						</view>
					</view>
					<view class="message-content">
						<view class="message-bubble ai-bubble" :class="{thinking: message.isThinking}">
							<text class="message-text">{{ message.content }}</text>
							<view v-if="message.isThinking" class="thinking-dots">
								<view class="dot"></view>
								<view class="dot"></view>
								<view class="dot"></view>
							</view>
						</view>
						<text class="message-time">{{ message.time }}</text>
					</view>
				</view>
				
				<!-- 用户消息 -->
				<view v-else class="message-user">
					<view class="message-content">
						<view class="message-bubble user-bubble">
							<text class="message-text">{{ message.content }}</text>
						</view>
						<text class="message-time">{{ message.time }}</text>
					</view>
					<view class="avatar-wrapper">
						<view class="user-avatar">
							<text class="yticon icon-tuandui"></text>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 快捷回复 -->
		<view v-if="showQuickReplies" class="quick-replies">
			<text class="quick-title">常见问题</text>
			<view class="quick-list">
				<view v-for="(reply, index) in quickReplies" :key="index" 
					  class="quick-reply-item" @click="sendQuickReply(reply)">
					<text>{{ reply }}</text>
				</view>
			</view>
		</view>
		
		<!-- 输入框 -->
		<view class="chat-input">
			<view class="input-wrapper">
				<view class="input-container">
					<input v-model="inputText" class="text-input" placeholder="请输入您要咨询的问题..."
						   @confirm="sendMessage" confirm-type="send" />
				</view>
				<view class="send-btn" @click="sendMessage" :class="{active: inputText.trim()}">
					<text class="yticon icon-you"></text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { askAI, askCustomerService, getChatHistory } from '@/api/service.js'

export default {
	data() {
		return {
			inputText: '',
			scrollTop: 0,
			showQuickReplies: true,
			productId: null, // 商品ID，从页面参数获取
			isLoading: false, // 发送中状态
			messageList: [
				{
					type: 'ai',
					content: '您好！欢迎来到Mall商城，我是AI智能客服小助手。很高兴为您服务！\n\n我可以帮您解决：\n• 商品咨询与推荐\n• 订单状态查询\n• 物流信息跟踪\n• 售后服务支持\n• 优惠活动介绍\n\n请问有什么可以帮助您的吗？',
					time: this.getCurrentTime()
				}
			],
			quickReplies: [
				'商品咨询',
				'订单查询', 
				'物流跟踪',
				'退换货',
				'优惠活动',
				'会员权益',
				'支付问题',
				'联系人工'
			]
		}
	},
	
	onLoad(options) {
		// 获取传入的商品ID
		if (options.productId) {
			this.productId = parseInt(options.productId);
			this.loadChatHistory();
		}
		
		// 测试服务器连接
		this.testServerConnection();
	},
	
	methods: {
		// 加载历史对话记录
		async loadChatHistory() {
			if (!this.productId) return;
			
			try {
				console.log('开始加载历史记录，productId:', this.productId);
				const res = await getChatHistory(this.productId);
				console.log('历史记录加载结果:', res);
				if (res.code === 200 && res.data && res.data.length > 0) {
					// 检查首条记录的用户问题是否为空，判断是否有真实历史记录
					const firstRecord = res.data[0];
					const hasRealHistory = firstRecord.userMessage && firstRecord.userMessage.trim() !== '';
					
					if (hasRealHistory) {
						// 有真实历史记录，直接加载历史记录，不清空默认欢迎消息
						// 转换历史记录格式
						res.data.forEach(item => {
							// 只添加非空的用户消息
							if (item.userMessage && item.userMessage.trim() !== '') {
								this.messageList.push({
									type: 'user',
									content: item.userMessage,
									time: this.formatTime(item.timestamp)
								});
							}
							
							// 添加AI回复
							this.messageList.push({
								type: 'ai',
								content: item.aiResponse,
								time: this.formatTime(item.timestamp)
							});
						});
						
						// 添加新的欢迎消息
						this.messageList.push({
							type: 'ai',
							content: '欢迎回来！我已为您加载了之前的对话记录。有什么新的问题需要咨询吗？',
							time: this.getCurrentTime()
						});
					} else {
						// 没有真实历史记录，只有AI问候语，使用默认的欢迎消息
						// 可以选择显示后端返回的问候语，或者保持默认消息
						this.messageList = [{
							type: 'ai',
							content: firstRecord.aiResponse || '您好！我是AI智能客服，很高兴为您服务！请问有什么可以帮您的吗？',
							time: this.getCurrentTime()
						}];
					}
					
					this.scrollToBottom();
				}
			} catch (error) {
				console.error('加载历史记录失败:', error);
				console.error('Error details:', {
					message: error.message,
					data: error.data,
					statusCode: error.statusCode,
					config: error.config
				});
				uni.showToast({
					title: '加载历史记录失败',
					icon: 'none'
				});
			}
		},
		
		async sendMessage() {
			if (!this.inputText.trim() || this.isLoading) return;
			
			const userMessage = this.inputText.trim();
			this.inputText = '';
			this.showQuickReplies = false;
			this.isLoading = true;
			
			// 添加用户消息
			this.messageList.push({
				type: 'user',
				content: userMessage,
				time: this.getCurrentTime()
			});
			
			// 添加AI思考中的临时消息
			const thinkingMessage = {
				type: 'ai',
				content: 'AI正在思考中...',
				time: this.getCurrentTime(),
				isThinking: true // 标记为思考状态
			};
			this.messageList.push(thinkingMessage);
			
			this.scrollToBottom();
			
			try {
				let response;
				
				// 根据是否有商品ID选择不同的接口
				if (this.productId) {
					// 商品客服问答
					console.log('调用商品客服接口，productId:', this.productId, 'message:', userMessage);
					response = await askCustomerService(userMessage, this.productId);
				} else {
					// 通用AI问答
					console.log('调用通用AI接口，message:', userMessage);
					response = await askAI(userMessage);
				}
				
				console.log('API响应结果:', response);
				
				// 移除思考中的消息
				const thinkingIndex = this.messageList.findIndex(msg => msg.isThinking);
				if (thinkingIndex !== -1) {
					this.messageList.splice(thinkingIndex, 1);
				}
				
				if (response.code === 200) {
					// 添加AI回复
					this.messageList.push({
						type: 'ai',
						content: response.data,
						time: this.getCurrentTime()
					});
				} else {
					// 处理错误响应
					this.messageList.push({
						type: 'ai',
						content: '抱歉，我暂时无法回答您的问题，请稍后重试或联系人工客服。',
						time: this.getCurrentTime()
					});
					
					uni.showToast({
						title: response.message || '服务异常',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('发送消息失败:', error);
				console.error('Error details:', {
					message: error.message,
					data: error.data,
					statusCode: error.statusCode,
					config: error.config,
					errMsg: error.errMsg
				});
				
				// 移除思考中的消息
				const thinkingIndex = this.messageList.findIndex(msg => msg.isThinking);
				if (thinkingIndex !== -1) {
					this.messageList.splice(thinkingIndex, 1);
				}
				
				// 添加错误提示消息
				this.messageList.push({
					type: 'ai',
					content: '网络连接异常，请检查网络后重试，或联系人工客服获得帮助。',
					time: this.getCurrentTime()
				});
				
				uni.showToast({
					title: '网络异常，请重试',
					icon: 'none'
				});
			} finally {
				this.isLoading = false;
				this.scrollToBottom();
			}
		},
		
		sendQuickReply(reply) {
			this.inputText = reply;
			this.sendMessage();
		},
		
		getCurrentTime() {
			const now = new Date();
			return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
		},
		
		// 格式化后端返回的时间戳
		formatTime(timestamp) {
			if (!timestamp) return this.getCurrentTime();
			
			const date = new Date(timestamp);
			return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
		},
		
		scrollToBottom() {
			this.$nextTick(() => {
				this.scrollTop = this.messageList.length * 1000;
			});
		},
		
		// 测试服务器连接
		async testServerConnection() {
			try {
				console.log('测试服务器连接...');
				// 尝试调用一个简单的AI接口
				const response = await askAI('测试连接');
				console.log('服务器连接正常:', response);
			} catch (error) {
				console.error('服务器连接失败:', error);
				if (error.statusCode === 500) {
					uni.showModal({
						title: '服务器错误',
						content: '后端服务出现内部错误，请检查服务器状态或联系技术支持',
						showCancel: false
					});
				} else if (error.statusCode === 404) {
					uni.showModal({
						title: '接口不存在',
						content: 'AI服务接口未找到，请检查后端API是否正确部署',
						showCancel: false
					});
				}
			}
		}
	}
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

page {
	background: $page-color-base;
}

.chat-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: $page-color-base;
}

.chat-header {
	background: #fff;
	padding: 20upx 30upx;
	border-bottom: 1px solid $border-color-light;
	
	.header-content {
		display: flex;
		align-items: center;
		
		.service-icon {
			width: 80upx;
			height: 80upx;
			background: linear-gradient(135deg, $uni-color-primary 0%, #ff6b9d 100%);
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-right: 20upx;
			
			.yticon {
				font-size: 36upx;
				color: #fff;
			}
		}
		
		.header-text {
			flex: 1;
			
			.header-title {
				font-size: $font-lg;
				color: $font-color-dark;
				font-weight: bold;
				display: block;
				line-height: 1.2;
			}
			
			.header-subtitle {
				font-size: $font-sm;
				color: $font-color-light;
				margin-top: 4upx;
				display: block;
				line-height: 1.2;
			}
		}
		
		.status-dot {
			width: 16upx;
			height: 16upx;
			background: $uni-color-success;
			border-radius: 50%;
			margin-left: 20upx;
		}
	}
}

.chat-messages {
	flex: 1;
	padding: 20upx;
	overflow-y: auto;
}

.message-item {
	margin-bottom: 30upx;
}

.message-ai, .message-user {
	display: flex;
	align-items: flex-end;
}

.message-ai {
	justify-content: flex-start;
	
	.avatar-wrapper {
		margin-right: 20upx;
	}
}

.message-user {
	justify-content: flex-end;
	
	.avatar-wrapper {
		margin-left: 20upx;
	}
	
	.message-content {
		align-items: flex-end;
	}
}

.ai-avatar, .user-avatar {
	width: 60upx;
	height: 60upx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	
	.yticon {
		font-size: 28upx;
		color: #fff;
	}
}

.ai-avatar {
	background: linear-gradient(135deg, $uni-color-primary 0%, #ff6b9d 100%);
}

.user-avatar {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.message-content {
	display: flex;
	flex-direction: column;
	max-width: 70%;
}

.message-bubble {
	padding: 20upx 24upx;
	border-radius: 20upx;
	margin-bottom: 8upx;
	position: relative;
	
	&:before {
		content: '';
		position: absolute;
		width: 0;
		height: 0;
		border: 10upx solid transparent;
	}
}

.ai-bubble {
	background: #fff;
	border-radius: 20upx 20upx 20upx 4upx;
	box-shadow: 0 2upx 12upx rgba(0,0,0,0.1);
	
	&:before {
		left: -18upx;
		bottom: 10upx;
		border-right-color: #fff;
	}
	
	&.thinking {
		background: #f8f9fa;
		
		.message-text {
			color: $font-color-light;
			font-style: italic;
		}
	}
}

.thinking-dots {
	display: flex;
	align-items: center;
	gap: 8upx;
	margin-top: 10upx;
	
	.dot {
		width: 8upx;
		height: 8upx;
		background: $uni-color-primary;
		border-radius: 50%;
		animation: thinking 1.4s infinite both;
		
		&:nth-child(1) {
			animation-delay: -0.32s;
		}
		
		&:nth-child(2) {
			animation-delay: -0.16s;
		}
		
		&:nth-child(3) {
			animation-delay: 0s;
		}
	}
}

@keyframes thinking {
	0%, 80%, 100% {
		transform: scale(0.8);
		opacity: 0.5;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}

.user-bubble {
	background: $uni-color-primary;
	color: #fff;
	border-radius: 20upx 20upx 4upx 20upx;
	
	&:before {
		right: -18upx;
		bottom: 10upx;
		border-left-color: $uni-color-primary;
	}
}

.message-text {
	font-size: $font-base;
	line-height: 1.5;
	word-wrap: break-word;
}

.message-time {
	font-size: $font-sm - 2upx;
	color: $font-color-light;
	margin-top: 4upx;
}

.quick-replies {
	background: #fff;
	padding: 20upx 30upx;
	border-top: 1px solid $border-color-light;
	
	.quick-title {
		font-size: $font-sm;
		color: $font-color-light;
		margin-bottom: 20upx;
		display: block;
	}
	
	.quick-list {
		display: flex;
		flex-wrap: wrap;
		gap: 16upx;
	}
	
	.quick-reply-item {
		background: $page-color-base;
		padding: 16upx 24upx;
		border-radius: 30upx;
		border: 1px solid $border-color-base;
		
		text {
			font-size: $font-sm;
			color: $font-color-base;
		}
		
		&:active {
			background: $border-color-light;
			transform: scale(0.95);
		}
	}
}

.chat-input {
	background: #fff;
	padding: 20upx 30upx;
	border-top: 1px solid $border-color-light;
	
	.input-wrapper {
		display: flex;
		align-items: center;
		gap: 20upx;
	}
	
	.input-container {
		flex: 1;
		background: $page-color-base;
		border-radius: 30upx;
		padding: 0 24upx;
		border: 1px solid $border-color-base;
		
		&:focus-within {
			border-color: $uni-color-primary;
		}
	}
	
	.text-input {
		width: 100%;
		height: 70upx;
		line-height: 70upx;
		font-size: $font-base;
		color: $font-color-dark;
		background: transparent;
		border: none;
	}
	
	.send-btn {
		width: 70upx;
		height: 70upx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		background: $border-color-base;
		transition: all 0.3s;
		
		.yticon {
			font-size: 32upx;
			color: $font-color-disabled;
			transition: all 0.3s;
		}
		
		&.active {
			background: $uni-color-primary;
			
			.yticon {
				color: #fff;
			}
		}
		
		&:active {
			transform: scale(0.95);
		}
	}
}

/* 占位符样式 */
.text-input::-webkit-input-placeholder {
	color: $font-color-light;
}
</style>
