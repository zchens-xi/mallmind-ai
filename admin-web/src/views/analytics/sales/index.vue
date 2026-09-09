<template>
  <div class="app-container">
    <!-- 日期选择 -->
    <el-card class="filter-container" shadow="never">
      <div>
        <div style="display: flex;align-items: center">
          <span>时间选择：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            align="right"
            unlink-panels
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            style="width: 400px"
          ></el-date-picker>
          <span style="margin-left: 20px">时间粒度：</span>
          <el-select v-model="timeGranularity" style="width: 120px">
            <el-option label="按日" value="day"></el-option>
            <el-option label="按周" value="week"></el-option>
            <el-option label="按月" value="month"></el-option>
          </el-select>
          <el-button
            type="primary"
            icon="el-icon-search"
            style="margin-left: 20px"
            @click="getData"
          >
            查询
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 销售概览 -->
    <div class="overview-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>销售总额</span>
            </div>
            <div class="overview-value">¥{{ formatNumber(overviewData.totalSales) }}</div>
            <div class="overview-growth">
              <span :class="{'growth-up': overviewData.salesGrowthRate > 0, 'growth-down': overviewData.salesGrowthRate < 0}">
                <i :class="overviewData.salesGrowthRate >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(overviewData.salesGrowthRate).toFixed(2) }}%
              </span>
              <span class="overview-compare">同比</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>订单总数</span>
            </div>
            <div class="overview-value">{{ formatNumber(overviewData.totalOrderCount) }}</div>
            <div class="overview-growth">
              <span :class="{'growth-up': overviewData.orderGrowthRate > 0, 'growth-down': overviewData.orderGrowthRate < 0}">
                <i :class="overviewData.orderGrowthRate >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(overviewData.orderGrowthRate || 0).toFixed(2) }}%
              </span>
              <span class="overview-compare">同比</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>客单价</span>
            </div>
            <div class="overview-value">¥{{ formatNumber(overviewData.avgOrderAmount) }}</div>
            <div class="overview-growth">
              <span :class="{'growth-up': overviewData.avgOrderAmountGrowthRate > 0, 'growth-down': overviewData.avgOrderAmountGrowthRate < 0}">
                <i :class="overviewData.avgOrderAmountGrowthRate >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(overviewData.avgOrderAmountGrowthRate || 0).toFixed(2) }}%
              </span>
              <span class="overview-compare">同比</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>退款率</span>
            </div>
            <div class="overview-value">{{ (overviewData.refundRate || 0).toFixed(2) }}%</div>
            <div class="overview-info">退款金额：¥{{ formatNumber(overviewData.refundAmount) }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 销售趋势图表 -->
    <el-card class="chart-card" shadow="never">
      <div slot="header" class="clearfix">
        <span>销售趋势</span>
        <el-radio-group v-model="salesTrendType" size="mini" style="float: right">
          <el-radio-button label="sales">销售额</el-radio-button>
          <el-radio-button label="orders">订单量</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart" ref="salesTrendChart"></div>
    </el-card>

    <!-- 销售分布图表 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>销售渠道分布</span>
            </div>
            <div class="chart" ref="channelChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>销售地域分布</span>
            </div>
            <div class="chart" ref="regionChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 支付方式和订单状态分布 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>支付方式分布</span>
            </div>
            <div class="chart" ref="paymentChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>订单状态分布</span>
            </div>
            <div class="chart" ref="orderStatusChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getSalesAnalytics,
  getSalesChannelDistribution,
  getUserRegionDistribution,
  getPaymentMethodDistribution,
  getOrderStatusDistribution
} from '@/api/analytics'

export default {
  name: 'SalesAnalytics',
  data() {
    return {
      // 日期选择相关
      pickerOptions: {
        shortcuts: [
          {
            text: '最近一周',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近一个月',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              picker.$emit('pick', [start, end])
            }
          },
          {
            text: '最近三个月',
            onClick(picker) {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              picker.$emit('pick', [start, end])
            }
          }
        ]
      },
      dateRange: [new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000), new Date()], // 默认最近30天
      timeGranularity: 'day',
      salesTrendType: 'sales',

      // 数据
      overviewData: {
        totalSales: 0,
        totalOrderCount: 0,
        avgOrderAmount: 0,
        salesGrowthRate: 0,
        orderGrowthRate: 0,
        avgOrderAmountGrowthRate: 0,
        refundRate: 0,
        refundAmount: 0,
        dateList: [],
        salesList: [],
        orderCountList: []
      },
      channelDistribution: {},
      regionDistribution: {},
      paymentDistribution: [],
      orderStatusDistribution: [],
      
      // 图表实例
      salesTrendChart: null,
      channelChart: null,
      regionChart: null,
      paymentChart: null,
      orderStatusChart: null
    }
  },
  mounted() {
    this.getData()
    // 窗口大小变化时，重新调整图表大小
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    // 销毁图表实例
    if (this.salesTrendChart) this.salesTrendChart.dispose()
    if (this.channelChart) this.channelChart.dispose()
    if (this.regionChart) this.regionChart.dispose()
    if (this.paymentChart) this.paymentChart.dispose()
    if (this.orderStatusChart) this.orderStatusChart.dispose()
  },
  watch: {
    salesTrendType() {
      this.renderSalesTrendChart()
    }
  },
  methods: {
    // 格式化数字，添加千分位
    formatNumber(num) {
      if (!num && num !== 0) return '--'
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    
    // 获取所有数据
    getData() {
      this.getOverviewData()
      this.getChannelDistribution()
      this.getRegionDistribution()
      this.getPaymentDistribution()
      this.getOrderStatusDistribution()
    },
    
    // 重新调整图表大小
    resizeCharts() {
      if (this.salesTrendChart) this.salesTrendChart.resize()
      if (this.channelChart) this.channelChart.resize()
      if (this.regionChart) this.regionChart.resize()
      if (this.paymentChart) this.paymentChart.resize()
      if (this.orderStatusChart) this.orderStatusChart.resize()
    },
    
    // 获取概览数据
    getOverviewData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        type: this.timeGranularity
      }
      
      getSalesAnalytics(params).then(response => {
        this.overviewData = response.data
        this.renderSalesTrendChart()
      })
    },
    
    // 获取销售渠道分布
    getChannelDistribution() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getSalesChannelDistribution(params).then(response => {
        this.channelDistribution = response.data
        this.renderChannelChart()
      })
    },
    
    // 获取销售地域分布
    getRegionDistribution() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getUserRegionDistribution(params).then(response => {
        this.regionDistribution = response.data
        this.renderRegionChart()
      })
    },
    
    // 获取支付方式分布
    getPaymentDistribution() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getPaymentMethodDistribution(params).then(response => {
        this.paymentDistribution = response.data
        this.renderPaymentChart()
      })
    },
    
    // 获取订单状态分布
    getOrderStatusDistribution() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getOrderStatusDistribution(params).then(response => {
        this.orderStatusDistribution = response.data
        this.renderOrderStatusChart()
      })
    },
    
    // 渲染销售趋势图表
    renderSalesTrendChart() {
      if (!this.overviewData || !this.overviewData.dateList) return
      
      const chartDom = this.$refs.salesTrendChart
      if (!chartDom) return
      
      if (!this.salesTrendChart) {
        this.salesTrendChart = echarts.init(chartDom)
      }
      
      const series = []
      const data = this.salesTrendType === 'sales' ? this.overviewData.salesList : this.overviewData.orderCountList
      const name = this.salesTrendType === 'sales' ? '销售额' : '订单量'
      
      series.push({
        name: name,
        type: 'line',
        data: data,
        smooth: true,
        symbolSize: 6,
        lineStyle: {
          width: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(250, 67, 106, 0.3)'
            },
            {
              offset: 1,
              color: 'rgba(250, 67, 106, 0.1)'
            }
          ])
        },
        itemStyle: {
          color: '#fa436a'
        }
      })
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            const param = params[0]
            if (this.salesTrendType === 'sales') {
              return `${param.name}<br/>${param.seriesName}: ¥${this.formatNumber(param.value)}`
            } else {
              return `${param.name}<br/>${param.seriesName}: ${this.formatNumber(param.value)}`
            }
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.overviewData.dateList
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: series
      }
      
      this.salesTrendChart.setOption(option)
    },
    
    // 渲染销售渠道分布图表
    renderChannelChart() {
      if (!this.channelDistribution) return
      
      const chartDom = this.$refs.channelChart
      if (!chartDom) return
      
      if (!this.channelChart) {
        this.channelChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = []
      if (this.channelDistribution.pc) data.push({ value: this.channelDistribution.pc, name: 'PC端' })
      if (this.channelDistribution.app) data.push({ value: this.channelDistribution.app, name: 'APP端' })
      if (this.channelDistribution.wechat) data.push({ value: this.channelDistribution.wechat, name: '微信端' })
      if (this.channelDistribution.others) data.push({ value: this.channelDistribution.others, name: '其他' })
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '销售额',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: data
          }
        ]
      }
      
      this.channelChart.setOption(option)
    },
    
    // 渲染销售地域分布图表
    renderRegionChart() {
      if (!this.regionDistribution) return
      
      const chartDom = this.$refs.regionChart
      if (!chartDom) return
      
      if (!this.regionChart) {
        this.regionChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = []
      const provinces = Object.keys(this.regionDistribution)
      provinces.forEach(province => {
        if (province !== 'count') {
          data.push({
            name: province,
            value: this.regionDistribution[province]
          })
        }
      })
      
      // 按数量排序并取前10
      data.sort((a, b) => b.value - a.value)
      const top10 = data.slice(0, 10)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: top10.map(item => item.name)
        },
        series: [
          {
            name: '用户数量',
            type: 'pie',
            radius: ['0', '70%'],
            center: ['40%', '50%'],
            roseType: 'radius',
            itemStyle: {
              borderRadius: 5
            },
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: true
              }
            },
            data: top10
          }
        ]
      }
      
      this.regionChart.setOption(option)
    },
    
    // 渲染支付方式分布图表
    renderPaymentChart() {
      if (!this.paymentDistribution || this.paymentDistribution.length === 0) return
      
      const chartDom = this.$refs.paymentChart
      if (!chartDom) return
      
      if (!this.paymentChart) {
        this.paymentChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = this.paymentDistribution.map(item => ({
        value: item.value,
        name: item.name
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '支付金额',
            type: 'pie',
            radius: '70%',
            center: ['40%', '50%'],
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      
      this.paymentChart.setOption(option)
    },
    
    // 渲染订单状态分布图表
    renderOrderStatusChart() {
      if (!this.orderStatusDistribution || this.orderStatusDistribution.length === 0) return
      
      const chartDom = this.$refs.orderStatusChart
      if (!chartDom) return
      
      if (!this.orderStatusChart) {
        this.orderStatusChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = this.orderStatusDistribution.map(item => ({
        value: item.value,
        name: item.name
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}单 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '订单数量',
            type: 'pie',
            radius: '70%',
            center: ['40%', '50%'],
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      
      this.orderStatusChart.setOption(option)
    },
    
    // 格式化日期为YYYY-MM-DD格式
    formatDate(date) {
      const year = date.getFullYear()
      let month = date.getMonth() + 1
      let day = date.getDate()
      
      month = month < 10 ? '0' + month : month
      day = day < 10 ? '0' + day : day
      
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style scoped>
.filter-container {
  margin-bottom: 20px;
}

.overview-container {
  margin-bottom: 20px;
}

.overview-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.overview-growth {
  font-size: 14px;
  margin-bottom: 5px;
}

.growth-up {
  color: #67C23A;
}

.growth-down {
  color: #F56C6C;
}

.overview-compare {
  color: #909399;
  margin-left: 5px;
  font-size: 12px;
}

.overview-info {
  font-size: 14px;
  color: #909399;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 400px;
}

.chart {
  width: 100%;
  height: 320px;
}
</style> 