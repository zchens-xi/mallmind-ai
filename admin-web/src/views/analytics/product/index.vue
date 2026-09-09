<template>
  <div class="app-container">
    <!-- 日期选择 -->


    <!-- 商品概览 -->
    <div class="overview-container">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>商品总数</span>
            </div>
            <div class="overview-value">{{ formatNumber(productData.totalProducts) }}</div>
            <div class="overview-info">在售商品：{{ formatNumber(productData.onSaleProducts) }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>商品浏览量</span>
            </div>
            <div class="overview-value">{{ formatNumber(productData.totalViews) }}</div>
            <div class="overview-info">人均浏览：{{ formatNumber(productData.viewsPerUser) }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" :body-style="{ padding: '20px' }">
            <div slot="header" class="clearfix">
              <span>商品收藏量</span>
            </div>
            <div class="overview-value">{{ formatNumber(productData.totalCollects) }}</div>
            <div class="overview-info">收藏率：{{ (productData.collectRate || 0).toFixed(2) }}%</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商品销售排行和分类占比 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品销售排行</span>
              <el-radio-group v-model="salesRankType" size="mini" style="float: right">
                <el-radio-button label="amount">销售额</el-radio-button>
                <el-radio-button label="quantity">销售量</el-radio-button>
              </el-radio-group>
            </div>
            <el-table :data="productSalesRank" style="width: 100%" size="mini">
              <el-table-column width="60">
                <template slot-scope="scope">
                  <div class="rank-number" :class="{'top-rank': scope.$index < 3}">{{ scope.$index + 1 }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="productName" label="商品名称"></el-table-column>
              <el-table-column :prop="salesRankType === 'amount' ? 'salesAmount' : 'salesQuantity'" :label="salesRankType === 'amount' ? '销售额' : '销售量'" width="120" align="right">
                <template slot-scope="scope">
                  <span v-if="salesRankType === 'amount'">¥{{ formatNumber(scope.row.salesAmount) }}</span>
                  <span v-else>{{ formatNumber(scope.row.salesQuantity) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品分类销售占比</span>
            </div>
            <div class="chart" ref="categoryChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商品浏览排行和商品转化率 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品浏览排行</span>
            </div>
            <el-table :data="productViewRank" style="width: 100%" size="mini">
              <el-table-column width="60">
                <template slot-scope="scope">
                  <div class="rank-number" :class="{'top-rank': scope.$index < 3}">{{ scope.$index + 1 }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="productName" label="商品名称"></el-table-column>
              <el-table-column prop="viewCount" label="浏览量" width="100" align="right"></el-table-column>
              <el-table-column prop="uniqueVisitors" label="访客数" width="100" align="right"></el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品转化率排行</span>
            </div>
            <el-table :data="productConversionRank" style="width: 100%" size="mini">
              <el-table-column width="60">
                <template slot-scope="scope">
                  <div class="rank-number" :class="{'top-rank': scope.$index < 3}">{{ scope.$index + 1 }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="productName" label="商品名称"></el-table-column>
              <el-table-column prop="conversionRate" label="转化率" width="100" align="right">
                <template slot-scope="scope">
                  {{ scope.row.conversionRate.toFixed(2) }}%
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 商品价格区间分布和商品库存分布 -->
    <div class="chart-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品价格区间分布</span>
            </div>
            <div class="chart" ref="priceRangeChart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <div slot="header" class="clearfix">
              <span>商品库存分布</span>
            </div>
            <div class="chart" ref="stockChart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getProductAnalytics,
  getProductSalesRank,
  getProductViewRank,
  getProductConversionRank,
  getProductPriceDistribution,
  getProductStockDistribution
} from '@/api/analytics'

export default {
  name: 'ProductAnalytics',
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
      salesRankType: 'amount',

      // 数据
      productData: {
        totalProducts: 0,
        onSaleProducts: 0,
        totalViews: 0,
        viewsPerUser: 0,
        totalCollects: 0,
        collectRate: 0
      },
      productSalesRank: [],
      productViewRank: [],
      productConversionRank: [],
      categorySalesPercent: [],
      priceDistribution: [],
      stockDistribution: [],
      
      // 图表实例
      categoryChart: null,
      priceRangeChart: null,
      stockChart: null
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
    if (this.categoryChart) this.categoryChart.dispose()
    if (this.priceRangeChart) this.priceRangeChart.dispose()
    if (this.stockChart) this.stockChart.dispose()
  },
  methods: {
    // 格式化数字，添加千分位
    formatNumber(num) {
      if (!num && num !== 0) return '--'
      return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    },
    
    // 获取所有数据
    getData() {
      this.getProductData()
      this.getProductSalesRankData()
      this.getProductViewRankData()
      this.getProductConversionRankData()
      this.getProductPriceDistribution()
      this.getProductStockDistribution()
    },
    
    // 重新调整图表大小
    resizeCharts() {
      if (this.categoryChart) this.categoryChart.resize()
      if (this.priceRangeChart) this.priceRangeChart.resize()
      if (this.stockChart) this.stockChart.resize()
    },
    
    // 获取商品概览数据
    getProductData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1])
      }
      
      getProductAnalytics(params).then(response => {
        this.productData = response.data
        this.categorySalesPercent = response.data.categorySalesPercent || []
        this.renderCategoryChart()
      })
    },
    
    // 获取商品销售排行
    getProductSalesRankData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        limit: 10
      }
      
      getProductSalesRank(params).then(response => {
        this.productSalesRank = response.data
      })
    },
    
    // 获取商品浏览排行
    getProductViewRankData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        limit: 10
      }
      
      getProductViewRank(params).then(response => {
        this.productViewRank = response.data
      })
    },
    
    // 获取商品转化率排行
    getProductConversionRankData() {
      const params = {
        startDate: this.formatDate(this.dateRange[0]),
        endDate: this.formatDate(this.dateRange[1]),
        limit: 10
      }
      
      getProductConversionRank(params).then(response => {
        this.productConversionRank = response.data
      })
    },
    
    // 获取商品价格区间分布
    getProductPriceDistribution() {
      getProductPriceDistribution().then(response => {
        this.priceDistribution = response.data
        this.renderPriceRangeChart()
      })
    },
    
    // 获取商品库存分布
    getProductStockDistribution() {
      getProductStockDistribution().then(response => {
        this.stockDistribution = response.data
        this.renderStockChart()
      })
    },
    
    // 渲染商品分类销售占比图表
    renderCategoryChart() {
      if (!this.categorySalesPercent || this.categorySalesPercent.length === 0) return
      
      const chartDom = this.$refs.categoryChart
      if (!chartDom) return
      
      if (!this.categoryChart) {
        this.categoryChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const data = this.categorySalesPercent.map(item => ({
        value: item.salesAmount,
        name: item.categoryName
      }))
      
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
      
      this.categoryChart.setOption(option)
    },
    
    // 渲染商品价格区间分布图表
    renderPriceRangeChart() {
      if (!this.priceDistribution || this.priceDistribution.length === 0) return
      
      const chartDom = this.$refs.priceRangeChart
      if (!chartDom) return
      
      if (!this.priceRangeChart) {
        this.priceRangeChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const xData = this.priceDistribution.map(item => item.range)
      const yData = this.priceDistribution.map(item => item.count)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}: {c}件商品'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xData,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '商品数量',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: [
          {
            name: '商品数量',
            type: 'bar',
            data: yData,
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      
      this.priceRangeChart.setOption(option)
    },
    
    // 渲染商品库存分布图表
    renderStockChart() {
      if (!this.stockDistribution || this.stockDistribution.length === 0) return
      
      const chartDom = this.$refs.stockChart
      if (!chartDom) return
      
      if (!this.stockChart) {
        this.stockChart = echarts.init(chartDom)
      }
      
      // 处理数据
      const xData = this.stockDistribution.map(item => item.range)
      const yData = this.stockDistribution.map(item => item.count)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: '{b}: {c}件商品'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xData,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '商品数量',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        series: [
          {
            name: '商品数量',
            type: 'bar',
            data: yData,
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      
      this.stockChart.setOption(option)
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

.rank-number {
  width: 24px;
  height: 24px;
  line-height: 24px;
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