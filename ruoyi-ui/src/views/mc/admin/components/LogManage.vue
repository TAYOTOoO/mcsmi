<template>
  <div class="log-manage-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCalls }}</div>
              <div class="stat-label">总调用次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <i class="el-icon-success"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.successRate }}%</div>
              <div class="stat-label">成功率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgTime }}s</div>
              <div class="stat-label">平均耗时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <i class="el-icon-coin"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTokens }}</div>
              <div class="stat-label">Token消耗</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="调用状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="成功" value="1" />
            <el-option label="失败" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型类型">
          <el-select v-model="queryParams.modelType" placeholder="请选择" clearable>
            <el-option label="图片生成" value="1" />
            <el-option label="文本生成" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="logList" style="width: 100%">
        <el-table-column label="日志ID" align="center" prop="logId" width="80" />
        <el-table-column label="用户" align="center" prop="username" width="120" />
        <el-table-column label="风格描述" align="center" width="150">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.styleDescription" placement="top">
              <span class="text-ellipsis">{{ scope.row.styleDescription }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="优化后提示词" align="center" width="200">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.optimizedPrompt" placement="top">
              <span class="text-ellipsis">{{ scope.row.optimizedPrompt || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="生成图片" align="center" width="100">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.imagePreviewUrl"
              :src="scope.row.imagePreviewUrl"
              :preview-src-list="[scope.row.imagePreviewUrl]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer;"
            >
              <div slot="error" class="image-error">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
            <span v-else-if="scope.row.generatedImageUrl">加载中...</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="文本模型" align="center" width="150">
          <template slot-scope="scope">
            <div>{{ scope.row.textModelName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="图像模型" align="center" width="150">
          <template slot-scope="scope">
            <div>{{ scope.row.imageModelName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="耗时(秒)" align="center" width="100">
          <template slot-scope="scope">
            <el-tag :type="getTimeTagType(scope.row.generationTime)" size="small">
              {{ scope.row.generationTime }}s
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === '1'" type="success">成功</el-tag>
            <el-tag v-else-if="scope.row.status === '2'" type="danger">失败</el-tag>
            <el-tag v-else type="info">处理中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
        <el-table-column label="操作" align="center" fixed="right" width="120">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="调用日志详情" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ detailData.logId }}</el-descriptions-item>
        <el-descriptions-item label="任务ID">{{ detailData.taskId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="风格描述" :span="2">
          <div style="white-space: pre-wrap;">{{ detailData.styleDescription }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="优化后提示词" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">{{ detailData.optimizedPrompt }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="文本模型">{{ detailData.textModelName }}</el-descriptions-item>
        <el-descriptions-item label="图像模型">{{ detailData.imageModelName }}</el-descriptions-item>
        <el-descriptions-item label="生成耗时">{{ detailData.generationTime }}秒</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === '1'" type="success">成功</el-tag>
          <el-tag v-else-if="detailData.status === '2'" type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="生成图片" :span="2">
          <el-image
            v-if="detailData.imagePreviewUrl"
            :src="detailData.imagePreviewUrl"
            :preview-src-list="[detailData.imagePreviewUrl]"
            fit="contain"
            style="max-width: 100%; max-height: 400px; cursor: pointer;"
          >
            <div slot="error" class="image-error">
              <i class="el-icon-picture-outline"></i>
              <div>加载失败</div>
            </div>
          </el-image>
          <span v-else-if="detailData.generatedImageUrl">加载中...</span>
          <span v-else>暂无图片</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.errorMessage" label="错误信息" :span="2">
          <div style="color: #F56C6C; white-space: pre-wrap;">{{ detailData.errorMessage }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGenLog, getGenLogStats } from '@/api/mc/genlog'
import imageLoader from '@/mixins/imageLoader'

export default {
  name: 'LogManage',
  mixins: [imageLoader],
  data() {
    return {
      loading: false,
      logList: [],
      total: 0,
      detailOpen: false,
      detailData: {},
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        username: null,
        status: null,
        modelType: null
      },
      stats: {
        totalCalls: 0,
        successRate: 0,
        avgTime: 0,
        totalTokens: 0
      }
    }
  },
  created() {
    this.getList()
    this.getStats()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const params = { ...this.queryParams }
        if (this.dateRange && this.dateRange.length === 2) {
          params.startTime = this.dateRange[0]
          params.endTime = this.dateRange[1]
        }
        const response = await listGenLog(params)
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
    getStats() {
      getGenLogStats().then(response => {
        this.stats = response.data
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    async handleView(row) {
      this.detailData = Object.assign({}, row)

      // 加载详情图片
      if (this.detailData.generatedImageUrl && this.detailData.taskId) {
        const blobUrl = await this.loadAuthImage(this.detailData.generatedImageUrl, this.detailData.taskId)
        if (blobUrl) {
          this.$set(this.detailData, 'imagePreviewUrl', blobUrl)
        }
      }

      this.detailOpen = true
    },
    getTimeTagType(time) {
      if (time < 30) return 'success'
      if (time < 60) return 'warning'
      return 'danger'
    },
    resetForm(refName) {
      if (this.$refs[refName]) {
        this.$refs[refName].resetFields()
      }
    }
  }
}
</script>

<style scoped>
.log-manage-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-card, .table-card {
  margin-bottom: 20px;
}

.text-ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

.image-error i {
  font-size: 24px;
  margin-bottom: 5px;
}
</style>
