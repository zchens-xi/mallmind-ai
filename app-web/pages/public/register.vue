<template>
	<view class="container">
		<view class="left-bottom-sign"></view>
		<view class="back-btn yticon icon-zuojiantou-up" @click="navBack"></view>
		<view class="right-top-sign"></view>
		<!-- 设置白色背景防止软键盘把下部绝对定位元素顶上来盖住输入框等 -->
		<view class="wrapper">
			<view class="register-form">
				<view class="title">会员注册</view>
				<view class="subtitle">欢迎加入Mall商城</view>
				
				<!-- 用户名输入框 -->
				<view class="input-item">
					<text class="yticon icon-tuandui"></text>
					<input class="input" type="text" v-model="formData.username" 
						   placeholder="请输入用户名" placeholder-class="placeholder" />
				</view>
				
				<!-- 邮箱输入框 -->
				<view class="input-item">
					<text class="yticon icon-youxiang"></text>
					<input class="input" type="text" v-model="formData.email" 
						   placeholder="请输入邮箱地址" placeholder-class="placeholder" />
				</view>
				
				<!-- 验证码输入框 -->
				<view class="input-item">
					<text class="yticon icon-yanzhengma"></text>
					<input class="input code-input" type="text" v-model="formData.authCode" 
						   placeholder="请输入邮箱验证码" placeholder-class="placeholder" />
					<view class="code-btn" @click="getAuthCode" :class="{ disabled: codeTime > 0 }">
						<text v-if="codeTime > 0">{{ codeTime }}s</text>
						<text v-else>获取验证码</text>
					</view>
				</view>
				
				<!-- 密码输入框 -->
				<view class="input-item">
					<text class="yticon icon-mima"></text>
					<input class="input" :type="showPassword ? 'text' : 'password'" 
						   v-model="formData.password" placeholder="请输入密码" 
						   placeholder-class="placeholder" />
					<text class="eye-icon yticon" :class="showPassword ? 'icon-yan' : 'icon-biyan'" 
						  @click="showPassword = !showPassword"></text>
				</view>
				
				<!-- 确认密码输入框 -->
				<view class="input-item">
					<text class="yticon icon-mima"></text>
					<input class="input" :type="showConfirmPassword ? 'text' : 'password'" 
						   v-model="confirmPassword" placeholder="请确认密码" 
						   placeholder-class="placeholder" />
					<text class="eye-icon yticon" :class="showConfirmPassword ? 'icon-yan' : 'icon-biyan'" 
						  @click="showConfirmPassword = !showConfirmPassword"></text>
				</view>
				
				<!-- 注册按钮 -->
				<button class="register-btn" @click="handleRegister" :disabled="isLoading">
					<text v-if="isLoading">注册中...</text>
					<text v-else>立即注册</text>
				</button>
				
				<!-- 登录链接 -->
				<view class="login-link">
					<text>已有账号？</text>
					<text class="link" @click="goToLogin">立即登录</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { request } from '@/utils/requestUtil.js'

export default {
	data() {
		return {
			formData: {
				username: '',
				password: '',
				email: '',
				authCode: ''
			},
			confirmPassword: '',
			showPassword: false,
			showConfirmPassword: false,
			isLoading: false,
			codeTime: 0,
			timer: null
		}
	},
	onLoad() {
	},
	methods: {
		navBack() {
			uni.navigateBack();
		},
		
		// 获取验证码
		async getAuthCode() {
			if (this.codeTime > 0) return;
			
			// 验证邮箱
			if (!this.formData.email) {
				uni.showToast({
					title: '请输入邮箱地址',
					icon: 'none'
				});
				return;
			}
			
			// 邮箱格式验证
			const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
			if (!emailRegex.test(this.formData.email)) {
				uni.showToast({
					title: '请输入正确的邮箱格式',
					icon: 'none'
				});
				return;
			}
			
			try {
				// 调用邮箱验证码接口，复用原有的手机验证码接口
				const response = await request({
					url: `/sso/getAuthCode?telephone=${encodeURIComponent(this.formData.email)}`,
					method: 'GET'
				});
				
				if (response.code === 200) {
					uni.showToast({
						title: '验证码已发送到邮箱',
						icon: 'success'
					});
					
					// 开始倒计时
					this.codeTime = 60;
					this.timer = setInterval(() => {
						this.codeTime--;
						if (this.codeTime <= 0) {
							clearInterval(this.timer);
							this.timer = null;
						}
					}, 1000);
				} else {
					uni.showToast({
						title: response.message || '发送失败',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('获取验证码失败:', error);
				uni.showToast({
					title: '网络异常，请重试',
					icon: 'none'
				});
			}
		},
		
		// 注册处理
		async handleRegister() {
			if (this.isLoading) return;
			
			// 表单验证
			if (!this.validateForm()) {
				return;
			}
			
			this.isLoading = true;
			
			try {
				const response = await request({
					url: `/sso/register?username=${encodeURIComponent(this.formData.username)}&password=${encodeURIComponent(this.formData.password)}&telephone=${encodeURIComponent(this.formData.email)}&authCode=${this.formData.authCode}`,
					method: 'POST'
				});
				
				if (response.code === 200) {
					uni.showModal({
						title: '注册成功',
						content: '恭喜您注册成功！现在可以使用账号登录了',
						showCancel: false,
						success: () => {
							// 注册成功后跳转到登录页面
							uni.navigateTo({
								url: '/pages/public/login'
							});
						}
					});
				} else {
					uni.showToast({
						title: response.message || '注册失败',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('注册失败:', error);
				uni.showToast({
					title: '网络异常，请重试',
					icon: 'none'
				});
			} finally {
				this.isLoading = false;
			}
		},
		
		// 表单验证
		validateForm() {
			if (!this.formData.username.trim()) {
				uni.showToast({
					title: '请输入用户名',
					icon: 'none'
				});
				return false;
			}
			
			if (this.formData.username.length < 2) {
				uni.showToast({
					title: '用户名至少2个字符',
					icon: 'none'
				});
				return false;
			}
			
			if (!this.formData.email) {
				uni.showToast({
					title: '请输入邮箱地址',
					icon: 'none'
				});
				return false;
			}
			
			// 邮箱格式验证
			const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
			if (!emailRegex.test(this.formData.email)) {
				uni.showToast({
					title: '请输入正确的邮箱格式',
					icon: 'none'
				});
				return false;
			}
			
			if (!this.formData.authCode) {
				uni.showToast({
					title: '请输入邮箱验证码',
					icon: 'none'
				});
				return false;
			}
			
			if (!this.formData.password) {
				uni.showToast({
					title: '请输入密码',
					icon: 'none'
				});
				return false;
			}
			
			if (this.formData.password.length < 6) {
				uni.showToast({
					title: '密码至少6位',
					icon: 'none'
				});
				return false;
			}
			
			if (this.formData.password !== this.confirmPassword) {
				uni.showToast({
					title: '两次密码不一致',
					icon: 'none'
				});
				return false;
			}
			
			return true;
		},
		
		// 跳转到登录页面
		goToLogin() {
			uni.navigateTo({
				url: '/pages/public/login'
			});
		}
	},
	
	onUnload() {
		// 清理定时器
		if (this.timer) {
			clearInterval(this.timer);
		}
	}
}
</script>

<style lang='scss'>
	@import '@/uni.scss';
	
	page {
		background: #fff;
	}
	
	.container {
		padding-top: 60px;
		position: relative;
		width: 100vw;
		height: 100vh;
		overflow: hidden;
		background: #fff;
	}

	.wrapper {
		position: relative;
		z-index: 90;
		background: #fff;
		padding-bottom: 40upx;
		height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.back-btn {
		position: absolute;
		left: 40upx;
		z-index: 9999;
		padding-top: var(--status-bar-height);
		top: 40upx;
		font-size: 40upx;
		color: $font-color-dark;
	}

	.register-form {
		width: 600upx;
		padding: 60upx 40upx;
		
		.title {
			font-size: 50upx;
			font-weight: bold;
			color: $font-color-dark;
			text-align: center;
			margin-bottom: 20upx;
		}
		
		.subtitle {
			font-size: $font-base;
			color: $font-color-light;
			text-align: center;
			margin-bottom: 80upx;
		}
		
		.input-item {
			position: relative;
			margin-bottom: 40upx;
			border-bottom: 1px solid $border-color-base;
			padding-bottom: 20upx;
			display: flex;
			align-items: center;
			
			.yticon {
				font-size: 36upx;
				color: $font-color-light;
				margin-right: 20upx;
				width: 40upx;
			}
			
			.input {
				flex: 1;
				height: 60upx;
				line-height: 60upx;
				font-size: $font-base;
				color: $font-color-dark;
				
				&.code-input {
					margin-right: 20upx;
				}
			}
			
			.code-btn {
				background: $uni-color-primary;
				color: #fff;
				padding: 16upx 24upx;
				border-radius: 8upx;
				font-size: $font-sm;
				white-space: nowrap;
				
				&.disabled {
					background: $font-color-disabled;
				}
			}
			
			.eye-icon {
				font-size: 36upx;
				color: $font-color-light;
				padding: 10upx;
			}
			
			&:focus-within {
				border-bottom-color: $uni-color-primary;
				
				.yticon {
					color: $uni-color-primary;
				}
			}
		}
		
		.register-btn {
			width: 100%;
			height: 90upx;
			background: $uni-color-primary;
			color: #fff;
			border: none;
			border-radius: 45upx;
			font-size: $font-lg;
			margin-top: 60upx;
			display: flex;
			align-items: center;
			justify-content: center;
			
			&:disabled {
				background: $font-color-disabled;
			}
			
			&:not(:disabled):active {
				background: #e50e63;
				transform: scale(0.98);
			}
		}
		
		.login-link {
			text-align: center;
			margin-top: 60upx;
			font-size: $font-base;
			color: $font-color-light;
			
			.link {
				color: $uni-color-primary;
				margin-left: 10upx;
			}
		}
	}

	.right-top-sign {
		position: absolute;
		top: 80upx;
		right: -30upx;
		z-index: 95;

		&:before,
		&:after {
			display: block;
			content: "";
			width: 400upx;
			height: 80upx;
			background: #b4f3e2;
		}

		&:before {
			transform: rotate(50deg);
			border-radius: 0 50px 0 0;
		}

		&:after {
			position: absolute;
			right: -198upx;
			top: 0;
			transform: rotate(-50deg);
			border-radius: 50px 0 0 0;
		}
	}

	.left-bottom-sign {
		position: absolute;
		left: -270upx;
		bottom: -320upx;
		border: 100upx solid #d0d1fd;
		border-radius: 50%;
		padding: 180upx;
	}
	
	.placeholder {
		color: $font-color-light;
	}
</style>
