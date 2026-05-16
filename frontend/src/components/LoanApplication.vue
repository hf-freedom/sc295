<template>
  <div class="loan-application">
    <el-row>
      <el-col :span="12">
        <el-card title="借款申请">
          <el-form :model="applicationForm" label-width="100px">
            <el-form-item label="选择用户">
              <el-select v-model="applicationForm.userId" placeholder="请选择用户">
                <el-option v-for="user in users" :key="user.id" :label="`${user.name} (${user.phone})`" :value="user.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="申请金额">
              <el-input type="number" v-model="applicationForm.requestedAmount" />
            </el-form-item>
            <el-form-item v-if="selectedUser">
              <span class="info-text">可用额度: {{ selectedUser.availableCreditLimit }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitApplication">提交申请</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="我的申请记录">
          <el-table :data="applications" border>
            <el-table-column prop="id" label="申请ID" width="80" />
            <el-table-column prop="requestedAmount" label="申请金额" width="120" />
            <el-table-column prop="approvedAmount" label="批准金额" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险等级" width="100" />
            <el-table-column prop="applicationDate" label="申请日期" width="120" />
            <el-table-column label="操作" width="160">
              <template slot-scope="scope">
                <el-button v-if="scope.row.status === 'APPROVED'" size="small" type="success" @click="disburseLoan(scope.row.id)">放款</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoanApplication',
  data() {
    return {
      users: [],
      applications: [],
      applicationForm: {
        userId: null,
        requestedAmount: null
      }
    }
  },
  computed: {
    selectedUser() {
      return this.users.find(u => u.id === this.applicationForm.userId)
    }
  },
  mounted() {
    this.loadUsers()
    this.loadApplications()
  },
  methods: {
    async loadUsers() {
      try {
        const res = await axios.get('/api/users')
        this.users = res.data.filter(u => !u.blocked)
      } catch (error) {
        this.$message.error('加载用户失败')
      }
    },
    async loadApplications() {
      try {
        const res = await axios.get('/api/loans')
        this.applications = res.data
      } catch (error) {
        this.$message.error('加载申请失败')
      }
    },
    async submitApplication() {
      if (!this.applicationForm.userId || !this.applicationForm.requestedAmount) {
        this.$message.warning('请填写完整信息')
        return
      }
      try {
        await axios.post('/api/loans', this.applicationForm)
        this.$message.success('申请提交成功')
        this.applicationForm = { userId: null, requestedAmount: null }
        this.loadApplications()
      } catch (error) {
        this.$message.error(error.response?.data?.message || '提交失败')
      }
    },
    async disburseLoan(id) {
      try {
        await axios.post(`/api/loans/${id}/disburse`)
        this.$message.success('放款成功')
        this.loadApplications()
      } catch (error) {
        this.$message.error('放款失败')
      }
    },
    getStatusType(status) {
      const types = {
        PENDING: 'warning',
        UNDER_REVIEW: 'info',
        APPROVED: 'success',
        REJECTED: 'danger',
        DISBURSED: 'primary',
        COMPLETED: 'success'
      }
      return types[status] || 'default'
    },
    getStatusText(status) {
      const texts = {
        PENDING: '待审核',
        UNDER_REVIEW: '人工复核',
        APPROVED: '已通过',
        REJECTED: '已拒绝',
        DISBURSED: '已放款',
        COMPLETED: '已完成'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.info-text {
  color: #20a0ff;
  font-weight: bold;
}
</style>