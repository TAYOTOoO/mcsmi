<template>
  <div class="mc-generate-container">
    <el-card class="generation-card">
      <div slot="header">
        <span class="card-title">🎮 MC材质包AI生成</span>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <!-- 风格输入 -->
        <el-form-item label="风格描述" prop="styleDescription">
          <el-input
            v-model="form.styleDescription"
            type="textarea"
            :rows="5"
            placeholder="请输入您想要的材质包风格，例如：
• cyberpunk (赛博朋克风格)
• medieval fantasy (中世纪奇幻)
• cute cartoon (可爱卡通)
• realistic HD (高清写实)
• chinese traditional (中国传统风格)
• pixel retro (复古像素)"
            maxlength="500"
            show-word-limit>
          </el-input>
        </el-form-item>

        <!-- MC版本选择 -->
        <el-form-item label="MC版本" prop="versionId">
          <el-select v-model="form.versionId" placeholder="请选择Minecraft版本" style="width: 100%">
            <el-option label="1.20.1 - Trails & Tales" :value="1" />
            <el-option label="1.19.4" :value="2" />
            <el-option label="1.18.2" :value="3" />
            <el-option label="1.17.1" :value="4" />
          </el-select>
        </el-form-item>

        <!-- 材质模板 -->
        <el-form-item label="材质模板" prop="templateId">
          <el-select v-model="form.templateId" placeholder="请选择材质模板" style="width: 100%">
            <el-option label="PvP物品50件套 (32x32)" :value="1" />
          </el-select>
        </el-form-item>

        <!-- 积分提示 -->
        <el-alert
          title="本次生成需要消耗100积分"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px">
          <template slot="default">
            <div>当前剩余积分: <strong style="color: #E6A23C; font-size: 18px;">{{ userPoints }}</strong></div>
          </template>
        </el-alert>

        <!-- 生成按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="generating"
            @click="submitGenerate"
            :disabled="userPoints < 100"
            style="width: 100%">
            <i class="el-icon-magic-stick"></i>
            {{ generating ? '生成中...' : '开始生成材质包' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的任务列表 -->
    <el-card class="tasks-card" style="margin-top: 20px">
      <div slot="header">
        <span class="card-title">📋 我的任务</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshTasks">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="taskList" style="width: 100%">
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        <el-table-column prop="styleDescription" label="风格描述" :show-overflow-tooltip="true" />
        <el-table-column prop="taskStatus" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.taskStatus === 0" type="info">待处理</el-tag>
            <el-tag v-else-if="scope.row.taskStatus === 1" type="primary">生成中</el-tag>
            <el-tag v-else-if="scope.row.taskStatus === 2" type="warning">预处理中</el-tag>
            <el-tag v-else-if="scope.row.taskStatus === 3" type="warning">切割中</el-tag>
            <el-tag v-else-if="scope.row.taskStatus === 4" type="success">完成</el-tag>
            <el-tag v-else-if="scope.row.taskStatus === 5" type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.taskStatus === 4"
              size="mini"
              type="success"
              @click="viewResult(scope.row)">查看结果</el-button>
            <el-button
              v-if="scope.row.taskStatus === 5"
              size="mini"
              type="warning"
              @click="regenerate(scope.row)">重新生成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { addGeneration, listGeneration, regenerate } from '@/api/mc/generation'

export default {
  name: 'McGenerate',
  data() {
    return {
      form: {
        styleDescription: '',
        versionId: 1,
        templateId: 1,
        imageModelId: 2,
        textModelId: 1
      },
      rules: {
        styleDescription: [
          { required: true, message: '请输入风格描述', trigger: 'blur' },
          { min: 2, message: '风格描述至少2个字符', trigger: 'blur' }
        ],
        versionId: [{ required: true, message: '请选择MC版本', trigger: 'change' }],
        templateId: [{ required: true, message: '请选择材质模板', trigger: 'change' }]
      },
      userPoints: 10000,
      generating: false,
      loading: false,
      taskList: []
    }
  },
  created() {
    this.loadUserPoints()
    this.loadTasks()
  },
  methods: {
    loadUserPoints() {
      // TODO: 调用API获取用户积分
      this.userPoints = 10000
    },
    loadTasks() {
      this.loading = true
      listGeneration().then(response => {
        this.taskList = response.rows
        this.loading = false
      })
    },
    refreshTasks() {
      this.loadTasks()
    },
    submitGenerate() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.userPoints < 100) {
            this.$message.error('积分不足，请先充值')
            return
          }

          this.$confirm('确认消耗100积分生成材质包?', '提示', {
            type: 'warning'
          }).then(() => {
            this.generating = true
            addGeneration(this.form).then(response => {
              this.$message.success('已提交生成任务，请稍后查看结果')
              this.generating = false
              this.userPoints -= 100
              this.loadTasks()
              this.$refs.form.resetFields()
            }).catch(() => {
              this.generating = false
            })
          })
        }
      })
    },
    viewResult(row) {
      this.$router.push({ path: '/mc/result', query: { taskId: row.taskId } })
    },
    regenerate(row) {
      this.$confirm('重新生成将消耗100积分，是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        regenerate(row.taskId).then(() => {
          this.$message.success('重新生成任务已提交')
          this.userPoints -= 100
          this.loadTasks()
        })
      })
    }
  }
}
</script>

<style scoped>
.mc-generate-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.generation-card {
  border-radius: 10px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .mc-generate-container {
    padding: 10px;
  }

  .el-form-item >>> .el-form-item__label {
    width: 100px !important;
  }

  .el-form-item >>> .el-form-item__content {
    margin-left: 100px !important;
  }
}
</style>
