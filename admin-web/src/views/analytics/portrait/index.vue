<template>
  <div class="app-container">

    <!-- 用户画像概览 -->
    <div class="overview-container" v-loading="dataLoading">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <div slot="header" class="clearfix">
              <span>分析用户数</span>
            </div>
            <div class="overview-value">{{ formatNumber(portraitData.analyzedUsers) }}</div>
            <div class="overview-info">覆盖率：{{ (portraitData.coverageRate || 0).toFixed(2) }}%</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <div slot="header" class="clearfix">
              <span>主要用户群体</span>
            </div>
            <div class="overview-value">{{ portraitData.mainUserGroup || '--' }}</div>
            <div class="overview-info">占比：{{ (portraitData.mainUserGroupRate || 0).toFixed(2) }}%</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <div slot="header" class="clearfix">
              <span>平均客单价</span>
            </div>
            <div class="overview-value">¥{{ formatNumber(portraitData.avgOrderAmount) }}</div>
            <div class="overview-info">人均消费：¥{{ formatNumber(portraitData.avgUserSpend) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <div slot="header" class="clearfix">
              <span>用户忠诚度</span>
            </div>
            <div class="overview-value">{{ (portraitData.loyaltyScore || 0).toFixed(1) }}</div>
            <div class="overview-info">复购率：{{ (portraitData.repurchaseRate || 0).toFixed(2) }}%</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户基本属性分析 -->
    <div class="section-title">用户基本特征</div>
    <div class="chart-container" v-loading="dataLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户性别分布</span>
            </div>
            <div class="chart" ref="genderChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户年龄分布</span>
            </div>
            <div class="chart" ref="ageChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-container" v-loading="dataLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户职业分布</span>
            </div>
            <div class="chart" ref="occupationChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户设备分布</span>
            </div>
            <div class="chart" ref="deviceChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户地域和消费能力分析 -->
    <div class="section-title">用户消费分析</div>
    <div class="chart-container" v-loading="dataLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户地域分布</span>
            </div>
            <div class="chart" ref="regionChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户消费能力分布</span>
            </div>
            <div class="chart" ref="consumptionChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户行为分析 -->
    <div class="section-title">用户行为分析</div>
    <div class="chart-container" v-loading="dataLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户消费偏好</span>
            </div>
            <div class="chart" ref="preferenceChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户活跃时段</span>
            </div>
            <div class="chart" ref="activeTimeChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getUserPortrait } from '@/api/analytics'

export default {
  name: 'UserPortrait',
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
      dateRange: [new Date(new Date().getTime() - 60 * 24 * 60 * 60 * 1000), new Date()], // 默认最近60天
      
      // 加载状态
      dataLoading: false,

      // 数据
      portraitData: {
        analyzedUsers: 0,
        coverageRate: 0,
        mainUserGroup: '',
        mainUserGroupRate: 0,
        avgOrderAmount: 0,
        avgUserSpend: 0,
        loyaltyScore: 0,
        repurchaseRate: 0,
        genderDistribution: [],
        ageDistribution: [],
        occupationDistribution: [],
        regionDistribution: {},
        consumptionLevelDistribution: [],
        consumptionPreferences: [],
        deviceDistribution: [],
        activeTimeDistribution: []
      },
      
      // 图表实例
      genderChart: null,
      ageChart: null,
      occupationChart: null,
      regionChart: null,
      consumptionChart: null,
      preferenceChart: null,
      deviceChart: null,
      activeTimeChart: null
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
    if (this.genderChart) this.genderChart.dispose()
    if (this.ageChart) this.ageChart.dispose()
    if (this.occupationChart) this.occupationChart.dispose()
    if (this.regionChart) this.regionChart.dispose()
    if (this.consumptionChart) this.consumptionChart.dispose()
    if (this.preferenceChart) this.preferenceChart.dispose()
    if (this.deviceChart) this.deviceChart.dispose()
    if (this.activeTimeChart) this.activeTimeChart.dispose()
  },
  methods: {
    // 格式化数字，添加千分位
    formatNumber(num) {
      if (!num && num !== 0) return '--'
      if (typeof num === 'string') {
        num = parseFloat(num)
        if (isNaN(num)) return '--'
      }
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    
    // 获取所有数据
    getData() {
      this.dataLoading = true
      
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      console.log('请求用户画像数据，参数:', params)
      
      getUserPortrait(params).then(response => {
        console.log('用户画像数据响应:', response)
        
        if (response && response.data) {
          this.portraitData = response.data
          this.$nextTick(() => {
            this.renderCharts()
          })
          this.$message.success('用户画像数据加载成功')
        } else {
          this.$message.warning('暂无数据')
          this.initEmptyData()
        }
      }).catch(error => {
        console.error('获取用户画像数据失败:', error)
        this.$message.error('获取用户画像数据失败，请稍后重试')
        this.initEmptyData()
      }).finally(() => {
        this.dataLoading = false
      })
    },
    
    // 初始化空数据
    initEmptyData() {
      this.portraitData = {
        analyzedUsers: 0,
        coverageRate: 0,
        mainUserGroup: '--',
        mainUserGroupRate: 0,
        avgOrderAmount: 0,
        avgUserSpend: 0,
        loyaltyScore: 0,
        repurchaseRate: 0,
        genderDistribution: [],
        ageDistribution: [],
        occupationDistribution: [],
        regionDistribution: {},
        consumptionLevelDistribution: [],
        consumptionPreferences: [],
        deviceDistribution: [],
        activeTimeDistribution: []
      }
      this.$nextTick(() => {
        this.renderCharts()
      })
    },
    
    // 重新调整图表大小
    resizeCharts() {
      if (this.genderChart) this.genderChart.resize()
      if (this.ageChart) this.ageChart.resize()
      if (this.occupationChart) this.occupationChart.resize()
      if (this.regionChart) this.regionChart.resize()
      if (this.consumptionChart) this.consumptionChart.resize()
      if (this.preferenceChart) this.preferenceChart.resize()
      if (this.deviceChart) this.deviceChart.resize()
      if (this.activeTimeChart) this.activeTimeChart.resize()
    },
    
    // 渲染所有图表
    renderCharts() {
      console.log('开始渲染图表，数据:', this.portraitData)
      this.renderGenderChart()
      this.renderAgeChart()
      this.renderOccupationChart()
      this.renderRegionChart()
      this.renderConsumptionChart()
      this.renderPreferenceChart()
      this.renderDeviceChart()
      this.renderActiveTimeChart()
    },
    
    // 渲染用户性别分布图表
    renderGenderChart() {
      const chartDom = this.$refs.genderChart
      if (!chartDom) return
      
      if (!this.genderChart) {
        this.genderChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.genderDistribution || this.portraitData.genderDistribution.length === 0) {
        const option = {
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.genderChart.setOption(option)
        return
      }
      
      // 处理数据
      const data = this.portraitData.genderDistribution.map(item => ({
        value: item.count || 0,
        name: item.gender || '未知'
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '性别分布',
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
            data: data,
            itemStyle: {
              color: function(params) {
                // 根据性别设置不同颜色
                const gender = params.name
                if (gender === '男') {
                  return '#4e79a7'
                } else if (gender === '女') {
                  return '#f28e2c'
                } else {
                  return '#e15759'
                }
              }
            }
          }
        ]
      }
      
      this.genderChart.setOption(option)
    },
    
    // 渲染用户年龄分布图表
    renderAgeChart() {
      const chartDom = this.$refs.ageChart
      if (!chartDom) return
      
      if (!this.ageChart) {
        this.ageChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.ageDistribution || this.portraitData.ageDistribution.length === 0) {
        const option = {
          title: {
            text: '暂无年龄分布数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.ageChart.setOption(option)
        return
      }
      
      // 处理数据
      const data = this.portraitData.ageDistribution.map(item => ({
        value: item.count,
        name: item.ageRange
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '年龄分布',
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
      
      this.ageChart.setOption(option)
    },
    
    // 渲染用户职业分布图表
    renderOccupationChart() {
      const chartDom = this.$refs.occupationChart
      if (!chartDom) return
      
      if (!this.occupationChart) {
        this.occupationChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.occupationDistribution || this.portraitData.occupationDistribution.length === 0) {
        const option = {
          title: {
            text: '暂无职业分布数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.occupationChart.setOption(option)
        return
      }
      
      // 处理数据
      const data = this.portraitData.occupationDistribution.map(item => ({
        value: item.count,
        name: item.occupation
      }))
      
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
            name: '职业分布',
            type: 'pie',
            radius: '70%',
            center: ['40%', '50%'],
            data: top10,
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
      
      this.occupationChart.setOption(option)
    },
    
    // 渲染用户地域分布图表
    renderRegionChart() {
      const chartDom = this.$refs.regionChart
      if (!chartDom) return
      
      if (!this.regionChart) {
        this.regionChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.regionDistribution || Object.keys(this.portraitData.regionDistribution).length === 0) {
        const option = {
          title: {
            text: '暂无地域分布数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.regionChart.setOption(option)
        return
      }
      
      // 处理数据
      const data = []
      const provinces = Object.keys(this.portraitData.regionDistribution)
      provinces.forEach(province => {
        if (province !== 'count') {
          data.push({
            name: province,
            value: this.portraitData.regionDistribution[province]
          })
        }
      })
      
      // 按数量排序并取前10
      data.sort((a, b) => b.value - a.value)
      const top10 = data.slice(0, 10)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}: {c}人'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '用户数',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        yAxis: {
          type: 'category',
          data: top10.map(item => item.name),
          axisLabel: {
            interval: 0
          }
        },
        series: [
          {
            name: '用户数量',
            type: 'bar',
            data: top10.map(item => item.value),
            itemStyle: {
              color: '#5470c6'
            }
          }
        ]
      }
      
      this.regionChart.setOption(option)
    },
    
    // 渲染用户消费能力分布图表
    renderConsumptionChart() {
      const chartDom = this.$refs.consumptionChart
      if (!chartDom) return
      
      if (!this.consumptionChart) {
        this.consumptionChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.consumptionLevelDistribution || this.portraitData.consumptionLevelDistribution.length === 0) {
        const option = {
          title: {
            text: '暂无消费能力数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.consumptionChart.setOption(option)
        return
      }
      
      // 处理数据
      const data = this.portraitData.consumptionLevelDistribution.map(item => ({
        value: item.count,
        name: item.level
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '消费能力',
            type: 'pie',
            radius: ['30%', '70%'],
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
            data: data
          }
        ]
      }
      
      this.consumptionChart.setOption(option)
    },
    
    // 渲染用户消费偏好图表
    renderPreferenceChart() {
      console.log('渲染消费偏好图表，数据:', this.portraitData.consumptionPreferences)
      const chartDom = this.$refs.preferenceChart
      if (!chartDom) return
      
      if (!this.preferenceChart) {
        this.preferenceChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.consumptionPreferences || this.portraitData.consumptionPreferences.length === 0) {
        const option = {
          title: {
            text: '暂无消费偏好数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.preferenceChart.setOption(option)
        return
      }
      // 处理数据
      const data = this.portraitData.consumptionPreferences
        .filter(item => item && (item.percentage !== undefined && item.percentage !== null) && item.category)
        .map(item => ({
          value: parseFloat(item.percentage) || 0,
          name: item.category || '未知类别'
        }))
      
      // 如果处理后的数据为空
      if (data.length === 0) {
        const option = {
          title: {
            text: '暂无有效的消费偏好数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.preferenceChart.setOption(option)
        return
      }
      
      // 按百分比排序
      data.sort((a, b) => b.value - a.value)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}: {c}%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: '占比(%)',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        yAxis: {
          type: 'category',
          data: data.map(item => item.name),
          axisLabel: {
            interval: 0
          }
        },
        series: [
          {
            name: '消费偏好',
            type: 'bar',
            data: data.map(item => item.value),
            itemStyle: {
              color: '#91cc75'
            }
          }
        ]
      }
      
      this.preferenceChart.setOption(option)
    },
    
    // 渲染用户设备分布图表
    renderDeviceChart() {
      console.log('渲染设备分布图表，数据:', this.portraitData.deviceDistribution)
      const chartDom = this.$refs.deviceChart
      if (!chartDom) return
      
      if (!this.deviceChart) {
        this.deviceChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.deviceDistribution || this.portraitData.deviceDistribution.length === 0) {
        const option = {
          title: {
            text: '暂无设备分布数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.deviceChart.setOption(option)
        return
      }
      // 处理数据
      const data = this.portraitData.deviceDistribution
        .filter(item => item && (item.count !== undefined && item.count !== null) && item.device)
        .map(item => ({
          value: parseInt(item.count) || 0,
          name: item.device || '未知设备'
        }))
      
      // 如果处理后的数据为空
      if (data.length === 0) {
        const option = {
          title: {
            text: '暂无有效的设备分布数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.deviceChart.setOption(option)
        return
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '设备分布',
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
      
      this.deviceChart.setOption(option)
    },
    
    // 渲染用户活跃时段分布图表
    renderActiveTimeChart() {
      console.log('渲染活跃时段图表，数据:', this.portraitData.activeTimeDistribution)
      const chartDom = this.$refs.activeTimeChart
      if (!chartDom) return
      
      if (!this.activeTimeChart) {
        this.activeTimeChart = echarts.init(chartDom)
      }
      
      // 检查数据是否存在
      if (!this.portraitData.activeTimeDistribution || this.portraitData.activeTimeDistribution.length === 0) {
        const option = {
          title: {
            text: '暂无活跃时段数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999',
              fontSize: 14
            }
          }
        }
        this.activeTimeChart.setOption(option)
        return
      }
      
      // 处理数据
      const hours = Array.from({length: 24}, (_, i) => i)
      const data = hours.map(hour => {
        const item = this.portraitData.activeTimeDistribution.find(d => parseInt(d.hour) === hour)
        return item ? (item.count || 0) : 0
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
          name: '活跃度',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: [
          {
            name: '活跃度',
            type: 'line',
            data: data,
            smooth: true,
            symbol: 'circle',
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
      
      this.activeTimeChart.setOption(option)
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
  margin-bottom: 30px;
}

.overview-card {
  height: 160px;
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 15px 0 10px;
}

.overview-info {
  font-size: 14px;
  color: #909399;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin: 30px 0 15px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
  line-height: 1.2;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 400px;
  transition: all 0.3s;
}

.chart-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart {
  width: 100%;
  height: 320px;
}
</style> 