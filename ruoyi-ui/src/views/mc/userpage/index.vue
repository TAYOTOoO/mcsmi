<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 16px; font-weight: bold;">用户端管理</span>
        <span style="color: #909399; font-size: 12px; margin-left: 10px;">管理用户端页面配置和公告信息</span>
      </div>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="用户端页面管理" name="pageConfig">
          <UserPageManage />
        </el-tab-pane>
        <el-tab-pane label="公告管理" name="announcement">
          <AnnouncementManage />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import UserPageManage from '../admin/components/UserPageManage'
import AnnouncementManage from '../admin/components/AnnouncementManage'

export default {
  name: 'McUserPageManagement',
  components: {
    UserPageManage,
    AnnouncementManage
  },
  data() {
    return {
      activeTab: 'pageConfig'
    }
  }
}
</script>

<style scoped>
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
</style>
