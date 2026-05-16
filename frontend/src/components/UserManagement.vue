<template>
  <div class="user-management">
    <el-row>
      <el-col :span="24">
        <el-card>
          <div class="card-header">
            <h2>用户管理</h2>
            <el-button type="primary" @click="showAddModal = true">新增用户</el-button>
          </div>
          
          <el-table :data="users" border>
            <el-table-column prop="id" label="用户ID" width="80" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="creditScore" label="信用分" width="100" />
            <el-table-column prop="incomeLevel" label="收入等级" width="100" />
            <el-table-column prop="totalCreditLimit" label="授信额度" width="120" />
            <el-table-column prop="usedCreditLimit" label="已用额度" width="120" />
            <el-table-column prop="availableCreditLimit" label="可用额度" width="120" />
            <el-table-column prop="blocked" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.blocked ? 'danger' : 'success'">
                  {{ scope.row.blocked ? '已限制' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="overdueCount" label="逾期次数" width="100" />
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteUser(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="editUserForm.id ? '编辑用户' : '新增用户'" :visible.sync="showAddModal">
      <el-form :model="editUserForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editUserForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editUserForm.phone" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input type="number" v-model="editUserForm.creditScore" />
        </el-form-item>
        <el-form-item label="收入等级">
          <el-select v-model="editUserForm.incomeLevel">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      showAddModal: false,
      editUserForm: {
        id: null,
        name: '',
        phone: '',
        creditScore: 600,
        incomeLevel: 'C'
      }
    }
  },
  mounted() {
    this.loadUsers()
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
    editUser(user) {
      this.editUserForm = {
        id: user.id,
        name: user.name,
        phone: user.phone,
        creditScore: user.creditScore,
        incomeLevel: user.incomeLevel
      }
      this.showAddModal = true
    },
    async saveUser() {
      try {
        if (this.editUserForm.id) {
          await axios.put(`/api/users/${this.editUserForm.id}`, {
            name: this.editUserForm.name,
            phone: this.editUserForm.phone,
            incomeLevel: this.editUserForm.incomeLevel
          })
        } else {
          await axios.post('/api/users', this.editUserForm)
        }
        this.showAddModal = false
        this.loadUsers()
        this.$message.success('保存成功')
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    async deleteUser(id) {
      try {
        await axios.delete(`/api/users/${id}`)
        this.loadUsers()
        this.$message.success('删除成功')
      } catch (error) {
        this.$message.error('删除失败')
      }
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}
</style>