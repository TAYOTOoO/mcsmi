<template>
  <el-dialog
    title="模型配置"
    :visible.sync="visible"
    width="900px"
    :before-close="handleClose"
    append-to-body
  >
    <div class="model-config-container">
      <!-- 文本模型配置 -->
      <div class="model-section">
        <div class="section-header">
          <h3>文本模型配置</h3>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAddModel(2)">添加文本模型</el-button>
        </div>
        <el-table :data="textModels" border>
          <el-table-column label="优先级" width="80" align="center">
            <template slot-scope="scope">
              <el-tag type="primary">{{ scope.row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="模型名称" prop="modelName" />
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleStatusChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-top"
                @click="handleMoveUp(scope.row, textModels)"
                :disabled="scope.$index === 0"
              >上移</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-bottom"
                @click="handleMoveDown(scope.row, textModels)"
                :disabled="scope.$index === textModels.length - 1"
              >下移</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 图片模型配置 -->
      <div class="model-section">
        <div class="section-header">
          <h3>图片模型配置</h3>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAddModel(1)">添加图片模型</el-button>
        </div>
        <el-table :data="imageModels" border>
          <el-table-column label="优先级" width="80" align="center">
            <template slot-scope="scope">
              <el-tag type="success">{{ scope.row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="模型名称" prop="modelName" />
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleStatusChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-top"
                @click="handleMoveUp(scope.row, imageModels)"
                :disabled="scope.$index === 0"
              >上移</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-bottom"
                @click="handleMoveDown(scope.row, imageModels)"
                :disabled="scope.$index === imageModels.length - 1"
              >下移</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加模型对话框 -->
    <el-dialog
      title="添加模型"
      :visible.sync="addDialogVisible"
      width="500px"
      append-to-body
    >
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="选择模型">
          <el-select v-model="addForm.modelId" placeholder="请选择模型" style="width: 100%">
            <el-option
              v-for="model in availableModels"
              :key="model.modelId"
              :label="model.modelName"
              :value="model.modelId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.remark" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAdd">确定</el-button>
      </div>
    </el-dialog>

    <div slot="footer">
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { listTemplateModelConfig, addTemplateModelConfig, updateTemplateModelConfig, delTemplateModelConfig } from '@/api/mc/templateModelConfig'
import { listAiModel } from '@/api/mc/aiModel'

export default {
  name: 'ModelConfigDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    templateId: {
      type: Number,
      default: null
    },
    templateName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      textModels: [],
      imageModels: [],
      addDialogVisible: false,
      addForm: {
        modelId: null,
        modelType: null,
        remark: ''
      },
      availableModels: [],
      allModels: []
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.loadData()
      }
    }
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        // 加载所有AI模型
        const modelRes = await listAiModel({ status: '0', isActive: '1' })
        this.allModels = modelRes.rows || []
        
        // 加载模板的模型配置
        const configRes = await listTemplateModelConfig({ templateId: this.templateId })
        const configs = configRes.rows || []
        
        // 分类
        this.textModels = configs.filter(c => c.modelType === 2).sort((a, b) => a.priority - b.priority)
        this.imageModels = configs.filter(c => c.modelType === 1).sort((a, b) => a.priority - b.priority)
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    
    handleAddModel(modelType) {
      this.addForm.modelType = modelType
      this.addForm.modelId = null
      this.addForm.remark = ''
      
      // 过滤可用模型
      const usedModelIds = (modelType === 2 ? this.textModels : this.imageModels).map(m => m.modelId)
      this.availableModels = this.allModels.filter(m => 
        m.modelType === modelType && !usedModelIds.includes(m.modelId)
      )
      
      this.addDialogVisible = true
    },
    
    async handleConfirmAdd() {
      if (!this.addForm.modelId) {
        this.$message.warning('请选择模型')
        return
      }
      
      try {
        const targetList = this.addForm.modelType === 2 ? this.textModels : this.imageModels
        const maxPriority = targetList.length > 0 ? Math.max(...targetList.map(m => m.priority)) : 0
        
        await addTemplateModelConfig({
          templateId: this.templateId,
          modelId: this.addForm.modelId,
          modelType: this.addForm.modelType,
          priority: maxPriority + 1,
          status: '0',
          remark: this.addForm.remark
        })
        
        this.$message.success('添加成功')
        this.addDialogVisible = false
        this.loadData()
      } catch (error) {
        this.$message.error('添加失败')
      }
    },
    
    handleDelete(row) {
      this.$confirm('确定删除该模型配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await delTemplateModelConfig(row.configId)
          this.$message.success('删除成功')
          this.loadData()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    
    handleMoveUp(row, list) {
      const index = list.findIndex(m => m.configId === row.configId)
      if (index > 0) {
        const temp = list[index].priority
        list[index].priority = list[index - 1].priority
        list[index - 1].priority = temp
        list.sort((a, b) => a.priority - b.priority)
      }
    },
    
    handleMoveDown(row, list) {
      const index = list.findIndex(m => m.configId === row.configId)
      if (index < list.length - 1) {
        const temp = list[index].priority
        list[index].priority = list[index + 1].priority
        list[index + 1].priority = temp
        list.sort((a, b) => a.priority - b.priority)
      }
    },
    
    async handleStatusChange(row) {
      try {
        await updateTemplateModelConfig(row)
        this.$message.success('状态更新成功')
      } catch (error) {
        this.$message.error('状态更新失败')
        this.loadData()
      }
    },
    
    async handleSave() {
      try {
        // 批量更新优先级
        const allConfigs = [...this.textModels, ...this.imageModels]
        for (const config of allConfigs) {
          await updateTemplateModelConfig(config)
        }
        this.$message.success('保存成功')
        this.$emit('refresh')
        this.handleClose()
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    
    handleClose() {
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style scoped>
.model-config-container {
  max-height: 600px;
  overflow-y: auto;
}

.model-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
</style>
