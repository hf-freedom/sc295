<template>
  <div class="repayment-management">
    <el-row>
      <el-col :span="24">
        <div class="filter-bar">
          <el-select v-model="selectedUser" placeholder="选择用户">
            <el-option v-for="user in users" :key="user.id" :label="`${user.name} (${user.phone})`" :value="user.id" />
          </el-select>
          <el-button type="primary" @click="processOverdue" style="margin-left: 20px;">处理逾期</el-button>
          <el-button type="success" @click="recoverCredit" style="margin-left: 10px;">恢复额度</el-button>
        </div>
      </el-col>
    </el-row>
    
    <el-row>
      <el-col :span="12">
        <el-card title="还款计划列表">
          <el-table :data="repaymentPlans" border>
            <el-table-column prop="id" label="计划ID" width="80" />
            <el-table-column prop="installmentNumber" label="期数" width="80" />
            <el-table-column prop="dueDate" label="到期日期" width="120" />
            <el-table-column prop="principalAmount" label="本金" width="100" />
            <el-table-column prop="interestAmount" label="利息" width="100" />
            <el-table-column prop="totalAmount" label="还款金额" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button v-if="scope.row.status === 'PENDING'" size="small" type="success" @click="showPayModal(scope.row)">还款</el-button>
                <el-button v-if="scope.row.status === 'PENDING'" size="small" type="primary" @click="showEarlyPayModal(scope.row)">提前还款</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="逾期记录">
          <el-table :data="overduePlans" border>
            <el-table-column prop="id" label="计划ID" width="80" />
            <el-table-column prop="installmentNumber" label="期数" width="80" />
            <el-table-column prop="dueDate" label="到期日期" width="120" />
            <el-table-column prop="totalAmount" label="逾期金额" width="120" />
            <el-table-column prop="status" label="状态" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="payForm.earlyRepayment ? '提前还款' : '还款'" :visible.sync="showPay">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="期数">
          <span>{{ payForm.installmentNumber }}期</span>
        </el-form-item>
        <el-form-item label="还款金额">
          <el-input type="number" v-model="payForm.amount" :disabled="!payForm.earlyRepayment" />
        </el-form-item>
        <el-form-item v-if="payForm.earlyRepayment">
          <span class="info-text">提前还款可享受5%利息减免</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showPay = false">取消</el-button>
        <el-button type="primary" @click="confirmPayment">确认还款</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RepaymentManagement',
  data() {
    return {
      users: [],
      selectedUser: null,
      repaymentPlans: [],
      showPay: false,
      payForm: {
        planId: null,
        installmentNumber: null,
        amount: null,
        earlyRepayment: false
      }
    }
  },
  computed: {
    overduePlans() {
      return this.repaymentPlans.filter(p => p.status === 'OVERDUE')
    }
  },
  mounted() {
    this.loadUsers()
  },
  watch: {
    selectedUser() {
      if (this.selectedUser) {
        this.loadRepaymentPlans()
      }
    }
  },
  methods: {
    async loadUsers() {
      try {
        const res = await axios.get('/api/users')
        this.users = res.data
      } catch (error) {
        this.$message.error('加载用户失败')
      }
    },
    async loadRepaymentPlans() {
      try {
        const res = await axios.get(`/api/repayments/user/${this.selectedUser}`)
        this.repaymentPlans = res.data
      } catch (error) {
        this.$message.error('加载还款计划失败')
      }
    },
    showPayModal(plan) {
      this.payForm = {
        planId: plan.id,
        installmentNumber: plan.installmentNumber,
        amount: plan.totalAmount,
        earlyRepayment: false
      }
      this.showPay = true
    },
    showEarlyPayModal(plan) {
      this.payForm = {
        planId: plan.id,
        installmentNumber: plan.installmentNumber,
        amount: plan.totalAmount,
        earlyRepayment: true
      }
      this.showPay = true
    },
    async confirmPayment() {
      try {
        await axios.post('/api/repayments/pay', {
          planId: this.payForm.planId,
          amount: this.payForm.amount,
          earlyRepayment: this.payForm.earlyRepayment
        })
        this.showPay = false
        this.loadRepaymentPlans()
        this.$message.success('还款成功')
      } catch (error) {
        this.$message.error(error.response?.data?.message || '还款失败')
      }
    },
    async processOverdue() {
      try {
        await axios.post('/api/repayments/process-overdue')
        this.$message.success('逾期处理完成')
        this.loadRepaymentPlans()
      } catch (error) {
        this.$message.error('处理失败')
      }
    },
    async recoverCredit() {
      try {
        await axios.post('/api/repayments/recover-credit')
        this.$message.success('额度恢复完成')
        this.loadUsers()
        if (this.selectedUser) {
          this.loadRepaymentPlans()
        }
      } catch (error) {
        this.$message.error('恢复失败')
      }
    },
    getStatusType(status) {
      const types = {
        PENDING: 'warning',
        PAID: 'success',
        OVERDUE: 'danger',
        EARLY_PAID: 'success'
      }
      return types[status] || 'default'
    },
    getStatusText(status) {
      const texts = {
        PENDING: '待还款',
        PAID: '已还款',
        OVERDUE: '已逾期',
        EARLY_PAID: '提前还款'
      }
      return texts[status] || status
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

.info-text {
  color: #20a0ff;
}
</style>