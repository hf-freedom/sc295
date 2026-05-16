<template>
  <div class="app-container">
    <el-container>
      <el-header class="header">
        <h1>借款额度审批系统</h1>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu :default-active="activeMenu" class="menu" @select="handleMenuSelect">
            <el-menu-item index="/">
              <i class="el-icon-menu"></i>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/loans/apply">
              <i class="el-icon-edit"></i>
              <span>借款申请</span>
            </el-menu-item>
            <el-menu-item index="/loans/review">
              <i class="el-icon-search"></i>
              <span>审核管理</span>
            </el-menu-item>
            <el-menu-item index="/repayments">
              <i class="el-icon-money"></i>
              <span>还款管理</span>
            </el-menu-item>
            <el-menu-item index="/reminders">
              <i class="el-icon-bell"></i>
              <span>提醒管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      activeMenu: '/'
    }
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
      this.$router.push(index)
    }
  },
  created() {
    this.activeMenu = this.$route.path
  },
  watch: {
    '$route.path'(newPath) {
      this.activeMenu = newPath
    }
  }
}
</script>

<style>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #20a0ff;
  color: white;
  text-align: center;
  line-height: 60px;
  padding: 0;
}

.header h1 {
  margin: 0;
  font-size: 20px;
}

.aside {
  background-color: #303133;
}

.menu {
  height: 100%;
  border-right: none;
}

.main {
  padding: 20px;
  overflow-y: auto;
}
</style>