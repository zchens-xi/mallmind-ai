<template>
  <div class="app-container">
    <!-- 概览数据卡片 -->
      <el-row :gutter="20">
        <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="card-item">
            <div class="card-icon sales-icon">
              <i class="el-icon-s-finance"></i>
            </div>
            <div class="card-info">
              <div class="card-title">今日销售额</div>
              <div class="card-value">¥{{ formatNumber(homeData.todaySales) }}</div>
              <div class="card-compare">
                <span>昨日: ¥{{ formatNumber(homeData.yesterdaySales) }}</span>
                <span class="card-trend" :class="{'up': homeData.salesGrowth > 0, 'down': homeData.salesGrowth < 0}">
                  <i :class="homeData.salesGrowth > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(homeData.salesGrowth).toFixed(2) }}%
                </span>
          </div>
            </div>
          </div>
        </el-card>
        </el-col>
        <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="card-item">
            <div class="card-icon order-icon">
              <i class="el-icon-s-order"></i>
            </div>
            <div class="card-info">
              <div class="card-title">今日订单数</div>
              <div class="card-value">{{ formatNumber(homeData.todayOrders) }}</div>
              <div class="card-compare">
                <span>昨日: {{ formatNumber(homeData.yesterdayOrders) }}</span>
                <span class="card-trend" :class="{'up': homeData.orderGrowth > 0, 'down': homeData.orderGrowth < 0}">
                  <i :class="homeData.orderGrowth > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(homeData.orderGrowth).toFixed(2) }}%
                </span>
          </div>
    </div>
          </div>
        </el-card>
        </el-col>
        <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="card-item">
            <div class="card-icon user-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="card-info">
              <div class="card-title">今日新增用户</div>
              <div class="card-value">{{ formatNumber(homeData.todayUsers) }}</div>
              <div class="card-compare">
                <span>昨日: {{ formatNumber(homeData.yesterdayUsers) }}</span>
                <span class="card-trend" :class="{'up': homeData.userGrowth > 0, 'down': homeData.userGrowth < 0}">
                  <i :class="homeData.userGrowth > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(homeData.userGrowth).toFixed(2) }}%
                </span>
            </div>
            </div>
            </div>
        </el-card>
          </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="card-item">
            <div class="card-icon visit-icon">
              <i class="el-icon-view"></i>
            </div>
            <div class="card-info">
              <div class="card-title">今日访问量</div>
              <div class="card-value">{{ formatNumber(homeData.todayVisits) }}</div>
              <div class="card-compare">
                <span>昨日: {{ formatNumber(homeData.yesterdayVisits) }}</span>
                <span class="card-trend" :class="{'up': homeData.visitGrowth > 0, 'down': homeData.visitGrowth < 0}">
                  <i :class="homeData.visitGrowth > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(homeData.visitGrowth).toFixed(2) }}%
                </span>
            </div>
            </div>
            </div>
        </el-card>
          </el-col>
        </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>商城销售趋势</span>
            <div style="float: right">
              <el-radio-group v-model="chartTimeRange" size="mini" @change="handleTimeRangeChange">
                <el-radio-button label="week">近一周</el-radio-button>
                <el-radio-button label="month">近一月</el-radio-button>
                <el-radio-button label="year">近一年</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart" ref="salesChart" style="height: 350px"></div>
        </el-card>
        </el-col>
      </el-row>

    <!-- 待办事项和快速入口 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>待处理事项</span>
    </div>
          <el-row :gutter="10" class="todo-item" v-for="(item, index) in todoList" :key="index" @click.native="handleTodoClick(item.type)">
            <el-col :span="19">
              <div class="todo-title" :class="{'todo-urgent': item.urgent}">
                <el-tag size="mini" :type="item.tagType" style="margin-right: 5px">{{ item.tagName }}</el-tag>
                {{ item.title }}
              </div>
            </el-col>
            <el-col :span="5" class="todo-time">
              <el-button type="text" size="mini">更多</el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>快速入口</span>
            </div>
          <el-row :gutter="20" class="shortcut-container">
            <el-col :span="6" v-for="(item, index) in shortcutList" :key="index">
              <div class="shortcut-item" @click="goToPath(item.path)">
                <i :class="item.icon"></i>
                <div>{{ item.title }}</div>
          </div>
        </el-col>
          </el-row>
        </el-card>
        </el-col>
      </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getHomeData } from '@/api/home'

  export default {
    name: 'home',
    data() {
      return {
      homeData: {
        todaySales: 0,
        yesterdaySales: 0,
        salesGrowth: 0,
        todayOrders: 0,
        yesterdayOrders: 0,
        orderGrowth: 0,
        todayUsers: 0,
        yesterdayUsers: 0,
        userGrowth: 0,
        todayVisits: 0,
        yesterdayVisits: 0,
        visitGrowth: 0
      },
      salesChartData: {
        dates: [],
        sales: [],
        orders: []
      },
      chartTimeRange: 'week',
      todoList: [],
      shortcutList: [
        { title: '商品管理', icon: 'el-icon-s-goods', path: '/pms/product' },
        { title: '订单管理', icon: 'el-icon-s-order', path: '/oms/order' },
        { title: '营销活动', icon: 'el-icon-s-ticket', path: '/sms/flash' },
        { title: '会员管理', icon: 'el-icon-s-custom', path: '/ums/admin' },
        { title: '数据分析', icon: 'el-icon-s-data', path: '/center/analytics/overview' },
        { title: '系统设置', icon: 'el-icon-s-tools', path: '/ums/menu' },
        { title: '添加商品', icon: 'el-icon-plus', path: '/pms/addProduct' },
        { title: '优惠券', icon: 'el-icon-s-finance', path: '/sms/coupon' }
      ],
      salesChart: null
    }
  },
  mounted() {
    this.getHomeData()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.salesChart) this.salesChart.dispose()
  },
  methods: {
    // 格式化数字，添加千分位分隔符
    formatNumber(num) {
      if (!num && num !== 0) return '0'
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    
    // 获取首页数据
    getHomeData() {
      getHomeData({ timeRange: this.chartTimeRange }).then(response => {
        this.homeData = response.data.overview
        this.salesChartData = response.data.salesChart
        
        // 更新待处理事项列表
        const pendingTasks = response.data.pendingTasks || {}
        this.todoList = [
          { 
            title: `有${pendingTasks.pendingShipment || 0}个订单待发货`, 
            urgent: true, 
            tagName: '订单', 
            tagType: 'danger', 
            time: '今天',
            type: 'order'
          },
          { 
            title: `有${pendingTasks.pendingRefund || 0}个退款申请待处理`, 
            urgent: true, 
            tagName: '退款', 
            tagType: 'warning', 
            time: '今天',
            type: 'refund'
          }
        ]
        
        this.initSalesChart()
      })
    },
    
    // 初始化销售图表
    initSalesChart() {
      const chartDom = this.$refs.salesChart
      if (!chartDom) return
      
      if (!this.salesChart) {
        this.salesChart = echarts.init(chartDom)
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        legend: {
          data: ['销售额', '订单数']
        },
        xAxis: [
          {
            type: 'category',
            data: this.salesChartData.dates,
            axisPointer: {
              type: 'shadow'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '销售额',
            min: 0,
            axisLabel: {
              formatter: '¥{value}'
            }
          },
          {
            type: 'value',
            name: '订单数',
            min: 0,
            axisLabel: {
              formatter: '{value}'
            }
          }
        ],
        series: [
          {
            name: '销售额',
            type: 'bar',
            data: this.salesChartData.sales,
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '订单数',
            type: 'line',
            yAxisIndex: 1,
            data: this.salesChartData.orders,
            smooth: true,
            itemStyle: {
              color: '#67C23A'
            },
            lineStyle: {
              width: 3
            }
          }
        ]
      }
      
      this.salesChart.setOption(option)
    },
    
    // 图表大小调整
    resizeCharts() {
      if (this.salesChart) this.salesChart.resize()
    },
    
    // 时间范围变更
    handleTimeRangeChange() {
      this.getHomeData()
    },
    
    // 处理待处理事项点击
    handleTodoClick(type) {
      if (type === 'order') {
      this.$router.push('/oms/order').catch(err => {
        if (err.name !== 'NavigationDuplicated') {
          this.$message({
            message: '页面正在加载中，请稍后再试',
            type: 'warning'
          })
        }
      })
      } else if (type === 'refund') {
        this.$router.push('/oms/returnApply').catch(err => {
          if (err.name !== 'NavigationDuplicated') {
            this.$message({
              message: '页面正在加载中，请稍后再试',
              type: 'warning'
            })
          }
        })
      }
    },
    
    // 跳转到指定路径
    goToPath(path) {
      this.$router.push(path).catch(err => {
        // 如果路由不存在或重复导航，给出提示
        if (err.name !== 'NavigationDuplicated') {
          this.$message({
            message: '页面正在加载中，请稍后再试',
            type: 'warning'
          })
        }
      })
    }
    }
  }
</script>

<style scoped>
.card-item {
  display: flex;
  align-items: center;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  }

.card-icon i {
  font-size: 30px;
  color: #fff;
  }

.sales-icon {
  background-color: #409EFF;
  }

.order-icon {
  background-color: #67C23A;
}

.user-icon {
  background-color: #E6A23C;
  }

.visit-icon {
  background-color: #F56C6C;
}

.card-info {
  flex: 1;
  }

.card-title {
  font-size: 14px;
    color: #909399;
  margin-bottom: 5px;
  }

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
  }

.card-compare {
  font-size: 12px;
  color: #909399;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-trend {
    font-weight: bold;
  }

.card-trend.up {
  color: #67C23A;
}

.card-trend.down {
  color: #F56C6C;
}

.todo-item {
  padding: 10px 0;
    border-bottom: 1px solid #EBEEF5;
  }

.todo-item:last-child {
  border-bottom: none;
  }

.todo-title {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  }

.todo-urgent {
  font-weight: bold;
}

.todo-time {
  text-align: right;
  color: #909399;
  font-size: 12px;
  }

.shortcut-container {
  padding: 10px 0;
  }

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80px;
  border-radius: 4px;
  background-color: #F5F7FA;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.shortcut-item:hover {
  background-color: #EBEEF5;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

.shortcut-item i {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 5px;
}

.shortcut-item div {
  font-size: 12px;
  }
</style>
