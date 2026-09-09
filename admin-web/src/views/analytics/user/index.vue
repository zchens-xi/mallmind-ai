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

    <!-- 用户概览 -->
    <div class="overview-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>总用户数</span>
            </div>
            <div class="overview-value">{{ formatNumber(userData.totalUsers) }}</div>
            <div class="overview-growth">
              <span :class="{'growth-up': userData.userGrowthRate > 0, 'growth-down': userData.userGrowthRate < 0}">
                <i :class="userData.userGrowthRate >= 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                {{ Math.abs(userData.userGrowthRate || 0).toFixed(2) }}%
              </span>
              <span class="overview-compare">同比</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>新增用户</span>
            </div>
            <div class="overview-value">{{ formatNumber(userData.newUsers) }}</div>
            <div class="overview-info">日均新增：{{ formatNumber(userData.avgNewUsers) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>活跃用户</span>
            </div>
            <div class="overview-value">{{ formatNumber(userData.activeUsers) }}</div>
            <div class="overview-info">活跃率：{{ (userData.activeRate || 0).toFixed(2) }}%</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>付费用户</span>
            </div>
            <div class="overview-value">{{ formatNumber(userData.paidUsers) }}</div>
            <div class="overview-info">付费率：{{ (userData.paidRate || 0).toFixed(2) }}%</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户趋势图表 -->
    <el-card class="chart-card" shadow="never">
      <div slot="header" class="clearfix">
        <span>用户增长趋势</span>
        <el-radio-group v-model="userTrendType" size="mini" style="float: right">
          <el-radio-button label="total">累计用户</el-radio-button>
          <el-radio-button label="new">新增用户</el-radio-button>
          <el-radio-button label="active">活跃用户</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart" ref="userTrendChart"></div>
    </el-card>

    <!-- 用户留存和用户活跃度 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户留存率</span>
            </div>
            <div class="chart" ref="retentionChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户活跃度分布</span>
            </div>
            <div class="chart" ref="activityChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户行为分析 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户行为分析</span>
            </div>
            <div class="chart" ref="behaviorChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户访问时段分布</span>
            </div>
            <div class="chart" ref="visitTimeChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <!-- 在用户分析页面添加数据质量警告显示 -->
    <!-- 查找合适的位置添加警告组件 -->
    <el-alert
      v-if="behaviorData.dataQualityWarning"
      :title="behaviorData.dataQualityWarning"
      type="warning"
      :closable="false"
      show-icon
      style="margin: 10px 0;"
    >
    </el-alert>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getUserAnalytics,
  getUserRetentionRate,
  getUserActivityDistribution,
  getUserBehaviorAnalytics,
  getUserVisitTimeDistribution
} from '@/api/analytics'

export default {
  name: 'UserAnalytics',
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
      userTrendType: 'total',

      // 数据
      userData: {
        totalUsers: 0,
        newUsers: 0,
        activeUsers: 0,
        paidUsers: 0,
        userGrowthRate: 0,
        avgNewUsers: 0,
        activeRate: 0,
        paidRate: 0,
        dateList: [],
        totalUsersList: [],
        newUsersList: [],
        activeUsersList: []
      },
      retentionData: [],
      activityDistribution: [],
      behaviorData: {},
      visitTimeDistribution: [],
      
      // 图表实例
      userTrendChart: null,
      retentionChart: null,
      activityChart: null,
      behaviorChart: null,
      visitTimeChart: null
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
    if (this.userTrendChart) this.userTrendChart.dispose()
    if (this.retentionChart) this.retentionChart.dispose()
    if (this.activityChart) this.activityChart.dispose()
    if (this.behaviorChart) this.behaviorChart.dispose()
    if (this.visitTimeChart) this.visitTimeChart.dispose()
  },
  watch: {
    userTrendType() {
      this.renderUserTrendChart()
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
      this.getUserData()
      this.getRetentionData()
      this.getActivityDistribution()
      this.getBehaviorData()
      this.getVisitTimeDistribution()
    },
    
    // 重新调整图表大小
    resizeCharts() {
      if (this.userTrendChart) this.userTrendChart.resize()
      if (this.retentionChart) this.retentionChart.resize()
      if (this.activityChart) this.activityChart.resize()
      if (this.behaviorChart) this.behaviorChart.resize()
      if (this.visitTimeChart) this.visitTimeChart.resize()
    },
    
    // 获取用户数据
    getUserData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        type: this.timeGranularity
      }
      
      getUserAnalytics(params).then(response => {
        this.userData = response.data
        this.renderUserTrendChart()
      })
    },
    
    // 获取用户留存率数据
    getRetentionData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getUserRetentionRate(params).then(response => {
        this.retentionData = response.data
        this.renderRetentionChart()
      })
    },
    
    // 获取用户活跃度分布
    getActivityDistribution() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getUserActivityDistribution(params).then(response => {
        this.activityDistribution = response.data
        this.renderActivityChart()
      })
    },
    
    // 获取用户行为数据
    getBehaviorData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getUserBehaviorAnalytics(params).then(response => {
        this.behaviorData = response.data
        this.renderBehaviorChart()
      })
    },
    
    // 获取用户访问时段分布
    getVisitTimeDistribution() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getUserVisitTimeDistribution(params).then(response => {
        this.visitTimeDistribution = response.data
        this.renderVisitTimeChart()
      })
    },
    
    // 渲染用户趋势图表
    renderUserTrendChart() {
      if (!this.userData || !this.userData.dateList) return
      
      const chartDom = this.$refs.userTrendChart
      if (!chartDom) return
      
      if (!this.userTrendChart) {
        this.userTrendChart = echarts.init(chartDom)
      }
      
      let data = []
      let name = ''
      
      switch (this.userTrendType) {
        case 'total':
          data = this.userData.totalUsersList
          name = '累计用户'
          break
        case 'new':
          data = this.userData.newUsersList
          name = '新增用户'
          break
        case 'active':
          data = this.userData.activeUsersList
          name = '活跃用户'
          break
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            const param = params[0]
            return `${param.name}<br/>${param.seriesName}: ${this.formatNumber(param.value)}`
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
          data: this.userData.dateList
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
        series: [
          {
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
                  color: 'rgba(128, 0, 255, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(128, 0, 255, 0.1)'
                }
              ])
            },
            itemStyle: {
              color: '#8000ff'
            }
          }
        ]
      }
      
      this.userTrendChart.setOption(option)
    },
    
    // 渲染用户留存率图表
    renderRetentionChart() {
      if (!this.retentionData || this.retentionData.length === 0) return
      
      const chartDom = this.$refs.retentionChart
      if (!chartDom) return
      
      if (!this.retentionChart) {
        this.retentionChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const dates = this.retentionData.map(item => item.date)
      const days = ['次日', '7日', '30日']
      const series = days.map(day => {
        const dayKey = day === '次日' ? 'nextDayRate' : day === '7日' ? 'day7Rate' : 'day30Rate'
        return {
          name: day + '留存',
          type: 'bar',
          data: this.retentionData.map(item => item[dayKey])
        }
      })
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              result += `${param.seriesName}: ${param.value}%<br/>`
            })
            return result
          }
        },
        legend: {
          data: days.map(day => day + '留存')
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value',
          name: '留存率(%)',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: series
      }
      
      this.retentionChart.setOption(option)
    },
    
    // 渲染用户活跃度分布图表
    renderActivityChart() {
      if (!this.activityDistribution || this.activityDistribution.length === 0) return
      
      const chartDom = this.$refs.activityChart
      if (!chartDom) return
      
      if (!this.activityChart) {
        this.activityChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = this.activityDistribution.map(item => ({
        value: item.count,
        name: item.level
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '活跃度',
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
      
      this.activityChart.setOption(option)
    },
    
    // 渲染用户行为分析图表
    renderBehaviorChart() {
      if (!this.behaviorData || !this.behaviorData.funnelData) return
      
      const chartDom = this.$refs.behaviorChart
      if (!chartDom) return
      
      if (!this.behaviorChart) {
        this.behaviorChart = echarts.init(chartDom)
      }
      
      // 处理数据，确保显示中文标签
      const data = this.behaviorData.funnelData.map(item => ({
        value: item.value,
        name: item.name || item.stage || item.label // 尝试多个可能的字段名
      }))
      
      console.log('用户行为漏斗数据:', data)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人'
        },
        legend: {
          data: data.map(item => item.name),
          bottom: 0
        },
        series: [
          {
            name: '用户行为',
            type: 'funnel',
            left: '10%',
            top: 30,
            bottom: 60,
            width: '80%',
            min: 0,
            max: data[0] ? data[0].value : 100,
            minSize: '0%',
            maxSize: '100%',
            sort: 'descending',
            gap: 2,
            label: {
              show: true,
              position: 'inside',
              formatter: '{b}\n{c}人'
            },
            labelLine: {
              length: 10,
              lineStyle: {
                width: 1,
                type: 'solid'
              }
            },
            itemStyle: {
              borderColor: '#fff',
              borderWidth: 1
            },
            emphasis: {
              label: {
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            data: data
          }
        ]
      }
      
      this.behaviorChart.setOption(option)
    },
    
    // 渲染用户访问时段分布图表
    renderVisitTimeChart() {
      if (!this.visitTimeDistribution || this.visitTimeDistribution.length === 0) return
      
      const chartDom = this.$refs.visitTimeChart
      if (!chartDom) return
      
      if (!this.visitTimeChart) {
        this.visitTimeChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const hours = Array.from({length: 24}, (_, i) => i)
      const data = hours.map(hour => {
        const item = this.visitTimeDistribution.find(d => d.hour === hour)
        return item ? item.count : 0
      })
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}时: {c}次'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: hours.map(hour => hour + '时')
        },
        yAxis: {
          type: 'value',
          name: '访问次数',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: [
          {
            name: '访问次数',
            type: 'bar',
            data: data,
            itemStyle: {
              color: function(params) {
                // 根据时段设置不同颜色
                const hour = parseInt(params.name)
                if (hour >= 0 && hour < 6) {
                  return '#3366cc' // 凌晨
                } else if (hour >= 6 && hour < 12) {
                  return '#66cc99' // 上午
                } else if (hour >= 12 && hour < 18) {
                  return '#ff9933' // 下午
                } else {
                  return '#9966cc' // 晚上
                }
              }
            }
          }
        ]
      }
      
      this.visitTimeChart.setOption(option)
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