<template>
  <div class="loan-review">
    <el-row>
      <el-col :span="24">
        <div class="filter-bar">
          <el-button type="primary" @click="autoReview">自动审核</el-button>
          <el-select v-model="statusFilter" placeholder="状态筛选" style="margin-left: 20px;">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="人工复核" value="UNDER_REVIEW" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已放款" value="DISBURSED" />
          </el-select>
        </div>
      </el-col>
    </el-row>
    
    <el-row>
      <el-col :span="12">
        <el-card title="待审核列表">
          <el-table :data="pendingApplications" border>
            <el-table-column prop="id" label="申请ID" width="80" />
            <el-table-column prop="userName" label="申请人" width="100" />
            <el-table-column prop="requestedAmount" label="申请金额" width="120" />
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template slot-scope="scope">
                <el-tag :type="getRiskType(scope.row.riskLevel)">
                  {{ scope.row.riskLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicationDate" label="申请日期" width="120" />
            <el-table-column label="操作" width="160">
              <template slot-scope="scope">
                <el-button size="small" type="success" @click="reviewApplication(scope.row.id, true)">通过</el-button>
                <el-button size="small" type="danger" @click="showRejectModal(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="高风险申请（需人工复核）">
          <el-table :data="highRiskApplications" border>
            <el-table-column prop="id" label="申请ID" width="80" />
            <el-table-column prop="userName" label="申请人" width="100" />
            <el-table-column prop="requestedAmount" label="申请金额" width="120" />
            <el-table-column prop="riskLevel" label="风险等级" width="100" />
            <el-table-column prop="applicationDate" label="申请日期" width="120" />
            <el-table-column label="操作" width="160">
              <template slot-scope="scope">
                <el-button size="small" type="success" @click="reviewApplication(scope.row.id, true)">通过</el-button>
                <el-button size="small" type="danger" @click="showRejectModal(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="拒绝申请" :visible.sync="showReject">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因">
          <el-textarea v-model="rejectForm.reason" rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showReject = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoanReview',
  data() {
    return {
      statusFilter: '',
      applications: [],
      showReject: false,
      rejectForm: {
        applicationId: null,
        reason: ''
      }
    }
  },
  computed: {
    pendingApplications() {
      return this.applications.filter(a => a.status === 'PENDING')
    },
    highRiskApplications() {
      return this.applications.filter(a => a.highRisk && !a.manualReviewed)
    }
  },
  mounted() {
    this.loadApplications()
  },
  methods: {
    async loadApplications() {
      try {
        const res = await axios.get('/api/loans')
        this.applications = res.data
      } catch (error) {
        this.$message.error('加载申请失败')
      }
    },
    async autoReview() {
      try {
        await axios.post('/api/loans/auto-review')
        this.$message.success('自动审核完成')
        this.loadApplications()
      } catch (error) {
        this.$message.error('自动审核失败')
      }
    },
    showRejectModal(application) {
      this.rejectForm = {
        applicationId: application.id,
        reason: ''
      }
      this.showReject = true
    },
    async confirmReject() {
      if (!this.rejectForm.reason) {
        this.$message.warning('请填写拒绝原因')
        return
      }
      try {
        await axios.post(`/api/loans/${this.rejectForm.applicationId}/review`, {
          approve: false,
          reason: this.rejectForm.reason
        })
        this.showReject = false
        this.loadApplications()
        this.$message.success('操作成功')
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async reviewApplication(id, approve) {
      try {
        await axios.post(`/api/loans/${id}/review`, {
          approve: approve,
          reason: approve ? '审核通过' : '审核拒绝'
        })
        this.loadApplications()
        this.$message.success(approve ? '已通过' : '已拒绝')
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    getRiskType(riskLevel) {
      const types = {
        LOW: 'success',
        MEDIUM: 'warning',
        HIGH: 'danger'
      }
      return types[riskLevel] || 'default'
    }
  }
}
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
</style>