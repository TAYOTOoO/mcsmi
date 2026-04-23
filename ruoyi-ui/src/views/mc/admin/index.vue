<template>
  <div class="mc-admin-container">
    <div class="admin-header">
      <h2><i class="el-icon-setting"></i> 材质工坊管理后台</h2>
      <div class="admin-info">
        <span>管理员：{{ username }}</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="card" class="admin-tabs">
      <!-- Tab 1: AI模型管理 -->
      <el-tab-pane label="AI模型管理" name="model">
        <template slot="label">
          <span><i class="el-icon-cpu"></i> AI模型管理</span>
        </template>
        <ModelManage />
      </el-tab-pane>

      <!-- Tab 2: 用户管理 -->
      <el-tab-pane label="用户管理" name="user">
        <template slot="label">
          <span><i class="el-icon-user"></i> 用户管理</span>
        </template>
        <UserManage />
      </el-tab-pane>

      <!-- Tab 3: 调用日志 -->
      <el-tab-pane label="调用日志" name="log">
        <template slot="label">
          <span><i class="el-icon-document"></i> 调用日志</span>
        </template>
        <LogManage />
      </el-tab-pane>

      <!-- Tab 4: 预提示词模板 -->
      <el-tab-pane label="预提示词模板" name="prompt">
        <template slot="label">
          <span><i class="el-icon-edit-outline"></i> 预提示词模板</span>
        </template>
        <PromptManage />
      </el-tab-pane>

      <!-- Tab 5: 邮件配置 -->
      <el-tab-pane label="邮件配置" name="email">
        <template slot="label">
          <span><i class="el-icon-message"></i> 邮件配置</span>
        </template>
        <EmailConfig />
      </el-tab-pane>

      <!-- Tab 6: 用户端页面管理 -->
      <el-tab-pane label="用户端页面管理" name="userpage">
        <template slot="label">
          <span><i class="el-icon-setting"></i> 用户端页面管理</span>
        </template>
        <UserPageManage />
      </el-tab-pane>

      <!-- Tab 7: 公告管理 -->
      <el-tab-pane label="公告管理" name="announcement">
        <template slot="label">
          <span><i class="el-icon-bell"></i> 公告管理</span>
        </template>
        <AnnouncementManage />
      </el-tab-pane>

      <!-- Tab 8: VIP功能管理 -->
      <el-tab-pane label="VIP功能管理" name="vip">
        <template slot="label">
          <span><i class="el-icon-star-on"></i> VIP功能管理</span>
        </template>
        <VipFeatureManage />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ModelManage from './components/ModelManage.vue'
import UserManage from './components/UserManage.vue'
import LogManage from './components/LogManage.vue'
import PromptManage from './components/PromptManage.vue'
import EmailConfig from './emailConfig.vue'
import UserPageManage from './components/UserPageManage.vue'
import AnnouncementManage from './components/AnnouncementManage.vue'
import VipFeatureManage from './components/VipFeatureManage.vue'

export default {
  name: 'McAdminIndex',
  components: {
    ModelManage,
    UserManage,
    LogManage,
    PromptManage,
    EmailConfig,
    UserPageManage,
    AnnouncementManage,
    VipFeatureManage
  },
  data() {
    return {
      activeTab: 'model',
      username: this.$store.getters.name || 'Admin'
    }
  },
  mounted() {
    // 从URL参数读取要打开的Tab
    const tab = this.$route.query.tab
    if (tab && ['model', 'user', 'log', 'prompt', 'email', 'userpage', 'announcement', 'vip'].includes(tab)) {
      this.activeTab = tab
    }
  },
  watch: {
    activeTab(newTab) {
      // 更新URL参数，方便刷新后保持当前Tab
      this.$router.replace({
        query: { ...this.$route.query, tab: newTab }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mc-admin-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.admin-header {
  background: white;
  padding: 20px 30px;
  margin-bottom: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: 500;
}

.admin-header h2 i {
  margin-right: 10px;
  color: #409EFF;
}

.admin-info {
  font-size: 14px;
  color: #606266;
}

.admin-tabs {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

::v-deep .el-tabs__item {
  font-size: 15px;
  height: 50px;
  line-height: 50px;
  padding: 0 30px;
}

::v-deep .el-tabs__item i {
  margin-right: 8px;
}

::v-deep .el-tabs__content {
  padding-top: 20px;
}
</style>
