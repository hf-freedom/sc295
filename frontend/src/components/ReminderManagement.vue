<template>
  <div class="reminder-management">
    <el-row>
      <el-col :span="24">
        <div class="filter-bar">
          <el-select v-model="typeFilter" placeholder="类型筛选">
            <el-option label="全部" value="" />
            <el-option label="到期提醒" value="DUE_SOON" />
            <el-option label="逾期提醒" value="OVERDUE" />
          </el-select>
          <el-button type="primary" @click="loadReminders" style="margin-left: 20px;">刷新</el-button>
        </div>
      </el-col>
    </el-row>
    
    <el-row>
      <el-col :span="24">
        <el-card title="提醒列表">
          <el-table :data="filteredReminders" border>
            <el-table-column prop="id" label="提醒ID" width="80" />
            <el-table-column prop="planId" label="计划ID" width="80" />
            <el-table-column prop="type" label="类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="getTypeTag(scope.row.type)">
                  {{ getTypeText(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="内容" />
            <el-table-column prop="reminderDate" label="提醒日期" width="120" />
            <el-table-column prop="sent" label="发送状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.sent ? 'success' : 'warning'">
                  {{ scope.row.sent ? '已发送' : '未发送' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template slot-scope="scope">
                <el-button v-if="!scope.row.sent" size="small" type="success" @click="markAsSent(scope.row.id)">标记发送</el-button>
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
  name: 'ReminderManagement',
  data() {
    return {
      reminders: [],
      typeFilter: ''
    }
  },
  computed: {
    filteredReminders() {
      if (!this.typeFilter) {
        return this.reminders
      }
      return this.reminders.filter(r => r.type === this.typeFilter)
    }
  },
  mounted() {
    this.loadReminders()
  },
  methods: {
    async loadReminders() {
      try {
        const res = await axios.get('/api/reminders')
        this.reminders = res.data
      } catch (error) {
        this.$message.error('加载提醒失败')
      }
    },
    async markAsSent(id) {
      try {
        await axios.put(`/api/reminders/${id}/mark-sent`)
        this.loadReminders()
        this.$message.success('标记成功')
      } catch (error) {
        this.$message.error('标记失败')
      }
    },
    getTypeTag(type) {
      const types = {
        DUE_SOON: 'info',
        OVERDUE: 'danger',
        RECOVERY: 'warning'
      }
      return types[type] || 'default'
    },
    getTypeText(type) {
      const texts = {
        DUE_SOON: '到期提醒',
        OVERDUE: '逾期提醒',
        RECOVERY: '催收提醒'
      }
      return texts[type] || type
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