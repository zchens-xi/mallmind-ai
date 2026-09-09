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

    <!-- 数据质量警告 -->
    <el-alert
      v-if="behaviorData.dataQualityWarning"
      :title="behaviorData.dataQualityWarning"
      type="warning"
      :closable="false"
      show-icon
      style="margin: 10px 0;"
    >
    </el-alert>

    <!-- 数据概览 -->
    <div class="overview-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>销售总额</span> <!-- 数据来源：oms_order.pay_amount（已支付订单） -->
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
              <span>订单总数</span> <!-- 数据来源：oms_order（已支付订单数量） -->
            </div>
            <div class="overview-value">{{ formatNumber(overviewData.totalOrderCount) }}</div>
            <div class="overview-info">客单价：¥{{ formatNumber(overviewData.avgOrderAmount) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>访问量</span> <!-- 数据来源：ums_member_statistics_info.page_view -->
            </div>
            <div class="overview-value">{{ formatNumber(behaviorData.pageViews) }}</div>
            <div class="overview-info">访客数：{{ formatNumber(behaviorData.visitors) }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>转化率</span> <!-- 数据来源：计算得出（支付人数/浏览人数）-->
            </div>
            <div class="overview-value">{{ conversionRate.toFixed(2) }}%</div>
            <div class="overview-info">跳出率：{{ behaviorData.bounceRate ? behaviorData.bounceRate.toFixed(2) : 0 }}%</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据趋势 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>销售趋势</span> <!-- 数据来源：oms_order.pay_amount 按日/周/月分组 -->
              <el-radio-group v-model="salesTrendType" size="mini" style="float: right">
                <el-radio-button label="sales">销售额</el-radio-button>
                <el-radio-button label="orders">订单量</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart" ref="salesTrendChart"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>用户转化漏斗</span> <!-- 数据来源：oms_order.member_id（下单/支付），其余阶段建议去除或补充真实埋点 -->
            </div>
            <div class="chart" ref="funnelChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商品分析 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>热销商品排行</span> <!-- 数据来源：pms_product, oms_order_item, oms_order -->
            </div>
            <el-table :data="productRankData" style="width: 100%" size="mini" :show-header="false">
              <el-table-column width="40">
                <template slot-scope="scope">
                  <div class="rank-number" :class="{'top-rank': scope.$index < 3}">{{ scope.$index + 1 }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="productName" label="商品名称"></el-table-column>
              <el-table-column prop="salesAmount" label="销售额" width="120" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.salesAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品分类销售占比</span> <!-- 数据来源：pms_product_category, pms_product, oms_order_item, oms_order -->
            </div>
            <div class="chart" ref="categoryChart"></div>
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
  getUserBehaviorAnalytics,
  getProductAnalytics,
  getProductSalesRank,
  getConversionFunnel
} from '@/api/analytics'

export default {
  name: 'Dashboard',
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
        salesGrowthRate: 0
      },
      behaviorData: {
        pageViews: 0,
        visitors: 0,
        pageViewsPerVisitor: 0,
        bounceRate: 0,
        avgStayTime: 0,
        funnelData: [],
        dataQualityWarning: '' // 新增数据质量警告属性
      },
      productData: {
        productSalesRank: [],
        categorySalesPercent: [],
        productViewRank: [],
        productCollectRank: [],
        productConversionRank: []
      },
      productRankData: [],
      funnelData: [],
      
      // 图表实例
      salesTrendChart: null,
      funnelChart: null,
      categoryChart: null
    }
  },
  computed: {
    conversionRate() {
      // 确保使用正确的数据源计算转化率
      // 转化率 = (支付订单数 / 访问量) * 100
      const funnelData = this.behaviorData.funnelData || []
      if (!funnelData || funnelData.length === 0) {
        console.log('转化率计算：behaviorData.funnelData为空，返回0')
        return 0
      }
      
      // 查找支付阶段数据
      const payStage = funnelData.find(item => item.name === '支付')
      if (!payStage || !payStage.value) {
        console.log('转化率计算：无支付阶段数据，返回0')
        return 0
      }
      
      // 查找浏览商品阶段数据
      const viewStage = funnelData.find(item => item.name === '浏览商品')
      if (!viewStage || !viewStage.value || viewStage.value === 0) {
        console.log('转化率计算：无浏览商品阶段数据或值为0，返回0')
        return 0
      }
      
      // 计算转化率
      const rate = (payStage.value / viewStage.value) * 100
      console.log('转化率计算：', payStage.value, '/', viewStage.value, '=', rate, '%')
      return rate
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
    if (this.funnelChart) this.funnelChart.dispose()
    if (this.categoryChart) this.categoryChart.dispose()
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
      this.getBehaviorData()
      this.getProductData()
      this.getProductRankData()
      this.getFunnelData()
    },
    
    // 重新调整图表大小
    resizeCharts() {
      if (this.salesTrendChart) this.salesTrendChart.resize()
      if (this.funnelChart) this.funnelChart.resize()
      if (this.categoryChart) this.categoryChart.resize()
    },
    
    // 获取概览数据
    getOverviewData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        type: this.timeGranularity
      }
      
      console.log('获取销售数据分析参数:', params)
      console.log('API基础URL:', process.env.BASE_API)
      getSalesAnalytics(params).then(response => {
        console.log('销售数据分析响应:', response)
        if (response && response.data) {
          this.overviewData = response.data
          console.log('销售总额数据:', this.overviewData.totalSales)
          console.log('订单总数数据:', this.overviewData.totalOrderCount)
          console.log('销售趋势数据:', this.overviewData.salesList)
          this.renderSalesTrendChart()
        } else {
          console.error('销售数据分析响应为空或格式错误')
        }
      }).catch(error => {
        console.error('获取销售数据分析失败:', error)
      })
    },
    
    // 获取用户行为数据
    getBehaviorData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      console.log('获取用户行为数据参数:', params)
      getUserBehaviorAnalytics(params).then(response => {
        console.log('用户行为数据响应:', response)
        if (response && response.data) {
          this.behaviorData = response.data
          console.log('访问量数据:', this.behaviorData.pageViews)
          console.log('访客数数据:', this.behaviorData.visitors)
          console.log('跳出率数据:', this.behaviorData.bounceRate)
          console.log('转化漏斗数据:', this.behaviorData.funnelData)
          
          // 获取数据后渲染转化漏斗图
          this.$nextTick(() => {
            this.renderFunnelChart()
          })
        } else {
          console.error('用户行为数据响应为空或格式错误')
        }
      }).catch(error => {
        console.error('获取用户行为数据失败:', error)
      })
    },
    
    // 获取商品分析数据
    getProductData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      console.log('获取商品分析数据参数:', params)
      getProductAnalytics(params).then(response => {
        console.log('商品分析数据响应:', response)
        if (response && response.data) {
          this.productData = response.data
          console.log('商品分类销售占比数据:', this.productData.categorySalesPercent)
          this.renderCategoryChart()
        } else {
          console.error('商品分析数据响应为空或格式错误')
        }
      }).catch(error => {
        console.error('获取商品分析数据失败:', error)
      })
    },
    
    // 获取商品销售排行
    getProductRankData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        limit: 10
      }
      
      console.log('获取商品销售排行参数:', params)
      getProductSalesRank(params).then(response => {
        console.log('商品销售排行响应:', response)
        if (response && response.data) {
          this.productRankData = response.data
          console.log('热销商品排行数据:', this.productRankData)
        } else {
          console.error('商品销售排行响应为空或格式错误')
        }
      }).catch(error => {
        console.error('获取商品销售排行失败:', error)
      })
    },
    
    // 获取转化漏斗数据
    getFunnelData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      console.log('获取转化漏斗数据参数:', params)
      getConversionFunnel(params).then(response => {
        console.log('转化漏斗数据响应:', response)
        if (response && response.data) {
          this.funnelData = response.data
          console.log('转化漏斗数据:', this.funnelData)
          this.renderFunnelChart()
        } else {
          console.error('转化漏斗数据响应为空或格式错误')
        }
      }).catch(error => {
        console.error('获取转化漏斗数据失败:', error)
      })
    },
    
    // 渲染销售趋势图表
    renderSalesTrendChart() {
      // 检查数据是否存在
      if (!this.overviewData || !this.overviewData.dateList || !this.overviewData.dateList.length) {
        console.error('渲染销售趋势图表：缺少必要的数据')
        return
      }
      
      const chartDom = this.$refs.salesTrendChart
      if (!chartDom) {
        console.error('渲染销售趋势图表：找不到图表DOM元素')
        return
      }
      
      if (!this.salesTrendChart) {
        this.salesTrendChart = echarts.init(chartDom)
      }
      
      const series = []
      let data = []
      const name = this.salesTrendType === 'sales' ? '销售额' : '订单量'
      
      // 确保数据存在
      if (this.salesTrendType === 'sales' && this.overviewData.salesList) {
        data = this.overviewData.salesList
        console.log('销售额趋势数据:', data)
      } else if (this.salesTrendType === 'orders' && this.overviewData.orderCountList) {
        data = this.overviewData.orderCountList
        console.log('订单量趋势数据:', data)
      } else {
        console.error('渲染销售趋势图表：缺少' + name + '数据')
        return
      }
      
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
      
      try {
        this.salesTrendChart.setOption(option)
        console.log('销售趋势图表渲染成功')
      } catch (error) {
        console.error('渲染销售趋势图表失败:', error)
      }
    },
    
    // 渲染转化漏斗图表
    renderFunnelChart() {
      // 使用behaviorData中的funnelData
      const funnelData = this.behaviorData.funnelData || []
      // 检查数据是否存在
      if (!funnelData || funnelData.length === 0) {
        console.error('渲染转化漏斗图表：缺少必要的数据')
        return
      }
      
      const chartDom = this.$refs.funnelChart
      if (!chartDom) {
        console.error('渲染转化漏斗图表：找不到图表DOM元素')
        return
      }
      
      if (!this.funnelChart) {
        this.funnelChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = funnelData.map(item => ({
        value: item.value,
        name: item.name
      }))
      
      console.log('转化漏斗图表数据:', data)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}'
        },
        legend: {
          data: data.map(item => item.name),
          bottom: 0
        },
        series: [
          {
            name: '转化漏斗',
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
              position: 'inside'
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
                fontSize: 14
              }
            },
            data: data
          }
        ]
      }
      
      try {
        this.funnelChart.setOption(option)
        console.log('转化漏斗图表渲染成功')
      } catch (error) {
        console.error('渲染转化漏斗图表失败:', error)
      }
    },
    
    // 渲染商品分类销售占比图表
    renderCategoryChart() {
      // 检查数据是否存在
      if (!this.productData || !this.productData.categorySalesPercent || this.productData.categorySalesPercent.length === 0) {
        console.error('渲染商品分类销售占比图表：缺少必要的数据')
        return
      }
      
      const chartDom = this.$refs.categoryChart
      if (!chartDom) {
        console.error('渲染商品分类销售占比图表：找不到图表DOM元素')
        return
      }
      
      if (!this.categoryChart) {
        this.categoryChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = this.productData.categorySalesPercent.map(item => ({
        value: item.salesAmount,
        name: item.categoryName
      }))
      
      console.log('商品分类销售占比图表数据:', data)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '销售额',
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
      
      try {
        this.categoryChart.setOption(option)
        console.log('商品分类销售占比图表渲染成功')
      } catch (error) {
        console.error('渲染商品分类销售占比图表失败:', error)
      }
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
  height: 400px;
}

.chart {
  width: 100%;
  height: 320px;
}

.rank-number {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background-color: #E6E6E6;
  color: #606266;
  border-radius: 2px;
}

.top-rank {
  background-color: #fa436a;
  color: #fff;
}
</style> 