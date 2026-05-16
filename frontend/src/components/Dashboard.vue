<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon user-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon loan-icon">
            <i class="el-icon-edit"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalApplications }}</div>
            <div class="stat-label">申请总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon approve-icon">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.approvedApplications }}</div>
            <div class="stat-label">已通过申请</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon risk-icon">
            <i class="el-icon-alert"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.highRiskApplications }}</div>
            <div class="stat-label">高风险申请</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card title="待审核申请">
          <el-table :data="pendingApplications" border>
            <el-table-column prop="id" label="申请ID" width="80" />
            <el-table-column prop="userName" label="申请人" width="100" />
            <el-table-column prop="requestedAmount" label="申请金额" width="120" />
            <el-table-column prop="riskLevel" label="风险等级" width="100" />
            <el-table-column prop="applicationDate" label="申请日期" width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="即将到期还款">
          <el-table :data="upcomingRepayments" border>
            <el-table-column prop="id" label="计划ID" width="80" />
            <el-table-column prop="installmentNumber" label="期数" width="80" />
            <el-table-column prop="totalAmount" label="还款金额" width="100" />
            <el-table-column prop="dueDate" label="到期日期" width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        totalUsers: 0,
        totalApplications: 0,
        approvedApplications: 0,
        highRiskApplications: 0
      },
      pendingApplications: [],
      upcomingRepayments: []
    }
  },
  mounted() {
    this.loadStats()
    this.loadPendingApplications()
    this.loadUpcomingRepayments()
  },
  methods: {
    async loadStats() {
      try {
        const [usersRes, loansRes, highRiskRes] = await Promise.all([
          axios.get('/api/users'),
          axios.get('/api/loans'),
          axios.get('/api/loans/high-risk')
        ])
        this.stats.totalUsers = usersRes.data.length
        this.stats.totalApplications = loansRes.data.length
        this.stats.approvedApplications = loansRes.data.filter(l => l.status === 'APPROVED' || l.status === 'DISBURSED').length
        this.stats.highRiskApplications = highRiskRes.data.length
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    async loadPendingApplications() {
      try {
        const res = await axios.get('/api/loans')
        this.pendingApplications = res.data.filter(l => l.status === 'PENDING' || l.status === 'UNDER_REVIEW').slice(0, 5)
      } catch (error) {
        console.error('加载待审核申请失败:', error)
      }
    },
    async loadUpcomingRepayments() {
      try {
        const res = await axios.get('/api/repayments/user/1')
        this.upcomingRepayments = res.data.filter(r => r.status === 'PENDING').slice(0, 5)
      } catch (error) {
        console.error('加载到期还款失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-right: 20px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.loan-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.approve-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.risk-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>