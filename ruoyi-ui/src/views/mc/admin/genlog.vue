<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="用户名">
        <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="处理中" value="0" />
          <el-option label="成功" value="1" />
          <el-option label="失败" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="logList">
      <el-table-column label="日志ID" align="center" prop="logId" width="80" />
      <el-table-column label="任务ID" align="center" prop="taskId" width="80" />
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="风格描述" align="center" prop="styleDescription" :show-overflow-tooltip="true" width="200" />
      <el-table-column label="文本模型" align="center" prop="textModelName" width="150" />
      <el-table-column label="图片模型" align="center" prop="imageModelName" width="150" />
      <el-table-column label="生成图片" align="center" width="120">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.imagePreviewUrl"
            style="width: 80px; height: 80px; cursor: pointer;"
            :src="scope.row.imagePreviewUrl"
            :preview-src-list="[scope.row.imagePreviewUrl]"
            fit="cover">
          </el-image>
          <span v-else-if="scope.row.generatedImageUrl">加载中...</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="耗时(秒)" align="center" prop="generationTime" width="100" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="info">处理中</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">成功</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 详情对话框 -->
    <el-dialog title="生成日志详情" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border v-if="logDetail">
        <el-descriptions-item label="日志ID">{{ logDetail.logId }}</el-descriptions-item>
        <el-descriptions-item label="任务ID">{{ logDetail.taskId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ logDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ logDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="风格描述" :span="2">{{ logDetail.styleDescription }}</el-descriptions-item>
        <el-descriptions-item label="文本模型">{{ logDetail.textModelName }}</el-descriptions-item>
        <el-descriptions-item label="图片模型">{{ logDetail.imageModelName }}</el-descriptions-item>
        <el-descriptions-item label="生成耗时">{{ logDetail.generationTime }} 秒</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="logDetail.status === '0'" type="info">处理中</el-tag>
          <el-tag v-else-if="logDetail.status === '1'" type="success">成功</el-tag>
          <el-tag v-else-if="logDetail.status === '2'" type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ logDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="logDetail.errorMessage">
          <span style="color: #F56C6C;">{{ logDetail.errorMessage }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="生成图片" :span="2">
          <el-image
            v-if="logDetail.imagePreviewUrl"
            style="width: 400px; max-height: 400px;"
            :src="logDetail.imagePreviewUrl"
            :preview-src-list="[logDetail.imagePreviewUrl]"
            fit="contain">
          </el-image>
          <span v-else-if="logDetail.generatedImageUrl">加载中...</span>
          <span v-else>暂无图片</span>
        </el-descriptions-item>
        <el-descriptions-item label="优化后的提示词" :span="2">
          <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap; font-family: monospace; font-size: 12px; background: #f5f5f5; padding: 10px; border-radius: 4px;">{{ logDetail.optimizedPrompt }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGenLog, getGenLog, delGenLog } from '@/api/mc/genlog'
import imageLoader from '@/mixins/imageLoader'

export default {
  name: 'McGenLog',
  mixins: [imageLoader],
  data() {
    return {
      loading: false,
      logList: [],
      total: 0,
      detailOpen: false,
      logDetail: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        username: null,
        status: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const response = await listGenLog(this.queryParams)
        this.logList = response.rows
        this.total = response.total

        // 加载所有图片
        for (const log of this.logList) {
          if (log.generatedImageUrl && log.taskId) {
            this.loadLogImage(log)
          }
        }
      } finally {
        this.loading = false
      }
    },
    async loadLogImage(log) {
      try {
        const blobUrl = await this.loadAuthImage(log.generatedImageUrl, log.taskId)
        if (blobUrl) {
          this.$set(log, 'imagePreviewUrl', blobUrl)
        }
      } catch (error) {
        console.error('加载图片失败:', error)
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    async handleView(row) {
      const response = await getGenLog(row.logId)
      this.logDetail = response.data

      // 加载详情图片
      if (this.logDetail.generatedImageUrl && this.logDetail.taskId) {
        const blobUrl = await this.loadAuthImage(this.logDetail.generatedImageUrl, this.logDetail.taskId)
        if (blobUrl) {
          this.$set(this.logDetail, 'imagePreviewUrl', blobUrl)
        }
      }

      this.detailOpen = true
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该生成日志?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delGenLog(row.logId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    }
  }
}
</script>

<style scoped>
.el-image {
  border-radius: 4px;
  border: 1px solid #DCDFE6;
}
</style>
