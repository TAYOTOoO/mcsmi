<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="模板名称">
        <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="是否启用">
        <el-select v-model="queryParams.isActive" placeholder="请选择" clearable>
          <el-option label="已启用" value="1" />
          <el-option label="未启用" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="材质类型">
        <el-select v-model="queryParams.materialType" placeholder="请选择" clearable>
          <el-option label="物品材质" :value="1" />
          <el-option label="UI材质" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增模板</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="templateList">
      <el-table-column label="模板ID" align="center" prop="templateId" width="80" />
      <el-table-column label="模板名称" align="center" prop="templateName" width="200" />
      <el-table-column label="模板类型" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.templateType === 'standard'" type="success">标准模板</el-tag>
          <el-tag v-else type="primary">自定义模板</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="材质类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.materialType === 1" type="warning">物品材质</el-tag>
          <el-tag v-else-if="scope.row.materialType === 2" type="info">UI材质</el-tag>
          <el-tag v-else type="info">未设置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否启用" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isActive"
            active-value="1"
            inactive-value="0"
            @change="handleActiveChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column label="文本模型" align="center" width="200" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ getModelName(scope.row.textModelId, 'text') }}
        </template>
      </el-table-column>
      <el-table-column label="图片模型" align="center" width="200" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ getModelName(scope.row.imageModelId, 'image') }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="small" type="text" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
          <el-button size="small" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="small" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>

        <el-form-item label="模板类型" prop="templateType">
          <el-radio-group v-model="form.templateType">
            <el-radio label="standard">标准模板</el-radio>
            <el-radio label="custom">自定义模板</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="材质类型" prop="materialType">
          <el-radio-group v-model="form.materialType">
            <el-radio :label="1">物品材质</el-radio>
            <el-radio :label="2">UI材质</el-radio>
          </el-radio-group>
          <div class="tip">物品材质和UI材质使用不同的预提示词模板</div>
        </el-form-item>

        <el-form-item label="参考图片" prop="referenceImageUrl">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept="image/*">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
          <div class="tip">可选项，用于图片生成时作为参考（建议用于UI材质模板）</div>
          <div v-if="form.referenceImageUrl" style="margin-top: 10px;">
            <img :src="getImagePreviewUrl(form.referenceImageUrl)" style="max-width: 200px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px;" />
            <el-button size="mini" type="danger" @click="form.referenceImageUrl = null" style="margin-left: 10px;">删除</el-button>
          </div>
        </el-form-item>

        <el-form-item label="模板内容" prop="templateContent">
          <el-input
            v-model="form.templateContent"
            type="textarea"
            :rows="15"
            placeholder="请输入模板内容，使用 [] 作为用户输入的占位符&#10;&#10;示例：&#10;Generate Minecraft [] style textures with the following properties:&#10;- Theme: []&#10;- Style: High quality, detailed"
          />
          <div class="tip">
            <strong>使用说明：</strong><br/>
            • 在需要插入用户输入的地方使用 <code>[]</code> 占位符<br/>
            • 用户提交的风格描述会自动替换所有 <code>[]</code> 占位符<br/>
            • 支持多处使用 <code>[]</code> 占位符<br/>
            • 示例：用户输入"中国风"，<code>[]</code> 会被替换为"中国风"
          </div>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入模板描述" />
        </el-form-item>

        <el-form-item label="文本生成模型" prop="textModelId">
          <el-select v-model="form.textModelId" placeholder="请选择文本生成模型" style="width: 100%">
            <el-option
              v-for="item in textModelList"
              :key="item.modelId"
              :label="item.remark ? item.modelName + ' (' + item.remark + ')' : item.modelName"
              :value="item.modelId"
            />
          </el-select>
          <div class="tip">用于优化用户输入的风格描述（文本生成模型）</div>
          
          <!-- 备用文本模型 -->
          <div style="margin-top: 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
              <span style="font-size: 13px; color: #606266;">备用模型（故障转移）</span>
              <el-button size="small" type="primary" icon="el-icon-plus" @click="handleAddBackupModel('text')">添加备用模型</el-button>
            </div>
            <el-table :data="textBackupModels" border v-if="textBackupModels.length > 0" style="margin-top: 10px;">
              <el-table-column label="优先级" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag type="primary">{{ scope.row.priority }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="模型名称" prop="modelName" />
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="scope">
                  <el-button size="small" type="text" icon="el-icon-top" @click="handleMoveUp(scope.$index, 'text')" :disabled="scope.$index === 0">上移</el-button>
                  <el-button size="small" type="text" icon="el-icon-bottom" @click="handleMoveDown(scope.$index, 'text')" :disabled="scope.$index === textBackupModels.length - 1">下移</el-button>
                  <el-button size="small" type="text" icon="el-icon-delete" @click="handleDeleteBackup(scope.$index, 'text')">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else style="text-align: center; padding: 20px; color: #909399; font-size: 13px;">暂无备用模型，点击上方按钮添加</div>
          </div>
        </el-form-item>

        <el-form-item label="图片生成模型" prop="imageModelId">
          <el-select v-model="form.imageModelId" placeholder="请选择图片生成模型" style="width: 100%">
            <el-option
              v-for="item in imageModelList"
              :key="item.modelId"
              :label="item.remark ? item.modelName + ' (' + item.remark + ')' : item.modelName"
              :value="item.modelId"
            />
          </el-select>
          <div class="tip">用于生成材质图片（图片生成模型）</div>
          
          <!-- 备用图片模型 -->
          <div style="margin-top: 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
              <span style="font-size: 13px; color: #606266;">备用模型（故障转移）</span>
              <el-button size="small" type="primary" icon="el-icon-plus" @click="handleAddBackupModel('image')">添加备用模型</el-button>
            </div>
            <el-table :data="imageBackupModels" border v-if="imageBackupModels.length > 0" style="margin-top: 10px;">
              <el-table-column label="优先级" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag type="success">{{ scope.row.priority }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="模型名称" prop="modelName" />
              <el-table-column label="操作" width="150" align="center">
                <template slot-scope="scope">
                  <el-button size="small" type="text" icon="el-icon-top" @click="handleMoveUp(scope.$index, 'image')" :disabled="scope.$index === 0">上移</el-button>
                  <el-button size="small" type="text" icon="el-icon-bottom" @click="handleMoveDown(scope.$index, 'image')" :disabled="scope.$index === imageBackupModels.length - 1">下移</el-button>
                  <el-button size="small" type="text" icon="el-icon-delete" @click="handleDeleteBackup(scope.$index, 'image')">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else style="text-align: center; padding: 20px; color: #909399; font-size: 13px;">暂无备用模型，点击上方按钮添加</div>
          </div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog title="查看模板" :visible.sync="viewOpen" width="900px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="模板名称">{{ viewData.templateName }}</el-descriptions-item>
        <el-descriptions-item label="模板类型">
          <el-tag v-if="viewData.templateType === 'standard'" type="success">标准模板</el-tag>
          <el-tag v-else type="primary">自定义模板</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="材质类型">
          <el-tag v-if="viewData.materialType === 1" type="warning">物品材质</el-tag>
          <el-tag v-else-if="viewData.materialType === 2" type="info">UI材质</el-tag>
          <el-tag v-else type="info">未设置</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="参考图片" v-if="viewData.referenceImageUrl">
          <img :src="getImagePreviewUrl(viewData.referenceImageUrl)" style="max-width: 300px; max-height: 300px; border: 1px solid #ddd; border-radius: 4px;" />
        </el-descriptions-item>
        <el-descriptions-item label="是否启用">
          <el-tag v-if="viewData.isActive === '1'" type="success">已启用</el-tag>
          <el-tag v-else type="info">未启用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="viewData.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="文本生成模型">{{ getModelName(viewData.textModelId, 'text') }}</el-descriptions-item>
        <el-descriptions-item label="图片生成模型">{{ getModelName(viewData.imageModelId, 'image') }}</el-descriptions-item>
        <el-descriptions-item label="模板内容">
          <pre style="white-space: pre-wrap; word-wrap: break-word; max-height: 400px; overflow-y: auto;">{{ viewData.templateContent }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPromptTemplate, getPromptTemplate, addPromptTemplate, updatePromptTemplate, delPromptTemplate, activatePromptTemplate } from '@/api/mc/prompt'
import { listModel } from '@/api/mc/model'
import { listTemplateModelConfig, addTemplateModelConfig, delTemplateModelConfig } from '@/api/mc/templateModelConfig'

export default {
  name: 'McPromptTemplate',
  data() {
    return {
      textBackupModels: [],
      imageBackupModels: [],
      loading: false,
      templateList: [],
      open: false,
      viewOpen: false,
      viewData: {},
      title: '',
      total: 0,
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload/referenceImage',
      uploadHeaders: { Authorization: 'Bearer ' + this.$store.getters.token },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateName: null,
        isActive: null,
        materialType: null
      },
      form: {},
      rules: {
        templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
        templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
        templateContent: [{ required: true, message: '模板内容不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        textModelId: [{ required: true, message: '请选择文本生成模型', trigger: 'change' }],
        imageModelId: [{ required: true, message: '请选择图片生成模型', trigger: 'change' }]
      },
      // AI模型列表（用于下拉选择）
      textModelList: [],
      imageModelList: []
    }
  },
  created() {
    this.getList()
    this.loadModelList()
  },
  methods: {
    getList() {
      this.loading = true
      listPromptTemplate(this.queryParams).then(response => {
        this.templateList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 加载AI模型列表（文本模型和图片模型） */
    loadModelList() {
      // 获取所有模型（不分页）
      listModel({ pageNum: 1, pageSize: 100 }).then(response => {
        const allModels = response.rows || []
        this.textModelList = allModels.filter(m => m.modelType === 2)
        this.imageModelList = allModels.filter(m => m.modelType === 1)
      })
    },
    /** 获取模型显示名称 */
    getModelName(modelId, type) {
      const list = type === 'text' ? this.textModelList : this.imageModelList
      const model = list.find(m => m.modelId === modelId)
      return model ? (model.remark ? model.modelName + ' (' + model.remark + ')' : model.modelName) : (modelId ? '模型ID:' + modelId : '未配置')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加预提示词模板'
    },
    handleView(row) {
      this.viewData = Object.assign({}, row)
      this.viewOpen = true
    },
    handleUpdate(row) {
      this.reset()
      getPromptTemplate(row.templateId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改预提示词模板'
        listTemplateModelConfig({ templateId: row.templateId }).then(res => {
          const configs = res.rows || []
          this.textBackupModels = configs.filter(c => c.modelType === 2).sort((a, b) => a.priority - b.priority).map(c => ({ configId: c.configId, modelId: c.modelId, modelName: c.modelName, priority: c.priority }))
          this.imageBackupModels = configs.filter(c => c.modelType === 1).sort((a, b) => a.priority - b.priority).map(c => ({ configId: c.configId, modelId: c.modelId, modelName: c.modelName, priority: c.priority }))
        })
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该预提示词模板?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delPromptTemplate(row.templateId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    handleActiveChange(row) {
      const text = row.isActive === '1' ? '启用' : '禁用'
      this.$confirm('确认要' + text + '该模板吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return activatePromptTemplate(row.templateId)
      }).then(() => {
        this.$message.success(text + '成功')
        this.getList()
      }).catch(() => {
        row.isActive = row.isActive === '1' ? '0' : '1'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.templateId != null) {
            updatePromptTemplate(this.form).then(response => {
              this.$message.success('修改成功')
              return this.saveBackupModels(this.form.templateId)
            }).then(() => {
              this.open = false
              this.getList()
            }).catch(error => {
              console.error('保存失败:', error)
              this.$message.error('保存失败: ' + (error.message || '未知错误'))
            })
          } else {
            addPromptTemplate(this.form).then(response => {
              this.$message.success('新增成功')
              // 安全获取templateId
              let templateId = null
              if (response && response.data) {
                templateId = response.data.templateId || response.data
              } else if (response) {
                templateId = response
              }
              
              if (!templateId) {
                console.warn('无法获取templateId，跳过备用模型保存')
                this.open = false
                this.getList()
                return Promise.resolve()
              }
              
              return this.saveBackupModels(templateId)
            }).then(() => {
              this.open = false
              this.getList()
            }).catch(error => {
              console.error('保存失败:', error)
              this.$message.error('保存失败: ' + (error.message || '未知错误'))
            })
          }
        }
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        templateId: null,
        templateName: null,
        templateType: 'standard',
        materialType: 1,
        referenceImageUrl: null,
        templateContent: null,
        isActive: '0',
        status: '0',
        description: null,
        remark: null,
        textModelId: null,
        imageModelId: null
      }
      this.resetForm('form')
    },
    handleAddBackupModel(type) {
      const modelList = type === 'text' ? this.textModelList : this.imageModelList
      const backupList = type === 'text' ? this.textBackupModels : this.imageBackupModels
      const usedIds = backupList.map(m => m.modelId)
      const availableModels = modelList.filter(m => !usedIds.includes(m.modelId))
      
      if (availableModels.length === 0) {
        this.$message.warning('没有可用的备用模型了')
        return
      }
      
      // 构建选项HTML
      const options = availableModels.map(m => {
        const label = m.remark ? `${m.modelName} (${m.remark})` : m.modelName
        return `<option value="${m.modelId}">${label}</option>`
      }).join('')
      
      const h = this.$createElement
      this.$msgbox({
        title: '选择备用模型',
        message: h('div', { style: 'padding: 20px 0;' }, [
          h('select', {
            attrs: { id: 'backup-model-select' },
            style: 'width: 100%; padding: 8px; font-size: 14px; border: 1px solid #dcdfe6; border-radius: 4px;',
            domProps: { innerHTML: '<option value="">请选择备用模型</option>' + options }
          })
        ]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            const select = document.getElementById('backup-model-select')
            const selectedValue = select ? parseInt(select.value) : null
            
            if (!selectedValue) {
              this.$message.warning('请选择一个模型')
              return
            }
            
            const model = modelList.find(m => m.modelId === selectedValue)
            if (model) {
              const newModel = {
                modelId: model.modelId,
                modelName: model.remark ? `${model.modelName} (${model.remark})` : model.modelName,
                priority: backupList.length + 1
              }
              if (type === 'text') {
                this.textBackupModels.push(newModel)
              } else {
                this.imageBackupModels.push(newModel)
              }
              this.$message.success('添加成功')
            }
          }
          done()
        }
      })
    },
    handleDeleteBackup(index, type) {
      if (type === 'text') {
        this.textBackupModels.splice(index, 1)
        this.textBackupModels.forEach((m, i) => m.priority = i + 1)
      } else {
        this.imageBackupModels.splice(index, 1)
        this.imageBackupModels.forEach((m, i) => m.priority = i + 1)
      }
    },
    handleMoveUp(index, type) {
      const list = type === 'text' ? this.textBackupModels : this.imageBackupModels
      if (index > 0) {
        const temp = list[index]
        list[index] = list[index - 1]
        list[index - 1] = temp
        list.forEach((m, i) => m.priority = i + 1)
      }
    },
    handleMoveDown(index, type) {
      const list = type === 'text' ? this.textBackupModels : this.imageBackupModels
      if (index < list.length - 1) {
        const temp = list[index]
        list[index] = list[index + 1]
        list[index + 1] = temp
        list.forEach((m, i) => m.priority = i + 1)
      }
    },
    async saveBackupModels(templateId) {
      try {
        const res = await listTemplateModelConfig({ templateId })
        const existingConfigs = res.rows || []
        for (const config of existingConfigs) { await delTemplateModelConfig(config.configId) }
        const allModels = [...this.textBackupModels.map(m => ({ ...m, modelType: 2 })), ...this.imageBackupModels.map(m => ({ ...m, modelType: 1 }))]
        for (const model of allModels) {
          await addTemplateModelConfig({ templateId, modelId: model.modelId, modelType: model.modelType, priority: model.priority, status: '0' })
        }
      } catch (error) {
        console.error('保存备用模型失败:', error)
        throw error
      }
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      
      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB!')
        return false
      }
      return true
    },
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.form.referenceImageUrl = response.data
        this.$message.success('上传成功')
      } else {
        this.$message.error(response.msg || '上传失败')
      }
    },
    handleUploadError(err) {
      this.$message.error('上传失败: ' + err.message)
    },
    getImagePreviewUrl(path) {
      // 如果是HTTP URL，直接返回
      if (path && path.startsWith('http')) {
        return path
      }
      // 如果是文件名，转换为访问URL
      if (path) {
        return process.env.VUE_APP_BASE_API + '/common/upload/referenceImage/' + path
      }
      return ''
    }
  }
}
</script>

<style scoped>
.tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}

.tip strong {
  color: #E6A23C;
}

.tip code {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  color: #E6A23C;
  font-weight: bold;
}

pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
</style>
