<template>
	<view v-if="showButton" class="global-service-btn" @click="openAIService">
		<image src="/static/service-icon.png" class="service-icon"></image>
		<text class="service-text">客服</text>
	</view>
</template>

<script>
export default {
	name: 'GlobalServiceButton',
	data() {
		return {
			showButton: true
		}
	},
	methods: {
		openAIService() {
			uni.navigateTo({
				url: '/pages/service/chat'
			});
		}
	}
}
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.global-service-btn {
	position: fixed;
	right: 30upx;
	bottom: 200upx;
	z-index: 9999;
	width: 120upx;
	height: 120upx;
	background: linear-gradient(135deg, $uni-color-primary 0%, #ff6b9d 100%);
	border-radius: 50%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8upx 24upx rgba($uni-color-primary, 0.4);
	
	.service-icon {
		width: 40upx;
		height: 40upx;
		margin-bottom: 5upx;
	}
	
	.service-text {
		font-size: 20upx;
		color: white;
		font-weight: bold;
	}
	
	&:active {
		transform: scale(0.95);
	}
	
	/* 呼吸灯效果 */
	&::before {
		content: '';
		position: absolute;
		width: 120upx;
		height: 120upx;
		border-radius: 50%;
		background: rgba($uni-color-primary, 0.3);
		animation: pulse 2s infinite;
	}
	
	@keyframes pulse {
		0% {
			transform: scale(1);
			opacity: 1;
		}
		100% {
			transform: scale(1.4);
			opacity: 0;
		}
	}
}
</style>
