<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="模型名称">
        <el-input v-model="queryParams.modelName" placeholder="请输入模型名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模型类型">
        <el-select v-model="queryParams.modelType" placeholder="请选择" clearable>
          <el-option label="图片生成" :value="1" />
          <el-option label="文本生成" :value="2" />
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
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增模型</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="modelList">
      <el-table-column label="模型ID" align="center" prop="modelId" width="80" />
      <el-table-column label="模型名称" align="center" prop="modelName" width="250" />
      <el-table-column label="模型类型" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.modelType === 1" type="success">图片生成</el-tag>
          <el-tag v-else-if="scope.row.modelType === 2" type="primary">文本生成</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="API地址" align="center" prop="apiUrl" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="启用调用" align="center" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isActive"
            active-value="1"
            inactive-value="0"
            @change="handleActiveChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-connection" @click="handleTest(scope.row)">测试</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="API基础地址" prop="apiBaseUrl">
          <el-input v-model="form.apiBaseUrl" placeholder="请输入API基础地址" />
          <div class="tip">
            <strong>注意：请使用API地址，不要使用管理界面地址！</strong><br/>
            <br/>
            常见API服务示例：<br/>
            • 艾可API: https://aicanapi.com/v1<br/>
            • OpenAI官方: https://api.openai.com/v1<br/>
            • New API / One API: https://your-domain.com/v1<br/>
            • Google Gemini: https://generativelanguage.googleapis.com/v1<br/>
            <br/>
            ❌ 错误示例: https://your-domain.com (这是管理界面，不是API地址)<br/>
            ✅ 正确示例: https://your-domain.com/v1 (API调用地址)
          </div>
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" placeholder="请输入API密钥" show-password />
          <div class="tip">密钥将加密存储，请妥善保管</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-download" @click="handleFetchModels">获取可用模型列表</el-button>
        </el-form-item>

        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="请输入模型名称或从上方获取" />
        </el-form-item>

        <el-form-item label="模型类型" prop="modelType">
          <el-radio-group v-model="form.modelType">
            <el-radio :label="1">图片生成模型</el-radio>
            <el-radio :label="2">文本生成模型</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="API地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="完整的API调用地址" />
          <div class="tip">
            完整的API调用地址会在获取模型列表后自动填充
          </div>
        </el-form-item>

        <el-form-item label="模型参数" prop="modelParams">
          <el-input
            v-model="form.modelParams"
            type="textarea"
            :rows="6"
            placeholder='请输入JSON格式的模型参数，例如:&#10;{"temperature": 0.8, "topK": 40, "topP": 0.95, "maxOutputTokens": 4096}'
          />
          <div class="tip">JSON格式，配置模型的生成参数</div>
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

    <!-- 模型选择对话框 -->
    <el-dialog title="选择模型" :visible.sync="modelSelectOpen" width="800px" append-to-body>
      <el-table :data="availableModels" @row-click="handleModelSelect" highlight-current-row>
        <el-table-column label="模型名称" prop="modelName" width="250" />
        <el-table-column label="显示名称" prop="displayName" width="200" />
        <el-table-column label="类型" align="center" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.modelType === 1" type="success">图片生成</el-tag>
            <el-tag v-else type="primary">文本生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" :show-overflow-tooltip="true" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="modelSelectOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listModel, getModel, addModel, updateModel, delModel, testModel, fetchModels, setActiveModel } from '@/api/mc/model'

export default {
  name: 'McModelManage',
  data() {
    return {
      loading: false,
      modelList: [],
      open: false,
      modelSelectOpen: false,
      availableModels: [],
      title: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        modelName: null,
        modelType: null
      },
      form: {},
      rules: {
        apiBaseUrl: [{ required: true, message: 'API基础地址不能为空', trigger: 'blur' }],
        apiKey: [{ required: true, message: 'API密钥不能为空', trigger: 'blur' }],
        modelName: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
        modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
        apiUrl: [
          { required: true, message: 'API地址不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listModel(this.queryParams).then(response => {
        this.modelList = response.rows
        this.loading = false
      })
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
      this.title = '添加AI模型'
    },
    handleUpdate(row) {
      this.reset()
      getModel(row.modelId).then(response => {
        this.form = response.data
        // 从完整API地址中提取基础地址
        if (this.form.apiUrl) {
          this.form.apiBaseUrl = this.extractBaseUrl(this.form.apiUrl)
        }
        this.open = true
        this.title = '修改AI模型'
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该AI模型?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delModel(row.modelId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    handleTest(row) {
      this.$confirm('确认测试该模型的连通性?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        const loading = this.$loading({
          lock: true,
          text: '测试中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })

        testModel(row.modelId).then(response => {
          loading.close()
          this.$message.success('模型连通性正常')
        }).catch(() => {
          loading.close()
        })
      })
    },
    handleFetchModels() {
      if (!this.form.apiBaseUrl || !this.form.apiKey) {
        this.$message.warning('请先填写API基础地址和密钥')
        return
      }

      const loading = this.$loading({
        lock: true,
        text: '获取模型列表中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      fetchModels({
        apiBaseUrl: this.form.apiBaseUrl,
        apiKey: this.form.apiKey
      }).then(response => {
        loading.close()
        this.availableModels = response.data

        if (this.availableModels.length === 0) {
          this.$message.warning('未找到可用模型')
        } else if (this.availableModels.length === 1) {
          // 只有一个模型，直接填充
          this.handleModelSelect(this.availableModels[0])
          this.$message.success('已自动填充模型信息')
        } else {
          // 多个模型，让用户选择
          this.modelSelectOpen = true
          this.$message.success(`找到 ${this.availableModels.length} 个可用模型，请选择`)
        }
      }).catch(error => {
        loading.close()
      })
    },
    handleModelSelect(row) {
      this.form.modelName = row.modelName
      this.form.modelType = row.modelType
      this.form.apiUrl = row.apiUrl
      this.modelSelectOpen = false
      this.$message.success('已选择模型: ' + row.displayName)
    },
    handleStatusChange(row) {
      updateModel(row).then(() => {
        this.$message.success('状态修改成功')
      })
    },
    handleActiveChange(row) {
      const statusText = row.isActive === '1' ? '启用' : '禁用'
      const modelTypeText = row.modelType === 1 ? '图片生成' : '文本生成'

      this.$confirm(`确认${statusText}该${modelTypeText}模型吗？${row.isActive === '1' ? '启用后将自动禁用同类型的其他模型。' : ''}`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        setActiveModel(row.modelId, { isActive: row.isActive }).then(() => {
          this.$message.success('设置成功')
          this.getList()
        }).catch(() => {
          row.isActive = row.isActive === '1' ? '0' : '1'
        })
      }).catch(() => {
        row.isActive = row.isActive === '1' ? '0' : '1'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 验证JSON格式
          if (this.form.modelParams) {
            try {
              JSON.parse(this.form.modelParams)
            } catch (e) {
              this.$message.error('模型参数必须是有效的JSON格式')
              return
            }
          }

          if (this.form.modelId != null) {
            updateModel(this.form).then(() => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addModel(this.form).then(() => {
              this.$message.success('新增成功')
              this.open = false
              this.getList()
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
        modelId: null,
        modelName: null,
        modelType: null,
        apiUrl: null,
        apiBaseUrl: null,
        apiKey: null,
        modelParams: null,
        status: '0',
        remark: null
      }
      this.resetForm('form')
    },
    extractBaseUrl(fullUrl) {
      // 从完整API地址中提取基础地址
      // 例如: https://aicanapi.com/v1beta/models/gemini-3-pro:generateContent
      // 提取为: https://aicanapi.com/v1beta
      try {
        if (fullUrl.includes('/models/')) {
          return fullUrl.substring(0, fullUrl.indexOf('/models/'))
        }
        return fullUrl
      } catch (e) {
        return fullUrl
      }
    }
  }
}
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
  line-height: 1.5;
}
</style>
